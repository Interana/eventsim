package io.confluent.eventsim.buildin

import io.confluent.eventsim.WeightedRandomThingGenerator

import scala.io.Source

/**
  * Randomly generates locations
  *
  * Population data and coordinated are from http://www.census.gov/geo/maps-data/data/gazetteer2010.html
  * Place names are from http://download.geonames.org/export/zip/
  *
  * country code      : iso country code, 2 characters
  * postal code       : varchar(20)
  * place name        : varchar(180)
  * admin name1       : 1. order subdivision (state) varchar(100)
  * admin code1       : 1. order subdivision (state) varchar(20)
  * admin name2       : 2. order subdivision (county/province) varchar(100)
  * admin code2       : 2. order subdivision (county/province) varchar(20)
  * admin name3       : 3. order subdivision (community) varchar(100)
  * admin code3       : 3. order subdivision (community) varchar(20)
  * latitude          : estimated latitude (wgs84)
  * longitude         : estimated longitude (wgs84)
  * accuracy          : accuracy of lat/lng from 1=estimated to 6=centroid
  *
  */

object RandomLocationGenerator extends WeightedRandomThingGenerator[(String, String, String, Double, Double)] {

  val statsSource = Source.fromFile("data/Gaz_zcta_national.txt", "ISO-8859-1")
  val namesSource = Source.fromFile("data/US.txt", "ISO-8859-1")

  val statsLines = statsSource.getLines().drop(1)
  val namesLines = namesSource.getLines().toList

  val nameMap: Map[String, (String, String)] = namesLines.map(
    l => {
      val e = l.split('\t')
      val zip = e(1)
      val city = e(2)
      val state = e(4)
      zip ->(city, state)
    })(collection.breakOut)

  statsLines.foreach(s => {
    val e = s.split('\t')
    val zip = e(0)
    val pop = e(1).toInt
    val lat = e(7).toDouble
    val lon = e(8).toDouble
    if (nameMap.contains(zip)) {
      val (city, state) = nameMap(zip)
      this.add((zip, city, state, lat, lon), pop)
    }
  }
  )

  statsSource.close()
  namesSource.close()

}
