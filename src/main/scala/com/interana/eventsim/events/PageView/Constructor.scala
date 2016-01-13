package com.interana.eventsim.events.PageView

trait Constructor extends com.interana.eventsim.events.Constructor {
  def setPage(s: String)
  def setAuth(s: String)
  def setMethod(s: String)
  def setStatus(i: Int)
  def setArtist(s: String)
  def setTitle(s: String)
  def setDuration(d: Float)
}
