package com.interana.eventsim.buildin

import com.interana.eventsim.WeightedRandomThingGenerator

import scala.io.Source

class GenericGenerator(fn: String, sep: String, thingColumn: Int, weightColumn: Int, header: Boolean)
  extends WeightedRandomThingGenerator[String] {

  val s = Source.fromFile(fn)
  val lines = s.getLines()
  if (header) lines.drop(1)
  for (l <- lines) {
    val fields = l.split(sep)
    this.add(fields(thingColumn), fields(weightColumn).toInt)
  }
  s.close()
}
