package com.interana.eventsim

import de.jollyday.{HolidayCalendar, HolidayManager}
import org.apache.commons.math3.random.{MersenneTwister, RandomGenerator}
import org.joda.time.{DateTime, DateTimeConstants, LocalDate}

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

  val rng: RandomGenerator = new MersenneTwister(0L)
  // If X has a standard uniform distribution, then by the inverse transform sampling method,
  // Y = − (1/λ) ln(X) has an exponential distribution with (rate) parameter λ
  // mu = (1 / lambda)

  def exponentialRandomValue(mu: Double) = -mu * Math.log(rng.nextDouble())

  def weekendDamping(dt: DateTime) = {
    if (dt.toLocalTime.getHourOfDay < 6 && isWeekendOrHoliday(dt.minusHours(6).toLocalDate)) {
      1.0f - (0.5f * dt.minuteOfDay().get() / 360)
    } else if (dt.toLocalTime.getHourOfDay > 18 && isWeekendOrHoliday(dt.plusHours(18).toLocalDate)) {
      0.5f + (0.5f * (dt.minuteOfDay().get() - 1080) / 360)
    } else {
      0.5f
    }
  }

  def keepThisDate(lastTs: DateTime, newTs: DateTime) = {
    val wd = rng.nextDouble() < weekendDamping(newTs)
    newTs.isAfter(lastTs) && !((isHoliday(newTs) || isWeekend(newTs)) && wd)
  }

  val MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000

  def warpOffset(ts:DateTime, offsetMilliSeconds: Long, dampingFactor: Double): Int = {
    val ms = ts.millisOfDay().get().toLong
    (dampingFactor * MILLISECONDS_PER_DAY * Math.sin( (ms - offsetMilliSeconds) * 2 * Math.PI / MILLISECONDS_PER_DAY)).toInt
  }

  val THREE_AM = 60 * 3 * 1000
  val DEFAULT_DAMPING = 0.0625
  def standardOffset(ts: DateTime) = warpOffset(ts, THREE_AM, DEFAULT_DAMPING)
  def standardWarp(ts: DateTime) = ts.plusMillis(warpOffset(ts, THREE_AM, DEFAULT_DAMPING))

}
