package com.interana.eventsim.events.StatusChange


import com.interana.eventsim.events.Builder
import io.confluent.eventsim.StatusChange

class AvroConstructor() extends com.interana.eventsim.events.AvroConstructor with Constructor {
  override var eventBuilder = StatusChange.newBuilder().asInstanceOf[Builder]
  def setAuth(s: String) = eventBuilder.asInstanceOf[StatusChange].setAuth(s)
  def setMethod(s: String) = eventBuilder.asInstanceOf[StatusChange].setMethod(s)
  def start() = {
    eventBuilder = StatusChange.newBuilder(eventBuilder.asInstanceOf[StatusChange]).asInstanceOf[Builder]
  }

}
