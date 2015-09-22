package com.interana.eventsim

import java.io.{OutputStream, Serializable}
import java.time.{ZoneOffset, LocalDateTime}

import com.interana.eventsim.Output.{EventWriter, JSONWriter, AvroWriter}
import com.interana.eventsim.config.ConfigFromFile

class User(val alpha: Double,
           val beta: Double,
           val startTime: LocalDateTime,
           val initialSessionStates: scala.collection.Map[(String,String),WeightedRandomThingGenerator[State]],
           val auth: String,
           val props: Map[String,Any],
           var device: scala.collection.immutable.Map[String,Any],
           val initialLevel: String,
           val stream: OutputStream
          ) extends Serializable with Ordered[User] {

  val userId = Counters.nextUserId
  var session = new Session(
    Some(Session.pickFirstTimeStamp(startTime, alpha, beta)),
      alpha, beta, initialSessionStates, auth, initialLevel)

  override def compare(that: User) =
    (that.session.nextEventTimeStamp, this.session.nextEventTimeStamp) match {
      case (None, None) => 0
      case (_: Some[LocalDateTime], None) => -1
      case (None, _: Some[LocalDateTime]) => 1
      case (thatValue: Some[LocalDateTime], thisValue: Some[LocalDateTime]) =>
        thatValue.get.compareTo(thisValue.get)
    }

  def nextEvent(): Unit = nextEvent(0.0)

  def nextEvent(prAttrition: Double) = {
    session.incrementEvent()
    if (session.done) {
      if (TimeUtilities.rng.nextDouble() < prAttrition ||
          session.currentState.auth == ConfigFromFile.churnedState.getOrElse("")) {
        session.nextEventTimeStamp = None
        // TODO: mark as churned
      }
      else {
        session = session.nextSession
      }
    }
  }

  val writer: Object with EventWriter = if (Main.useAvro) new AvroWriter(stream) else new JSONWriter(stream)

  def writeEvent() = {
    val showUserDetails = ConfigFromFile.showUserWithState(session.currentState.auth)
    writer.start
    writer.setTs(session.nextEventTimeStamp.get.toInstant(ZoneOffset.UTC)toEpochMilli())
    if (showUserDetails) writer.setUserId(userId)
    writer.setSessionId( session.sessionId)
    writer.setPage(session.currentState.page)
    writer.setAuth(session.currentState.auth)
    writer.setMethod(session.currentState.method)
    writer.setStatus(session.currentState.status)
    writer.setLevel(session.currentState.level)
    writer.setItemInSession(session.itemInSession)
    writer.setDeviceDetails(device)
    if (showUserDetails) {
      writer.setUserDetails(props)
      if (Main.tag.isDefined)
        writer.setTag(Main.tag.get)
    }
    if (session.currentState.page=="NextSong") {
      writer.setArtist(session.currentSong.get._2)
      writer.setTitle(session.currentSong.get._3)
      writer.setDuration(session.currentSong.get._4)
    }
    writer.end
  }

  def tsToString(ts: LocalDateTime) = ts.toString()

  def nextEventTimeStampString =
    tsToString(this.session.nextEventTimeStamp.get)

}

