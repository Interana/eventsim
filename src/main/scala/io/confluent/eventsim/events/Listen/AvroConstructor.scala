package io.confluent.eventsim.events.Listen


import io.confluent.eventsim.avro.{Listen, song}

import scala.collection.JavaConversions._

class AvroConstructor() extends io.confluent.eventsim.events.AvroConstructor[Listen] with Constructor {

  def schema = Listen.getClassSchema

  var eventBuilder = Listen.newBuilder()
  var songBuilder = song.newBuilder()

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


  def setAuth(s: String) = eventBuilder.setAuth(s)

  def setArtist(s: String) = songBuilder.setArtist(s)

  def setTitle(s: String) = songBuilder.setTitle(s)

  def setDuration(d: Float) = songBuilder.setDuration(d)

  override def start() = {
    eventBuilder = Listen.newBuilder(eventBuilder)
    songBuilder = song.newBuilder(songBuilder)
  }

  def end() = {
    eventBuilder.setSongProperties(songBuilder.build())
    eventBuilder.build()
  }


}
