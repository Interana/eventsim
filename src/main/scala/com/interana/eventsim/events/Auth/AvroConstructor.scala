package com.interana.eventsim.events.Auth

import com.interana.eventsim.events.Builder
import io.confluent.eventsim.Auth

class AvroConstructor() extends com.interana.eventsim.events.AvroConstructor with Constructor {
  override var eventBuilder = Auth.newBuilder().asInstanceOf[Builder]
  def setSuccess(boolean: Boolean) = eventBuilder.asInstanceOf[Auth.Builder].setSuccess(boolean)
  def start() = {
    eventBuilder = Auth.newBuilder(eventBuilder.asInstanceOf[Auth.Builder]).asInstanceOf[Builder]
  }

}
