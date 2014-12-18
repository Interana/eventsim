package com.interana.similarparser

import java.io.{File, PrintWriter}
import java.util.ArrayList

import com.fasterxml.jackson.core.{JsonFactory, JsonParser, TreeNode}

/**
 * Created by jadler on 10/10/14.
 */
object Main extends App {

  //val path = args(0)
  val root1 = new File("data/lastfm_train")
  val root2 = new File("data/lastfm_test")
  val out = new PrintWriter("../../data/similar_songs.csv")

  def processDirectory(file: java.io.File): Unit = {
    for (f <- file.listFiles()) {
      if (f.isDirectory)
        processDirectory(f)
      else {
        processFile(f)
      }
    }
  }

  val jsonFactory = new JsonFactory(new com.fasterxml.jackson.databind.ObjectMapper())

  // val similarMap = new scala.collection.mutable.HashMap[String, ArrayList[String]]()

  def unquote(s: String): String = s.substring(1,s.length()-1)

  def processFile(file: java.io.File) = {
    val parser: JsonParser = jsonFactory.createParser(file)
    val tree: TreeNode  = parser.readValueAsTree()
    val trackId = tree.get("track_id").toString()
    //println("trackId: " + trackId)
    val similars = tree.get("similars")
    val similarSet = new ArrayList[String]()
    for (i <- 0 until similars.size()) {
      val item = similars.get(i)
      val tid = item.get(0)
      //similarSet.add(tid.toString())
      out.println(unquote(trackId) +","+unquote(tid.toString()))
    }
    //similarMap.put(trackId,similarSet)

  }

  processDirectory(root1)
  processDirectory(root2)
  out.close()
}
