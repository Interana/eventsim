package com.interana.eventsim.buildin

import com.interana.eventsim.WeightedRandomThingGenerator

import scala.io.Source

object RandomFirstNameGenerator extends WeightedRandomThingGenerator[(String,String)] {

  val s = Source.fromFile("data/yob1990.txt","ISO-8859-1")
  val lines = s.getLines()
  for (l <- lines) {
    val fields = l.split(",")
    this.add((fields(0).toLowerCase.capitalize,fields(1)), fields(2).toInt)
  }
  s.close()

}
