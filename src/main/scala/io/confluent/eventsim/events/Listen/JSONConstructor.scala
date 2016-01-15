package io.confluent.eventsim.events.Listen

class JSONConstructor() extends io.confluent.eventsim.events.JSONConstructor with Constructor {
  def setAuth(s: String) = generator.writeStringField("auth", s)

  def setArtist(s: String) = generator.writeStringField("artist", s)

  def setTitle(s: String) = generator.writeStringField("song", s)

  def setDuration(f: Float) = generator.writeNumberField("duration", f)
}
