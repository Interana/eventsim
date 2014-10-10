package com.interana.eventsim.buildin

import com.interana.eventsim.WeightedRandomThingGenerator

import scala.io.Source

/**
 * Created by jadler on 10/7/14.
 */
object RandomSongGenerator extends WeightedRandomThingGenerator[(String,String,String,Double)] {
  System.err.println("Loading song file...")
  val s = Source.fromFile("data/listen_counts.txt","ISO-8859-1")
  val listenLines = s.getLines()
  var i = 0
  for (ll <- listenLines) {
    System.err.print("\r" + i)
    i +=1
    try {
      val fields = ll.split("\t")
      val trackId = fields(0)
      val artist = fields(1)
      val songName = fields(2)
      val duration = {
        val d = fields(3); if (d != "") d.toDouble else 180.0
      }
      val count = fields(4).toInt
      this.add((trackId, artist, songName, duration), count)
    } catch {
      case e: NumberFormatException => {
        println("\n" + ll + "\n")
        throw e
      }
    }
  }
  System.err.println("\t...done!\n")
  s.close()

}
