package com.interana.eventsim

import java.time.temporal.{ChronoField, ChronoUnit}
import java.time.{DayOfWeek, Duration, LocalDateTime, LocalDate}

import com.interana.eventsim.Constants._
import com.interana.eventsim.config.ConfigFromFile
import de.jollyday.HolidayManager
import org.apache.commons.math3.random.MersenneTwister

object TimeUtilities {

  // def dateTimeToLocalDate(dt: Instant): LocalDate = LocalDate.from(Instant.ofEpochMilli(dt.getMillis()))

  // first implementation: US only
  val holidays = HolidayManager.getInstance()
  def isHoliday(ld: LocalDate): Boolean = holidays.isHoliday(ld)

  def isWeekend(ld: LocalDate): Boolean = {
    val dow = ld.getDayOfWeek
    dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
  }

  def isWeekendOrHoliday(i: LocalDateTime): Boolean = isWeekendOrHoliday(LocalDate.from(i))
  def isWeekendOrHoliday(ld: LocalDate): Boolean = isWeekend(ld) || isHoliday(ld)

  val rng = new MersenneTwister(Main.seed) // Mersenne Twisters are fast and good enough for fake data

  // If X has a standard uniform distribution, then by the inverse transform sampling method,
  // Y = − (1/λ) ln(X) has an exponential distribution with (rate) parameter λ
  // mu = (1 / lambda)

  def exponentialRandomValue(mu: Double) = -mu * Math.log(rng.nextDouble())


