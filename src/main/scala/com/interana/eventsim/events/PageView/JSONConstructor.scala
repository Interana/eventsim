package com.interana.eventsim.events.PageView

class JSONConstructor() extends com.interana.eventsim.events.JSONConstructor with Constructor {
  def setPage(s: String) = generator.writeStringField("page",s)
  def setAuth(s: String) = generator.writeStringField("auth", s)
  def setMethod(s: String) = generator.writeStringField("method", s)
  def setStatus(i: Int) = generator.writeNumberField("status", i)
  def setArtist(s: String) = generator.writeStringField("artist", s)
  def setTitle(s: String) = generator.writeStringField("song", s)
  def setDuration(f: Float) = generator.writeNumberField("duration", f)
}
