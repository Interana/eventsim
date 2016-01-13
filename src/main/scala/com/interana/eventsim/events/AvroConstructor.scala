package com.interana.eventsim.events

/**
  * Created by jadler on 1/13/16.
  */
abstract class AvroConstructor() extends Object with Constructor {
  var eventBuilder: Builder
  def setTs(n: Long) = eventBuilder.setTs(n)
  def setUserId(n: Long) = eventBuilder.setUserId(n)
  def setSessionId(n: Long) = eventBuilder.setSessionId(n)
  def setLevel(s: String) = eventBuilder.setLevel(s)
  def setItemInSession(i: Int) = eventBuilder.setItemInSession(i)
  def setUserDetails(m: Map[String,Any]): Unit =
    eventBuilder.setUserDetails(m.asInstanceOf[java.util.Map[CharSequence,AnyRef]])
  def setDeviceDetails(m: Map[String, Any]): Unit =
    eventBuilder.setDeviceDetails(m.asInstanceOf[java.util.Map[CharSequence,AnyRef]])

  def setTag(s: String) = eventBuilder.setTag(s)

  def start(): Unit

  def end() = {
    eventBuilder.build()
  }

}
