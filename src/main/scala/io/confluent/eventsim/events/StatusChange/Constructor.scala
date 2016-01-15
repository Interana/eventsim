package io.confluent.eventsim.events.StatusChange

trait Constructor extends io.confluent.eventsim.events.Constructor {
  def setAuth(s: String)
}
