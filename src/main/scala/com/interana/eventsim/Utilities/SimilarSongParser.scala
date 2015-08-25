package com.interana.eventsim.Utilities

import java.io.{FileOutputStream, PrintWriter, File}
import java.net.URL
import java.nio.file.NotDirectoryException
import java.util.ArrayList
import java.util.zip.GZIPOutputStream
import sys.process._

import com.fasterxml.jackson.core.{TreeNode, JsonParser, JsonFactory}

object SimilarSongParser {


  def compute() = {

    /* files from
     * http://labrosa.ee.columbia.edu/millionsong/sites/default/files/lastfm/lastfm_train.zip
     * http://labrosa.ee.columbia.edu/millionsong/sites/default/files/lastfm/lastfm_test.zip
     *
     */

    val rawDataDir = new File("data/raw")
    if (!rawDataDir.exists())
      rawDataDir.mkdirs()
    if (!rawDataDir.isDirectory)
      throw new NotDirectoryException("data/raw exists but is not a directory")

    val trainArchive = new File("data/raw/lastfm_train.zip")
    if (!trainArchive.exists())
      new URL("http://labrosa.ee.columbia.edu/millionsong/sites/default/files/lastfm/lastfm_train.zip") #> trainArchive !!

    val testArchive = new File("data/raw/lastfm_test.zip")
    if (!testArchive.exists())
      new URL("http://labrosa.ee.columbia.edu/millionsong/sites/default/files/lastfm/lastfm_test.zip") #> testArchive !!

    val trainFolder = new File("data/lastfm_train")
    if (!trainFolder.exists()) {
      val result = "unzip " + trainArchive + " -d " + trainFolder !
    }

    val testFolder = new File("data/lastfm_test")
    if (!testFolder.exists()) {
      val result = "unzip " + testArchive + " -d " + testFolder !
    }
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

    processDirectory(trainFolder)
    processDirectory(testFolder)
    out.close()
    gzipOutputStream.close()
    fileOutputStream.close()
  }
}
