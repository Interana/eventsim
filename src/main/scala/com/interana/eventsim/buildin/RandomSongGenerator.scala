package com.interana.eventsim.buildin

import java.io.FileInputStream
import java.util.zip.GZIPInputStream

import com.interana.eventsim.WeightedRandomThingGenerator

import scala.collection.mutable
import scala.io.Source

object RandomSongGenerator extends WeightedRandomThingGenerator[String] {
  System.err.println("Loading song file...")
  // val s = Source.fromFile("data/listen_counts.txt","ISO-8859-1")
  val fis = new FileInputStream("data/listen_counts.txt.gz")
  val gis = new GZIPInputStream(fis)
  val s = Source.fromInputStream(gis,"ISO-8859-1")

  val listenLines = s.getLines()

  val trackIdMap = new mutable.HashMap[String,(String,String,Double,Int)]()
  var i = 0
  for (ll <- listenLines) {
    if ((i % 1000) == 0)
      System.err.print("\r" + i)
    i +=1
    try {
      val fields = ll.split("\t")
      val trackId = fields(0)
      val artist = fields(1)
      val songName = fields(2)
      val duration = {
        val d = fields(3)
        if (d != "") d.toDouble else 180.0
      }
      val count = fields(4).toInt
      trackIdMap.put(trackId,(artist,songName,duration,count))
      this.add(trackId, count)
    } catch {
      case e: NumberFormatException => {
        println("\n" + ll + "\n")
        throw e
      }
    }
  }
  System.err.println("\t...done loading song file. " + trackIdMap.size + " tracks loaded.")
  s.close()

  System.err.println("Loading similar song file...")
  val similarSongs = new mutable.HashMap[String, WeightedRandomThingGenerator[String]]()

  try {
    val ssFis = new FileInputStream("data/similar_songs.csv.gz")
    val ssGis = new GZIPInputStream(ssFis)
    val similarSongSource = Source.fromInputStream(ssGis, "ISO-8859-1")

    //val similarSongSource = Source.fromFile("data/similar_songs.csv","ISO-8859-1")
    val similarSongLines = similarSongSource.getLines()
    i = 0

    for (s <- similarSongLines) {
      if ((i % 1000) == 0)
        System.err.print("\r" + i)
      i += 1
      val fields = s.split(",")
      val trackId = fields(0)
      val similarTrack = fields(1)

      if (trackIdMap.contains(similarTrack)) {
        val metadata = trackIdMap(similarTrack)
        val similars = if (similarSongs.contains(trackId)) similarSongs(trackId)
        else {
          val newArray = new WeightedRandomThingGenerator[String]()
          similarSongs.put(trackId, newArray)
          newArray
        }
        similars.add(similarTrack, metadata._4)
      }
    }

    System.err.println("\t...done loading similar song file")
    System.err.println("\tAvailable for " + similarSongs.size + " songs.")
    similarSongSource.close()
  } catch {
    case e: Exception =>
      System.err.println("Could not load similar song file (don't worry if it's missing)\n")
  }


  def nextSong(lastTrackId: String): (String,String,String,Double) = {
      val nextTrackId =
        if (!similarSongs.isEmpty && similarSongs.contains(lastTrackId)) {
          similarSongs(lastTrackId).randomThing
        } else {
          this.randomThing
        }
      val song = trackIdMap(nextTrackId)
      (nextTrackId,song._1,song._2,song._3)
    }

  def nextSong(): (String, String, String, Double) = nextSong("")

}
