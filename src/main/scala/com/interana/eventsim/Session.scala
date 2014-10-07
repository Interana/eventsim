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

    val nextState = currentState.nextState(rng)
    if (nextState.nonEmpty) {
      val nextTimeStamp = nextEventTimeStamp.get.plusSeconds(
      if (nextState.get.status >= 300 && nextState.get.status <= 399) 1 else exponentialRandomValue(alpha).toInt)
      nextEventTimeStamp = Some(nextTimeStamp)
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

  def pickFirstTimeStamp(st: DateTime,
    alpha: Double, // alpha = expected request inter-arrival time
    beta: Double  // beta  = expected session inter-arrival time
   ): DateTime = {
    // pick random start point, iterate to steady state
    val startPoint = st.minusSeconds(beta.toInt * 2)
    var candidate = pickNextSessionStartTime(startPoint, beta)
    while (new DateTime(candidate).isBefore(new DateTime(st).minusSeconds(beta.toInt))) {
      candidate = pickNextSessionStartTime(candidate, beta)
    }
    candidate
  }

  def pickNextSessionStartTime(lastTimeStamp: DateTime, beta: Double): DateTime = {
    val randomGap = exponentialRandomValue(beta).toInt + SiteConfig.sessionGap
    val nextTimestamp: DateTime = TimeUtilities.standardWarp(lastTimeStamp.plusSeconds(randomGap))
    assert(randomGap > 0)

    if (nextTimestamp.isBefore(lastTimeStamp)) {
      // force forward progress
      pickNextSessionStartTime(lastTimeStamp.plusSeconds(SiteConfig.sessionGap), beta)
    } else if (keepThisDate(lastTimeStamp, nextTimestamp)) {
      nextTimestamp
    } else
      pickNextSessionStartTime(nextTimestamp, beta)
  }
}
