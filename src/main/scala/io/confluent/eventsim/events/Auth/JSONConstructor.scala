package io.confluent.eventsim.events.Auth

class JSONConstructor() extends io.confluent.eventsim.events.JSONConstructor with Constructor {
  def setSuccess(boolean: Boolean) = generator.writeBooleanField("success", boolean)
}
