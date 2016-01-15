package io.confluent.eventsim.events.StatusChange

import io.confluent.eventsim.avro.StatusChange

import scala.collection.JavaConversions._

class AvroConstructor() extends io.confluent.eventsim.events.AvroConstructor[StatusChange] with Constructor {

  def schema = StatusChange.getClassSchema

  var eventBuilder = StatusChange.newBuilder()

  def setTs(n: Long) = eventBuilder.setTs(n)

  def setUserId(n: Long) = eventBuilder.setUserId(n)

  def setSessionId(n: Long) = eventBuilder.setSessionId(n)

  def setAuth(s: String) = eventBuilder.setAuth(s)

  def setLevel(s: String) = eventBuilder.setLevel(s)

  def setItemInSession(i: Int) = eventBuilder.setItemInSession(i)

  def setUserDetails(m: Map[String, Any]): Unit =
    eventBuilder.setUserDetails(m.asInstanceOf[Map[CharSequence, AnyRef]])

  // eventBuilder.setUserDetails(m.asInstanceOf[java.util.Map[CharSequence,AnyRef]])
  def setDeviceDetails(m: Map[String, Any]): Unit =
    eventBuilder.setDeviceDetails(m.asInstanceOf[Map[CharSequence, AnyRef]])

  // eventBuilder.setDeviceDetails(m.asInstanceOf[java.util.Map[CharSequence,AnyRef]])
  def setTag(s: String) = eventBuilder.setTag(s)

  def start() = {
    eventBuilder = StatusChange.newBuilder(eventBuilder)
  }

  def end() = eventBuilder.build()


}
