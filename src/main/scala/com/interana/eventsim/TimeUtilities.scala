package com.interana.eventsim

import de.jollyday.{HolidayCalendar, HolidayManager}
import org.apache.commons.math3.random.{MersenneTwister, RandomGenerator}
import org.joda.time.{Interval, DateTime, DateTimeConstants, LocalDate}
import Constants._

object TimeUtilities {

  // first implementation: US only
  val holidays = HolidayManager.getInstance(HolidayCalendar.UNITED_STATES)
  def isHoliday(ld: LocalDate):Boolean = holidays.isHoliday(ld)
  def isHoliday(ts: DateTime): Boolean = isHoliday(new LocalDate(ts))

  def isWeekend(ld: LocalDate): Boolean = {
    val dow = ld.getDayOfWeek
    dow == DateTimeConstants.SATURDAY || dow == DateTimeConstants.SUNDAY
  }
  def isWeekend(ts: DateTime): Boolean = isWeekend(new LocalDate(ts))

  def isWeekendOrHoliday(ld: LocalDate) = isWeekend(ld) || isHoliday(ld)

  val rng: RandomGenerator = new MersenneTwister(SiteConfig.seed)
  // If X has a standard uniform distribution, then by the inverse transform sampling method,
  // Y = − (1/λ) ln(X) has an exponential distribution with (rate) parameter λ
  // mu = (1 / lambda)

  def exponentialRandomValue(mu: Double) = -mu * Math.log(rng.nextDouble())

  def weekendDamping(dt: DateTime) = {

    val lastMidnight = dt.withMillisOfDay(0)
    val noon = lastMidnight.withHourOfDay(12)
    val lastNoon = noon.minusDays(1)
    val nextMidnight = lastMidnight.plusDays(1)
    val nextNoon = noon.plusDays(1)

    val wOrH_yesterday = isWeekendOrHoliday(lastNoon.toLocalDate)
    val wOrH_noon      = isWeekendOrHoliday(noon.toLocalDate)
    val wOrH_tomorrow  = isWeekendOrHoliday(nextNoon.toLocalDate)

    (wOrH_yesterday, wOrH_noon, wOrH_tomorrow) match {
      case (false, false, false) => 0.0
      case (true,  true,  true) => SiteConfig.weekendDamping

      case (false, false, true) =>
        val nextMidnightMinusOffset = nextMidnight.minusMinutes(SiteConfig.weekendDampingOffset)
        if (dt.isBefore(nextMidnightMinusOffset))
          0.0
        else
          SiteConfig.weekendDamping *
            (new Interval(nextMidnightMinusOffset, dt).toDurationMillis / 60000) / SiteConfig.weekendDampingScale


      case (true, false, false) =>
        val lastMidnightPlusOffset = lastMidnight.plusMinutes(SiteConfig.weekendDampingOffset)
        if (dt.isAfter(lastMidnightPlusOffset))
          0.0
        else
          SiteConfig.weekendDamping *
            (new Interval(dt, lastMidnightPlusOffset).toDurationMillis / 60000) / SiteConfig.weekendDampingScale


      case (false, true, false) =>
        val lastMidnightMinusOffset = lastMidnight.minusMinutes(SiteConfig.weekendDampingOffset)
        val endOfRampUp = lastMidnightMinusOffset.plusMinutes(SiteConfig.weekendDampingScale)
        val nextMidnightPlusOffset = nextMidnight.plusMinutes(SiteConfig.weekendDampingOffset)
        val startOfRollDown = nextMidnightPlusOffset.minusMinutes(SiteConfig.weekendDampingScale)
        if (dt.isBefore(endOfRampUp))
          SiteConfig.weekendDamping *
            (new Interval(lastMidnightMinusOffset, dt).toDurationMillis / 60000) / SiteConfig.weekendDampingScale
        else if (dt.isAfter(startOfRollDown))
          SiteConfig.weekendDamping *
            (new Interval(dt, nextMidnightPlusOffset).toDurationMillis / 60000) / SiteConfig.weekendDampingScale
        else SiteConfig.weekendDamping

      case (false, true, true) =>
        val lastMidnightMinusOffset = lastMidnight.minusMinutes(SiteConfig.weekendDampingOffset)
        val endOfRampUp = lastMidnightMinusOffset.plusMinutes(SiteConfig.weekendDampingScale)
        if (dt.isBefore(endOfRampUp))
          SiteConfig.weekendDamping *
            (new Interval(lastMidnightMinusOffset, dt).toDurationMillis / 60000) / SiteConfig.weekendDampingScale
        else SiteConfig.weekendDamping

      case (true, false, true) =>
        val lastMidnightPlusOffset = lastMidnight.plusMinutes(SiteConfig.weekendDampingOffset)
        val nextMidnightMinusOffset = nextMidnight.minusMinutes(SiteConfig.weekendDampingOffset)
        if (dt.isBefore(lastMidnightPlusOffset)) {
          SiteConfig.weekendDamping *
            (new Interval(dt, lastMidnightPlusOffset).toDurationMillis / 60000) / SiteConfig.weekendDampingScale
        } else if (dt.isAfter(nextMidnightMinusOffset)) {
          SiteConfig.weekendDamping *
            (new Interval(nextMidnightMinusOffset, dt).toDurationMillis / 60000) / SiteConfig.weekendDampingScale
        } else
          0.0

      case (true, true, false) =>
        val nextMidnightPlusOffset = nextMidnight.plusMinutes(SiteConfig.weekendDampingOffset)
        val startOfRollDown = nextMidnightPlusOffset.minusMinutes(SiteConfig.weekendDampingScale)
        if (dt.isAfter(startOfRollDown))
          SiteConfig.weekendDamping *
            (new Interval(dt, nextMidnightPlusOffset).toDurationMillis / 60000) / SiteConfig.weekendDampingScale
        else SiteConfig.weekendDamping


    }

  }

  def keepThisDate(lastTs: DateTime, newTs: DateTime) = {
    val damping = weekendDamping(newTs)
    val weekday = if (damping > 0.0) rng.nextDouble() < 1.0 - weekendDamping(newTs) else true
    newTs.isAfter(lastTs) && weekday
  }


  def warpOffset(ts:DateTime, offsetSeconds: Long, dampingFactor: Double): Int = {
    val s = ts.secondOfDay().get().toLong
    (dampingFactor * SECONDS_PER_DAY * Math.sin( (s - offsetSeconds) * 2 * Math.PI / SECONDS_PER_DAY)).toInt
  }

  def standardOffset(ts: DateTime) = warpOffset(ts, THREE_AM, SiteConfig.damping)
  def standardWarp(ts: DateTime) = ts.plusSeconds(warpOffset(ts, THREE_AM, SiteConfig.damping))


  def reverseWarpOffset(ts: DateTime, offsetSeconds: Long, dampingFactor: Double): Int = {
    val s = ts.secondOfDay().get().toLong
    (Math.asin(s / (dampingFactor * SECONDS_PER_DAY)) / (2 * Math.PI / SECONDS_PER_DAY ) + offsetSeconds).toInt
  }

  def reverseStandardWarp(ts: DateTime) = ts.minusSeconds(reverseWarpOffset(ts, THREE_AM, SiteConfig.damping))

}
