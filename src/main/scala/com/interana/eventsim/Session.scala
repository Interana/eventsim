package com.interana.eventsim

import com.interana.eventsim.TimeUtilities._
import org.joda.time.DateTime

/**
 * Created by jadler on 9/4/14.
 *
 * Object to capture session related calculations and properties
 *
 */
class Session(var nextEventTimeStamp: Option[DateTime],
              val alpha: Double, // alpha = expected request inter-arrival time
              val beta: Double,  // beta  = expected session inter-arrival time
              val initialState: State) {

  val sessionId = Counters.nextSessionId
  var itemInSession = 0
  var done = false
  var currentState:State = initialState.nextState(rng).get

  def incrementEvent() = {
    val nextTimestamp = nextEventTimeStamp.get.plusSeconds(exponentialRandomValue(alpha).toInt)
    val nextState = currentState.nextState(rng)
    if (nextState.nonEmpty) {
      nextEventTimeStamp = Some(nextTimestamp)
      currentState = nextState.get
      itemInSession += 1
    } else {
      done = true
    }
  }

  def nextSession =
    new Session(Some(Session.pickNextSessionStartTime(nextEventTimeStamp.get, beta)), alpha, beta, initialState)

}

object Session {

  val SESSION_BREAK = 30 * 60 * 1000

  def pickFirstTimeStamp(st: DateTime,
    alpha: Double, // alpha = expected request inter-arrival time
    beta: Double  // beta  = expected session inter-arrival time
   ): DateTime = {
    // pick random start point, iterate to steady state
    val startPoint = st.minusSeconds(beta.toInt * 3)
    var candidate = pickNextSessionStartTime(startPoint, beta)
    while (new DateTime(candidate).isBefore(new DateTime(st).minusSeconds(alpha.toInt * 3))) {
      candidate = pickNextSessionStartTime(candidate, beta)
    }
    candidate
  }

  def pickNextSessionStartTime(lastTimeStamp: DateTime, beta: Double): DateTime = {
    val randomGap = exponentialRandomValue(beta).toInt + SESSION_BREAK
    val nextTimestamp: DateTime = lastTimeStamp.plusSeconds(randomGap)

    /*
    System.err.write(("\r pickNextSessionStartTime (Last=" +
      lastTimeStamp.toString(ISODateTimeFormat.dateTime()) +
      ", Next=" + nextTimestamp.toString(ISODateTimeFormat.dateTime()) +
      ", beta=" + beta.toString() +
      ", randomGap=" + randomGap + "\n"

      ).getBytes)
     */

    assert(randomGap > 0)

    if (keepThisDate(lastTimeStamp, nextTimestamp)) {
      if (randomGap > Constants.SECONDS_PER_DAY)
        TimeUtilities.standardWarp(nextTimestamp)
      else
        nextTimestamp
    } else
      pickNextSessionStartTime(lastTimeStamp, beta)

    /*
    var ticker = 1
    while (! keepThisDate(lastTimeStamp, nextTimestamp)) {
      System.err.write(("\r pickNextSessionStartTime (Last=" +
        lastTimeStamp.toString(ISODateTimeFormat.dateTime()) +
        ", Next=" + nextTimestamp.toString(ISODateTimeFormat.dateTime()) +
        ") ticker=" + ticker.toInt).getBytes)
      ticker += 1
      nextTimestamp = nextTimestamp.plusMillis(exponentialRandomValue(beta).toInt)
    }
    TimeUtilities.standardWarp(nextTimestamp)
    */
  }
}
