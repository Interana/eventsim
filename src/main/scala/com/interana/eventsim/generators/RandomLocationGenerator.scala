package com.interana.eventsim.generators

import com.interana.eventsim.WeightedRandomThingGenerator

import scala.io.Source

/**
 * Created by jadler on 9/2/14.
 */
class RandomLocationGenerator extends WeightedRandomThingGenerator[String] {

  val s = Source.fromFile("data/CBSA-EST2013-alldata.csv","ISO-8859-1")
  val lines = s.getLines
  val cbsaRegex = new scala.util.matching.Regex(
    """\d+\,[^\,]*\,[^\,]*\,\"([^\"]+)\"\,M(?:et|ic)ropolitan\ Statistical\ Area\,(\d+)\,.*""",
    "name", "pop")
  val fields = for {l <- lines; m <- cbsaRegex findFirstMatchIn l}
    yield (m.group("name"), m.group("pop").toInt.asInstanceOf[Integer])
  fields.foreach(this.add(_))
  s.close

}
