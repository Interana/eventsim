package io.confluent.eventsim.events

/**
  * Created by jadler on 1/13/16.
  */
trait Constructor {
  def setTs(n: Long)

  def setUserId(n: Long)

  def setSessionId(n: Long)

  def setLevel(s: String)

  def setItemInSession(i: Int)

  def setUserDetails(m: Map[String, Any])

  def setDeviceDetails(m: Map[String, Any])

  def setTag(s: String)

  def start()

  def end(): Object
}
