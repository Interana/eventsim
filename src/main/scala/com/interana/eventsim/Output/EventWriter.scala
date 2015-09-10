package com.interana.eventsim.Output

trait EventWriter {
  def setTs(n: Long)
  def setUserId(n: Long)
  def setSessionId(n: Long)
  def setPage(s: String)
  def setAuth(s: String)
  def setMethod(s: String)
  def setStatus(i: Int)
  def setLevel(s: String)
  def setItemInSession(i: Int)
  def setArtist(s: String)
  def setTitle(s: String)
  def setDuration(d: Float)
  def setUserDetails(m: Map[String,Any])
  def setTag(s: String)
  def start()
  def end()
}
