package com.interana.eventsim.buildin

import com.interana.eventsim.WeightedRandomThingGenerator

import scala.io.Source

/**
 * Data from http://techblog.willshouse.com/2012/01/03/most-common-user-agents/
 */
object RandomUserAgentGenerator extends WeightedRandomThingGenerator[(String,String,String)] {

  val s = Source.fromFile("data/user agents.txt", "UTF-16" /*, "ISO-8859-1" */)
  val lines = s.getLines().drop(1)
  for (l <- lines) {
    val fields = l.split("\t")
    this.add((fields(2),fields(3),fields(4)), fields(1).trim.toInt)
  }
  s.close()

}
