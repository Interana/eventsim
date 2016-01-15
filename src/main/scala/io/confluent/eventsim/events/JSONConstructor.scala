package io.confluent.eventsim.events

import java.io.ByteArrayOutputStream

import com.fasterxml.jackson.core.{JsonEncoding, JsonFactory}

/**
  * Created by jadler on 1/13/16.
  */
class JSONConstructor extends Object with Constructor {
  val jsonFactory = new JsonFactory()
  jsonFactory.setRootValueSeparator("")
  val buffer = new ByteArrayOutputStream()
  val generator = jsonFactory.createGenerator(buffer, JsonEncoding.UTF8)

  def setTs(n: Long) = generator.writeNumberField("ts", n)

  def setUserId(n: Long) = generator.writeNumberField("userId", n)

  def setSessionId(n: Long) = generator.writeNumberField("sessionId", n)

  def setLevel(s: String) = generator.writeStringField("level", s)

  def setItemInSession(i: Int) = generator.writeNumberField("itemInSession", i)

  def setUserDetails(m: Map[String, Any]) =
    m.foreach((p: (String, Any)) => {
      p._2 match {
        case _: Long => generator.writeNumberField(p._1, p._2.asInstanceOf[Long])
        case _: Int => generator.writeNumberField(p._1, p._2.asInstanceOf[Int])
        case _: Double => generator.writeNumberField(p._1, p._2.asInstanceOf[Double])
        case _: Float => generator.writeNumberField(p._1, p._2.asInstanceOf[Float])
        case _: String => generator.writeStringField(p._1, p._2.asInstanceOf[String])
      }
    })

  def setDeviceDetails(m: Map[String, Any]) =
    m.foreach((p: (String, Any)) => {
      p._2 match {
        case _: Long => generator.writeNumberField(p._1, p._2.asInstanceOf[Long])
        case _: Int => generator.writeNumberField(p._1, p._2.asInstanceOf[Int])
        case _: Double => generator.writeNumberField(p._1, p._2.asInstanceOf[Double])
        case _: Float => generator.writeNumberField(p._1, p._2.asInstanceOf[Float])
        case _: String => generator.writeStringField(p._1, p._2.asInstanceOf[String])
      }
    })

  def setTag(s: String) = generator.writeStringField("tag", s)

  def start() = generator.writeStartObject()

  def end() = {
    generator.writeEndObject()
    generator.writeRaw('\n')
    generator.flush()
    val ba = buffer.toByteArray
    buffer.reset()
    ba
  }
}
