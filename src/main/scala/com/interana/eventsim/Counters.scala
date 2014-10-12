package com.interana.eventsim

object Counters {
  // some global counters
  private var sessionId = 0L
  private var userId: Int = Main.Conf.firstUserId.get.get

  def nextSessionId = {
    sessionId = sessionId + 1
    sessionId
  }

  def nextUserId = {
    userId = userId + 1
    userId
  }
}