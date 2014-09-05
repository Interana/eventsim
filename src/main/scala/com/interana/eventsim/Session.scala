package com.interana.eventsim

import com.interana.eventsim.TimeUtilities._
import org.joda.time.DateTime

/**
 * Created by jadler on 9/4/14.
 *
 * Object to capture session related calculations and properties
 *
 */
class Session(var nextEventTimeStamp: DateTime,
              val alpha: Double, val beta: Double, val gamma: Double,
              var currentState: State) {

  val sessionEndTimeStamp: DateTime = nextEventTimeStamp.plusMillis(exponentialRandomValue(alpha).toInt)
  val sessionId = Counters.nextSessionId
  var itemInSession = 0
  var done = false
  val initialState = currentState

  def incrementEvent = {
    val nextTimestamp = nextEventTimeStamp.plusMillis(exponentialRandomValue(alpha / beta).toInt)
    if (nextTimestamp.isAfter(sessionEndTimeStamp))
      done = true
    else
      nextEventTimeStamp = nextTimestamp
    currentState = currentState.nextState(rng)
    itemInSession += 1
  }

  def nextSession =
    new Session(Session.pickNextTimeStamp(nextEventTimeStamp, gamma), alpha, beta, gamma, initialState)

}

object Session {

  val SESSION_BREAK = 30 * 60 * 1000

  def pickFirstTimeStamp(st: DateTime, alpha: Double, gamma: Double): DateTime = {
    // pick random start point, iterate to steady state
    val startPoint = st.minusMillis(gamma.toInt * 3)
    var candidate = pickNextTimeStamp(startPoint, gamma)
    while (new DateTime(candidate).isBefore(new DateTime(st).minusMillis(alpha.toInt * 3))) {
      candidate = pickNextTimeStamp(candidate, gamma)
    }
    candidate
  }

  def pickNextTimeStamp(lastTimeStamp: DateTime, gamma: Double) = {
    var nextTimestamp: DateTime =
      TimeUtilities.standardWarp(lastTimeStamp.plusMillis(exponentialRandomValue(gamma).toInt + SESSION_BREAK))
    while (! keepThisDate(lastTimeStamp, nextTimestamp)) {
      nextTimestamp = TimeUtilities.standardWarp((lastTimeStamp.plusMillis(exponentialRandomValue(gamma).toInt)))
    }
    nextTimestamp
  }
}
