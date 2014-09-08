package com.interana.eventsim.generators

import com.interana.eventsim.WeightedRandomThingGenerator

import scala.io.Source

/**
 * Created by jadler on 9/4/14.
 * Data originally from http://www.census.gov/genealogy/www/data/2000surnames/index.html
 */
class RandomLastNameGenerator extends WeightedRandomThingGenerator[String] {

  val s = Source.fromFile("data/Top1000Surnames.csv","ISO-8859-1")
  val lines = s.getLines.drop(1)
  for (l <- lines) {
    val fields = l.split(",")
    this.add(fields(0).toLowerCase.capitalize, fields(2).toInt)
  }
  s.close

}
