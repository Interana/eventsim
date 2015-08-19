package com.interana.eventsim.Utilities

import java.io.{FileOutputStream, PrintWriter, File}
import java.util.ArrayList
import java.util.zip.GZIPOutputStream

import com.fasterxml.jackson.core.{TreeNode, JsonParser, JsonFactory}

/**
 * Created by jadler on 8/19/15.
 */
object SimilarSongParser {


  def compute() = {

    //val path = args(0)
    val root1 = new File("data/lastfm_train")
    val root2 = new File("data/lastfm_test")
    // val out = new PrintWriter("../../data/similar_songs.csv")
    val fileOutputStream = new FileOutputStream("data/similar_songs.csv.gz")
    val gzipOutputStream = new GZIPOutputStream(fileOutputStream)
    val out = new PrintWriter(gzipOutputStream)

    val jsonFactory = new JsonFactory(new com.fasterxml.jackson.databind.ObjectMapper())

    def unquote(s: String): String = s.substring(1, s.length() - 1)

    def processFile(file: java.io.File) = {
      val parser: JsonParser = jsonFactory.createParser(file)
      val tree: TreeNode = parser.readValueAsTree()
      val trackId = tree.get("track_id").toString
      //println("trackId: " + trackId)
      val similars = tree.get("similars")
      val similarSet = new ArrayList[String]()
      for (i <- 0 until similars.size()) {
        val item = similars.get(i)
        val tid = item.get(0)
        out.println(unquote(trackId) + "," + unquote(tid.toString))
      }
    }

    def processDirectory(file: java.io.File): Unit = {
      for (f <- file.listFiles()) {
        if (f.isDirectory)
          processDirectory(f)
        else {
          processFile(f)
        }
      }
    }

    processDirectory(root1)
    processDirectory(root2)
    out.close()
    gzipOutputStream.close()
    fileOutputStream.close()
  }
}
