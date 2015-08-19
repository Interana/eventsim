package com.interana.eventsim

import com.interana.eventsim.config.ConfigFromFile

object Counters {
  // some global counters
  private var sessionId = 0L
  private var userId = ConfigFromFile.firstUserId.getOrElse(Main.ConfFromOptions.firstUserId.get.get)

  def nextSessionId = {
    sessionId = sessionId + 1
    sessionId
  }

  def nextUserId = {
    userId = userId + 1
    userId
  }
}