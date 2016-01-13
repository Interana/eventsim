package com.interana.eventsim.events.Listen


import com.interana.eventsim.events.Builder
import io.confluent.eventsim.{Listen, song}

class AvroConstructor() extends com.interana.eventsim.events.AvroConstructor with Constructor {
  override var eventBuilder = Listen.newBuilder().asInstanceOf[Builder]
  var songBuilder = song.newBuilder()
  def setAuth(s: String) = eventBuilder.asInstanceOf[Listen].setAuth(s)
  def setArtist(s: String) = songBuilder.setArtist(s)
  def setTitle(s: String) = songBuilder.setTitle(s)
  def setDuration(d: Float) = songBuilder.setDuration(d)

  override def start() = {
    eventBuilder = Listen.newBuilder(eventBuilder.asInstanceOf[Listen]).asInstanceOf[Builder]
    songBuilder = song.newBuilder(songBuilder)
  }
  override def end() = {
    eventBuilder.asInstanceOf[Listen].setSongProperties(songBuilder.build())
    eventBuilder.build()
  }

}
