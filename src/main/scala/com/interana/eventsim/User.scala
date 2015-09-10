package com.interana.eventsim

import java.io.{OutputStream, Serializable}
import java.time.{ZoneOffset, LocalDateTime}

import com.fasterxml.jackson.core.{JsonEncoding, JsonFactory}
import com.interana.eventsim.config.ConfigFromFile
import org.apache.avro.file.DataFileWriter
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter

import scala.util.parsing.json.JSONObject

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

  private val EMPTY_MAP = Map()

  def eventString = {
    val showUserDetails = ConfigFromFile.showUserWithState(session.currentState.auth)
    var m = device.+(
      "ts" -> session.nextEventTimeStamp.get.toInstant(ZoneOffset.UTC).toEpochMilli,
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

    /* most of the event generator code is pretty generic, but this is hard-coded
     * for a fake music web site
     */
    if (session.currentState.page=="NextSong")
      m += (
        "artist" -> session.currentSong.get._2,
        "song" -> session.currentSong.get._3,
        "length" -> session.currentSong.get._4
        )

    val j = new JSONObject(m)
    j.toString()
  }


  val writer = if (Main.useAvro) new AvroWriter() else new JSONWriter()

  trait EventWriter {
    def setTs(n: Long)
    def setUserId(n: Long)
    def setSessionId(n: Long)
    def setPage(s: String)
    def setAuth(s: String)
    def setMethod(s: String)
    def setStatus(i: Int)
    def setLevel(s: String)
    def setItemInSession(i: Int)
    def setArtist(s: String)
    def setTitle(s: String)
    def setDuration(d: Float)
    def setUserDetails(m: Map[String,Any])
    def setTag(s: String)
    def start()
    def end()
  }

  class JSONWriter extends Object with EventWriter {
    // use Jackson streaming to maximize efficiency
    // (earlier versions used Scala's JSON generators, but they were slow)
    val generator = User.jsonFactory.createGenerator(stream, JsonEncoding.UTF8)
    def setTs(n: Long) = generator.writeNumberField("ts", n)
    def setUserId(n: Long) = generator.writeNumberField("userId", n)
    def setSessionId(n: Long) = generator.writeNumberField("sessionId", n)
    def setPage(s: String) = generator.writeStringField("page",s)
    def setAuth(s: String) = generator.writeStringField("auth", s)
    def setMethod(s: String) = generator.writeStringField("method", s)
    def setStatus(i: Int) = generator.writeNumberField("status", i)
    def setLevel(s: String) = generator.writeStringField("level", s)
    def setItemInSession(i: Int) = generator.writeNumberField("itemInSession", i)
    def setArtist(s: String) = generator.writeStringField("artist", s)
    def setTitle(s: String) = generator.writeStringField("song", s)
    def setDuration(f: Float) = generator.writeNumberField("duration", f)
    def setUserDetails(m: Map[String,Any]) =
      props.foreach((p: (String, Any)) => {
        p._2 match {
          case _: Long => generator.writeNumberField(p._1, p._2.asInstanceOf[Long])
          case _: Int => generator.writeNumberField(p._1, p._2.asInstanceOf[Int])
          case _: Double => generator.writeNumberField(p._1, p._2.asInstanceOf[Double])
          case _: Float => generator.writeNumberField(p._1, p._2.asInstanceOf[Float])
          case _: String => generator.writeStringField(p._1, p._2.asInstanceOf[String])
        }})
    def setTag(s: String) = generator.writeStringField("tag", s)
    def start = generator.writeStartObject()
    def end() = {
      generator.writeEndObject()
      generator.writeRaw('\n')
      generator.flush()
    }
  }

  class AvroWriter extends Object with EventWriter {
    val encoder = EncoderFactory.get().binaryEncoder(stream, null)
    val datumWriter = new SpecificDatumWriter[Event](Event.getClassSchema)
    val dataFileWriter = new DataFileWriter[Event](datumWriter)
    var eventBuilder = Event.newBuilder()
    var songBuilder = song.newBuilder()
    def setTs(n: Long) = eventBuilder.setTs(n)
    def setUserId(n: Long) = eventBuilder.setUserId(n)
    def setSessionId(n: Long) = eventBuilder.setSessionId(n)
    def setPage(s: String) = eventBuilder.setPage(s)
    def setAuth(s: String) = eventBuilder.setAuth(s)
    def setMethod(s: String) = eventBuilder.setMethod(s)
    def setStatus(i: Int) = eventBuilder.setStatus(i)
    def setLevel(s: String) = eventBuilder.setLevel(s)
    def setItemInSession(i: Int) = eventBuilder.setItemInSession(i)
    def setArtist(s: String) = songBuilder.setArtist(s)
    def setTitle(s: String) = songBuilder.setTitle(s)
    def setDuration(d: Float) = songBuilder.setDuration(d)
    def setUserDetails(m: Map[String,Any]) = eventBuilder.setUserDetails(m.asInstanceOf[java.util.Map[CharSequence,AnyRef]])
    def setTag(s: String) = eventBuilder.setTag(s)
    def start() = {
      eventBuilder = Event.newBuilder(eventBuilder)
      songBuilder = song.newBuilder(songBuilder)
    }
    def end() = {
      eventBuilder.setSongProperties(songBuilder.build())
      val e = eventBuilder.build()
      dataFileWriter.append(e)
    }

  }

  val builder = Event.newBuilder()
  val songBuilder = com.interana.eventsim.song.newBuilder()

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