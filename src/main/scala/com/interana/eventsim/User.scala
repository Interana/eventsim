package com.interana.eventsim

import java.io.{OutputStream, Serializable}

import com.fasterxml.jackson.core.{JsonEncoding, JsonFactory, JsonGenerator}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.util.parsing.json.JSONObject

class User(val alpha: Double, // alpha = expected request inter-arrival time
           val beta: Double,  // beta  = expected session inter-arrival time
           val startTime: DateTime,
           val initialSessionStates: scala.collection.Map[(String,String),WeightedRandomThingGenerator[State]],
           val auth: String,
           val props: scala.collection.immutable.Map[String,Any],
           var device: scala.collection.immutable.Map[String,Any],
           val stream: OutputStream
          ) extends Serializable with Ordered[User] {

  val userId = Counters.nextUserId
  var session = new Session(
    Some(Session.pickFirstTimeStamp(startTime, alpha, beta)),
      alpha, beta, initialSessionStates, auth, props.getOrElse("level","").asInstanceOf[String])

  override def compare(that: User): Int =
    (that.session.nextEventTimeStamp, this.session.nextEventTimeStamp) match {
      case (None, None) => 0
      case (_: Some[DateTime], None) => -1
      case (None, _: Some[DateTime]) => 1
      case (thatValue: Some[DateTime], thisValue: Some[DateTime]) =>
        thatValue.get.compareTo(thisValue.get)
    }

  def nextEvent(): Unit = nextEvent(0.0)

  def nextEvent(prAttrition: Double) = {
    session.incrementEvent()
    if (session.done) {
      if (TimeUtilities.rng.nextDouble() < prAttrition ||
          session.currentState.auth == SiteConfig.churnedState.getOrElse("")) {
        session.nextEventTimeStamp = None
        // TODO: mark as churned
      }
      else {
        session = session.nextSession
      }
    }
  }

  private val EMPTY_MAP = Map()

  def eventString = {
    val showUserDetails = SiteConfig.showUserWithState(session.currentState.auth)
    var m = device.+(
      "ts" -> session.nextEventTimeStamp.get.getMillis,
      "userId" -> (if (showUserDetails) userId else ""),
      "sessionId" -> session.sessionId,
      "page" -> session.currentState.page,
      "auth" -> session.currentState.auth,
      "method" -> session.currentState.method,
      "status" -> session.currentState.status,
      "itemInSession" -> session.itemInSession
    )

    if (showUserDetails)
      m ++= props

    if (session.currentState.page=="NextSong")
      m += (
        "artist" -> session.currentSong.get._2,
        "song" -> session.currentSong.get._3,
        "length" -> session.currentSong.get._4
        )

    val j = new JSONObject(m)
    j.toString()
  }


  val writer: JsonGenerator = User.jsonFactory.createGenerator(stream, JsonEncoding.UTF8)

  def writeEvent = {
    val showUserDetails = SiteConfig.showUserWithState(session.currentState.auth)
    writer.writeStartObject()
    writer.writeNumberField("ts", session.nextEventTimeStamp.get.getMillis)
    writer.writeStringField("userId", (if (showUserDetails) userId.toString() else ""))
    writer.writeNumberField("sessionId", session.sessionId)
    writer.writeStringField("page", session.currentState.page)
    writer.writeStringField("auth", session.currentState.auth)
    writer.writeStringField("method", session.currentState.method)
    writer.writeNumberField("status", session.currentState.status)
    writer.writeNumberField("itemInSession", session.itemInSession)
    if (showUserDetails) {
      props.foreach((p: (String, Any)) => {
        //writer.writeObjectField(p._1, p._2)
        p._2 match {
          case _: Long => writer.writeNumberField(p._1, p._2.asInstanceOf[Long])
          case _: Int => writer.writeNumberField(p._1, p._2.asInstanceOf[Int])
          case _: Double => writer.writeNumberField(p._1, p._2.asInstanceOf[Double])
          case _: Float => writer.writeNumberField(p._1, p._2.asInstanceOf[Float])
          case _: String => writer.writeStringField(p._1, p._2.asInstanceOf[String])
        }})
      if (Main.Conf.tag.isSupplied)
        writer.writeStringField("tag", Main.Conf.tag.get.get)
    }
    if (session.currentState.page=="NextSong") {
      writer.writeStringField("artist", session.currentSong.get._2)
      writer.writeStringField("song",  session.currentSong.get._3)
      writer.writeNumberField("length", session.currentSong.get._4)
    }
    writer.writeEndObject()
    writer.writeRaw('\n')
    writer.flush()
  }

  def tsToString(ts: DateTime): String = {
      ts.toString(ISODateTimeFormat.dateTime())
  }

  def nextEventTimeStampString = tsToString(this.session.nextEventTimeStamp.get)

  def mkString = props.+(
    "alpha" -> alpha,
    "beta" -> beta,
    "startTime" -> tsToString(startTime),
    "initialSessionStates" -> initialSessionStates,
    "nextEventTimeStamp" -> tsToString(session.nextEventTimeStamp.get) ,
    "sessionId" -> session.sessionId ,
    "userId" -> userId ,
    "currentState" -> session.currentState)

}

object User {
  protected val jsonFactory = new JsonFactory()
  jsonFactory.setRootValueSeparator("")
}