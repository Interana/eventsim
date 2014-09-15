package com.interana.eventsim

import java.io.Serializable

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.util.parsing.json.JSONObject

class User(val alpha: Double, // alpha = expected request inter-arrival time
           val beta: Double,  // beta  = expected session inter-arrival time
           val startTime: DateTime,
           val initialState: State,
           val props: scala.collection.immutable.Map[String,Any]) extends Serializable with Ordered[User] {

  val userId = Counters.nextUserId
  var session = new Session(Session.pickFirstTimeStamp(startTime, alpha, beta), alpha, beta, initialState)

  override def compare(that: User): Int = that.session.nextEventTimeStamp.compareTo(this.session.nextEventTimeStamp)

  def nextEvent() = {
    session.incrementEvent()
    if (session.done)
      session = session.nextSession
  }

  def eventString = {
    val m = props.+(
      "ts" -> session.nextEventTimeStamp.getMillis,
      "userId" -> userId,
      "sessionId" -> session.sessionId,
      "page" -> session.currentState.name,
      "itemInSession" -> session.itemInSession
    )
    val j = new JSONObject(m)
    j.toString()
  }

  def tsToString(ts: DateTime): String = {
      ts.toString(ISODateTimeFormat.dateTime())
  }

  def nextEventTimeStampString = tsToString(this.session.nextEventTimeStamp)

  def mkString = props.+(
    "alpha" -> alpha,
    "beta" -> beta,
    "startTime" -> tsToString(startTime),
    "initialState" -> initialState ,
    "nextEventTimeStamp" -> tsToString(session.nextEventTimeStamp) ,
    //"sessionEndTimeStamp" -> tsToString(session.sessionEndTimeStamp) ,
    "sessionId" -> session.sessionId ,
    "userId" -> userId ,
    "currentState" -> session.currentState)

}