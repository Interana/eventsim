package com.interana.eventsim.buildin

import com.interana.eventsim.WeightedRandomThingGenerator

import scala.io.Source

/**
 * Created by jadler on 10/7/14.
 */
class RandomSongGenerator extends WeightedRandomThingGenerator[String] {

  val s = Source.fromFile("data/train_triplets.txt","ISO-8859-1")
  val lines = s.getLines()
  val counts = new scala.collection.mutable.HashMap[String,Int]()
  for (l <- lines) {
    val fields = l.split(",")
    val song = fields(1)
    val count = fields(2).toInt
    counts.put(song, counts.getOrElse(song,0) + count)
  }
  s.close()
  counts.foreach((p:(String,Int)) => this.add(p._1,p._2))

}
