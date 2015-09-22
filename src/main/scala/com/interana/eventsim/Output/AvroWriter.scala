package com.interana.eventsim.Output

import java.io.OutputStream

import com.interana.eventsim.{song, Event}
import org.apache.avro.file.DataFileWriter
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter

class AvroWriter(val stream: OutputStream) extends Object with EventWriter {
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

  def setDeviceDetails(m: Map[String, Any]): Unit =
    eventBuilder.setDeviceDetails(m.asInstanceOf[java.util.Map[CharSequence,AnyRef]])
}
