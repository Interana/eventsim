package com.interana.eventsim.events.Listen


trait Constructor extends com.interana.eventsim.events.Constructor {
  def setAuth(s: String)
  def setArtist(s: String)
  def setTitle(s: String)
  def setDuration(d: Float)
}
