package com.interana.eventsim.Utilities

import java.io.PrintWriter

import scala.io.Source

object TrackListenCount  {

  def compute() = {

    // metadata format
    // song analysis (should be 31)
    // 0 1 2 3 4
    // analysisSampleRate,AudioMD5,danceability,duration,endOfFadeIn,
    // 5 6 7 8 9 10
    // energy,idxBarsConfidence,idxBarsStart,idxBeatsConfidence,idxBeatsStart,idxSectionsConfidence,
    // 11 12 13 14 15
    // idxSectionsStart,idxSegmentsConfidence,idxSegmentsLoudnessMax,idxSegmentsLoudnessMaxTime,idxSegmentsLoudnessStart,
    // 16 17 18 19
    // idxSegmentsPitches,idxSegmentsStart,idxSegmentsTimbre,idxTatumsConfidence,
    // 20 21 22 23 24 25 26
    // idxTatumsStart,Key,KeyConfidence,loudness,mode,modeConfidence,startOfFaceOut,
    // 27 28 29 30
    // tempo,timeSignature,timeSignatureConfidence,TrackId

    var counter = 0
    val mdfile = Source.fromFile("data/songs_analysis.txt","ISO-8859-1")
    val mdfileLines = mdfile.getLines()
    val metadata = new scala.collection.mutable.HashMap[String,Double]()
    for (md <- mdfileLines) {
      System.err.print("\r" + counter.toString)
      counter += 1
      val mdFields = md.split("\\s+")
      val trackId = mdFields(30)
      val duration = mdFields(3)
      metadata.put(trackId, duration.toDouble)
    }

    val s = Source.fromFile("data/train_triplets.txt", "ISO-8859-1")
    val lines = s.getLines()
    val counts = new scala.collection.mutable.HashMap[String, Int]()
    for (l <- lines) {
      System.err.print("\r" + counter.toString)
      counter += 1
      val fields = l.split("\t")
      // val userId = fields(0) // not needed
      val song = fields(1)
      val count = fields(2).toInt
      counts.put(song, counts.getOrElse(song, 0) + count)
    }
    s.close()

    // unique tracks format:
    // trackId<SEP>songId<SEP>artistName<SEP>songTitle
    val trackFile = Source.fromFile("data/unique_tracks.txt","ISO-8859-1")
    val trackFileLines = trackFile.getLines()
    val tracks = new scala.collection.mutable.HashMap[String,(String,String,String)]()
    for (t <- trackFileLines) {
      System.err.print("\r" + counter.toString)
      counter += 1
      try {
        val fields = t.split("<SEP>")
        val trackId = fields(0)
        val songId = fields(1)
        val artistName = fields(2)
        val songTitle = fields(3)
        tracks.put(trackId, (songId, artistName, songTitle))
      } catch {
        case e: IndexOutOfBoundsException => {
          // silently forget the record
          // println("while processing" + t)
          // throw e
        }
      }
    }
    trackFile.close()

    val out = new PrintWriter("data/listen_counts.txt")

    tracks.foreach((r:(String,(String,String,String))) => {
      val (trackId,(songId,artist,songName)) = r
      val count = counts.getOrElse(songId,0)
      val duration = metadata(trackId)
      if (count > 0)
        out.println(trackId + "\t" + removeTabs(artist)  + "\t" + removeTabs(songName)  + "\t" + duration  + "\t" + count )
    })

    out.close()
  }

  def removeTabs(s: String): String = {
    s.replaceAll("\t","     ")
  }

}
