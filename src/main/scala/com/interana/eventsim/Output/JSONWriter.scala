package com.interana.eventsim.Output

import java.io.OutputStream

import com.fasterxml.jackson.core.{JsonFactory, JsonEncoding}

class JSONWriter(val stream: OutputStream) extends Object with EventWriter {
  // use Jackson streaming to maximize efficiency
  // (earlier versions used Scala's JSON generators, but they were slow)
  val jsonFactory = new JsonFactory()
  jsonFactory.setRootValueSeparator("")
  val generator = jsonFactory.createGenerator(stream, JsonEncoding.UTF8)
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
    m.foreach((p: (String, Any)) => {
      p._2 match {
        case _: Long => generator.writeNumberField(p._1, p._2.asInstanceOf[Long])
        case _: Int => generator.writeNumberField(p._1, p._2.asInstanceOf[Int])
        case _: Double => generator.writeNumberField(p._1, p._2.asInstanceOf[Double])
        case _: Float => generator.writeNumberField(p._1, p._2.asInstanceOf[Float])
        case _: String => generator.writeStringField(p._1, p._2.asInstanceOf[String])
      }})
  def setDeviceDetails(m: Map[String,Any]) =
    m.foreach((p: (String, Any)) => {
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
