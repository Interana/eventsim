package io.confluent.eventsim.events.StatusChange

class JSONConstructor() extends io.confluent.eventsim.events.JSONConstructor with Constructor {
  def setAuth(s: String) = generator.writeStringField("auth", s)
}
