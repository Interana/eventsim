package com.interana.eventsim.events.Auth

class JSONConstructor() extends com.interana.eventsim.events.JSONConstructor with Constructor {
  def setSuccess(boolean: Boolean) = generator.writeBooleanField("success", boolean)
}
