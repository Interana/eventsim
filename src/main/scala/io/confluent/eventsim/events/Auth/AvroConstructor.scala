package io.confluent.eventsim.events.Auth

import io.confluent.eventsim.avro.Auth

import scala.collection.JavaConversions._

class AvroConstructor() extends io.confluent.eventsim.events.AvroConstructor[Auth] with Constructor {

  def schema = Auth.getClassSchema

  var eventBuilder = Auth.newBuilder()

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

  def setSuccess(boolean: Boolean) = eventBuilder.setSuccess(boolean)

  def start() = {
    eventBuilder = Auth.newBuilder(eventBuilder)
  }

  def end() = eventBuilder.build()


}
