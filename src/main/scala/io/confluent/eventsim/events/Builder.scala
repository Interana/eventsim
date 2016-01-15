package io.confluent.eventsim.events

import java.lang

import org.apache.avro.specific.SpecificRecord

/**
  * Created by jadler on 1/13/16.
  */
trait Builder {
  // list of common methods used in avro constructors

  def setTs(value: Long): Builder

  def setUserId(value: lang.Long): Builder

  def setSessionId(value: Long): Builder

  def setLevel(value: CharSequence): Builder

  def setItemInSession(value: Int): Builder

  def setUserDetails(value: Map[CharSequence, AnyRef]): Builder

  def setDeviceDetails(value: Map[CharSequence, AnyRef]): Builder

  def setTag(value: CharSequence): Builder

  def build(): SpecificRecord

}
