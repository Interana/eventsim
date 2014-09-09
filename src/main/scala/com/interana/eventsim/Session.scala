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
              val alpha: Double, // alpha = expected request inter-arrival time
              val beta: Double,  // beta  = expected session inter-arrival time
              val initialState: State) {

  val sessionId = Counters.nextSessionId
  var itemInSession = 0
  var done = false
  var currentState:State = initialState.nextState(rng).get

  def incrementEvent() = {
    val nextTimestamp = nextEventTimeStamp.plusMillis(exponentialRandomValue(alpha).toInt)
    val nextState = currentState.nextState(rng)
    if (nextState.nonEmpty) {
      nextEventTimeStamp = nextTimestamp
      currentState = nextState.get
      itemInSession += 1
    } else {
      done = true
    }
  }

  def nextSession =
    new Session(Session.pickNextSessionStartTime(nextEventTimeStamp, beta), alpha, beta, initialState)

}

object Session {

  val SESSION_BREAK = 30 * 60 * 1000

  def pickFirstTimeStamp(st: DateTime,
    alpha: Double, // alpha = expected request inter-arrival time
    beta: Double  // beta  = expected session inter-arrival time
   ): DateTime = {
    // pick random start point, iterate to steady state
    val startPoint = st.minusMillis(beta.toInt * 3)
    var candidate = pickNextSessionStartTime(startPoint, beta)
    while (new DateTime(candidate).isBefore(new DateTime(st).minusMillis(alpha.toInt * 3))) {
      candidate = pickNextSessionStartTime(candidate, beta)
    }
    candidate
  }

  def pickNextSessionStartTime(lastTimeStamp: DateTime, beta: Double) = {
    var nextTimestamp: DateTime =
      TimeUtilities.standardWarp(lastTimeStamp.plusMillis(exponentialRandomValue(beta).toInt + SESSION_BREAK))
    while (! keepThisDate(lastTimeStamp, nextTimestamp)) {
      nextTimestamp = TimeUtilities.standardWarp(lastTimeStamp.plusMillis(exponentialRandomValue(beta).toInt))
    }
    nextTimestamp
  }
}