  def weekendDamping(dt: LocalDateTime) = {
    // gradually scale down traffic volume on weekends
    val lastMidnight = dt.truncatedTo(ChronoUnit.DAYS)
    val noon = lastMidnight.plus(12, ChronoUnit.HOURS)
    val lastNoon = noon.minus(1, ChronoUnit.DAYS)
    val nextMidnight = lastMidnight.plus(1, ChronoUnit.DAYS)
    val nextNoon = noon.plus(1, ChronoUnit.DAYS)

    val wOrH_yesterday = isWeekendOrHoliday(lastNoon)
    val wOrH_noon      = isWeekendOrHoliday(noon)
    val wOrH_tomorrow  = isWeekendOrHoliday(nextNoon)

    (wOrH_yesterday, wOrH_noon, wOrH_tomorrow) match {
      case (false, false, false) => 0.0
      case (true,  true,  true) => ConfigFromFile.weekendDamping

      case (false, false, true) =>
        val nextMidnightMinusOffset = nextMidnight.minus(ConfigFromFile.weekendDampingOffset, ChronoUnit.MINUTES)
        if (dt.isBefore(nextMidnightMinusOffset))
          0.0
        else
          ConfigFromFile.weekendDamping *
            Duration.between(nextMidnightMinusOffset, dt).toMillis / 60000 / ConfigFromFile.weekendDampingScale

      case (true, false, false) =>
        val lastMidnightPlusOffset = lastMidnight.plus(ConfigFromFile.weekendDampingOffset, ChronoUnit.MINUTES)
        if (dt.isAfter(lastMidnightPlusOffset))
          0.0
        else
          ConfigFromFile.weekendDamping *
            Duration.between(dt, lastMidnightPlusOffset).toMillis / 60000 / ConfigFromFile.weekendDampingScale

      case (false, true, false) =>
        val lastMidnightMinusOffset = lastMidnight.minus(ConfigFromFile.weekendDampingOffset, ChronoUnit.MINUTES)
        val endOfRampUp = lastMidnightMinusOffset.plus(ConfigFromFile.weekendDampingScale, ChronoUnit.MINUTES)
        val nextMidnightPlusOffset = nextMidnight.plus(ConfigFromFile.weekendDampingOffset, ChronoUnit.MINUTES)
        val startOfRollDown = nextMidnightPlusOffset.minus(ConfigFromFile.weekendDampingScale, ChronoUnit.MINUTES)
        if (dt.isBefore(endOfRampUp))
          ConfigFromFile.weekendDamping *
            Duration.between(lastMidnightMinusOffset, dt).toMillis / 60000 / ConfigFromFile.weekendDampingScale
        else if (dt.isAfter(startOfRollDown))
          ConfigFromFile.weekendDamping *
            Duration.between(dt, nextMidnightPlusOffset).toMillis / 60000 / ConfigFromFile.weekendDampingScale
        else ConfigFromFile.weekendDamping

      case (false, true, true) =>
        val lastMidnightMinusOffset = lastMidnight.minus(ConfigFromFile.weekendDampingOffset, ChronoUnit.MINUTES)
        val endOfRampUp = lastMidnightMinusOffset.plus(ConfigFromFile.weekendDampingScale, ChronoUnit.MINUTES)
        if (dt.isBefore(endOfRampUp))
          ConfigFromFile.weekendDamping *
            Duration.between(lastMidnightMinusOffset, dt).toMillis / 60000 / ConfigFromFile.weekendDampingScale
        else ConfigFromFile.weekendDamping

      case (true, false, true) =>
        val lastMidnightPlusOffset = lastMidnight.plus(ConfigFromFile.weekendDampingOffset, ChronoUnit.MINUTES)
        val nextMidnightMinusOffset = nextMidnight.minus(ConfigFromFile.weekendDampingOffset, ChronoUnit.MINUTES)
        if (dt.isBefore(lastMidnightPlusOffset)) {
          ConfigFromFile.weekendDamping *
            Duration.between(dt, lastMidnightPlusOffset).toMillis / 60000 / ConfigFromFile.weekendDampingScale
        } else if (dt.isAfter(nextMidnightMinusOffset)) {
          ConfigFromFile.weekendDamping *
            Duration.between(nextMidnightMinusOffset, dt).toMillis / 60000 / ConfigFromFile.weekendDampingScale
        } else
          0.0

      case (true, true, false) =>
        val nextMidnightPlusOffset = nextMidnight.plus(ConfigFromFile.weekendDampingOffset, ChronoUnit.MINUTES)
        val startOfRollDown = nextMidnightPlusOffset.minus(ConfigFromFile.weekendDampingScale, ChronoUnit.MINUTES)
        if (dt.isAfter(startOfRollDown))
          ConfigFromFile.weekendDamping *
            Duration.between(dt, nextMidnightPlusOffset).toMillis / 60000 / ConfigFromFile.weekendDampingScale
        else ConfigFromFile.weekendDamping

    }
  }

  def keepThisDate(lastTs: LocalDateTime, newTs: LocalDateTime) =
    if (weekendDamping(newTs) > 0.0) rng.nextDouble() < 1.0 - weekendDamping(newTs) else true

  def warpOffset(ts:LocalDateTime, offsetSeconds: Long, dampingFactor: Double): Int = {
    val s = ts.getLong(ChronoField.SECOND_OF_DAY)
    (dampingFactor * SECONDS_PER_DAY * Math.sin( (s - offsetSeconds) * 2 * Math.PI / SECONDS_PER_DAY)).toInt
  }

  def standardOffset(ts: LocalDateTime) = warpOffset(ts, THREE_AM, ConfigFromFile.damping)
  def standardWarp(ts: LocalDateTime) = ts.plusSeconds(warpOffset(ts, THREE_AM, ConfigFromFile.damping))

  def reverseWarpOffset(ts: LocalDateTime, offsetSeconds: Long, dampingFactor: Double) = {
    val s = ts.getLong(ChronoField.SECOND_OF_DAY)
    (Math.asin(s / (dampingFactor * SECONDS_PER_DAY)) / (2 * Math.PI / SECONDS_PER_DAY ) + offsetSeconds).toInt
  }

  def reverseStandardWarp(ts: LocalDateTime) = ts.minusSeconds(reverseWarpOffset(ts, THREE_AM, ConfigFromFile.damping))

}
