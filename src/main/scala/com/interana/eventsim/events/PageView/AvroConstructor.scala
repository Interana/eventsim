package com.interana.eventsim.events.PageView


import com.interana.eventsim.events.Builder
import io.confluent.eventsim.{song, PageView}

class AvroConstructor() extends com.interana.eventsim.events.AvroConstructor with Constructor {
  override var eventBuilder = PageView.newBuilder().asInstanceOf[Builder]
  var songBuilder = song.newBuilder()
  def setPage(s: String) = eventBuilder.asInstanceOf[PageView].setPage(s)
  def setAuth(s: String) = eventBuilder.asInstanceOf[PageView].setAuth(s)
  def setMethod(s: String) = eventBuilder.asInstanceOf[PageView].setMethod(s)
  def setStatus(i: Int) = eventBuilder.asInstanceOf[PageView].setStatus(i)
  def setArtist(s: String) = songBuilder.setArtist(s)
  def setTitle(s: String) = songBuilder.setTitle(s)
  def setDuration(d: Float) = songBuilder.setDuration(d)

  def start() = {
    eventBuilder = PageView.newBuilder(eventBuilder.asInstanceOf[PageView]).asInstanceOf[Builder]
    songBuilder = song.newBuilder(songBuilder)
  }

  override def end() = {
    eventBuilder.asInstanceOf[PageView].setSongProperties(songBuilder.build())
    eventBuilder.build()
  }

}
