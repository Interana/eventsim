package io.confluent.eventsim.events.PageView

import io.confluent.eventsim.avro.{PageView, song}

import scala.collection.JavaConversions._

class AvroConstructor() extends io.confluent.eventsim.events.AvroConstructor[PageView] with Constructor {

  def schema = PageView.getClassSchema

  var eventBuilder = PageView.newBuilder()

  def setTs(n: Long) = eventBuilder.setTs(n)

  def setUserId(n: Long) = eventBuilder.setUserId(n)

  def setSessionId(n: Long) = eventBuilder.setSessionId(n)

  def setLevel(s: String) = eventBuilder.setLevel(s)

  def setItemInSession(i: Int) = eventBuilder.setItemInSession(i)

  def setUserDetails(m: Map[String, Any]): Unit =
    eventBuilder.setUserDetails(m.asInstanceOf[Map[CharSequence, AnyRef]])

  def setDeviceDetails(m: Map[String, Any]): Unit =
    eventBuilder.setDeviceDetails(m.asInstanceOf[Map[CharSequence, AnyRef]])

  def setTag(s: String) = eventBuilder.setTag(s)


  var songBuilder = song.newBuilder()

  def setPage(s: String) = eventBuilder.setPage(s)

  def setAuth(s: String) = eventBuilder.setAuth(s)

  def setMethod(s: String) = eventBuilder.setMethod(s)

  def setStatus(i: Int) = eventBuilder.setStatus(i)

  def setArtist(s: String) = songBuilder.setArtist(s)

  def setTitle(s: String) = songBuilder.setTitle(s)

  def setDuration(d: Float) = songBuilder.setDuration(d)

  def start() = {
    eventBuilder = PageView.newBuilder(eventBuilder)
    songBuilder = song.newBuilder(songBuilder)
  }

  override def end() = {
    if (songBuilder.hasArtist)
      eventBuilder.setSongProperties(songBuilder.build())
    eventBuilder.build()
  }

}
