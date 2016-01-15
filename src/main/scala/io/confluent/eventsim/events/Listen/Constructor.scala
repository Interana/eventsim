package io.confluent.eventsim.events.Listen

trait Constructor extends io.confluent.eventsim.events.Constructor {
  def setAuth(s: String)

  def setArtist(s: String)

  def setTitle(s: String)

  def setDuration(d: Float)
}
