package com.interana.eventsim.events.StatusChange

class JSONConstructor() extends com.interana.eventsim.events.JSONConstructor with Constructor {
  def setMethod(s: String) = generator.writeStringField("method", s)
  def setAuth(s: String) = generator.writeStringField("auth", s)
}
