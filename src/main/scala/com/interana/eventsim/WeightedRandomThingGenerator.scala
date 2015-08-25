package com.interana.eventsim

import scala.collection.mutable.ArrayBuffer

/**
 * Class to randomly return a thing from a (weighted) list of things
 */

class WeightedRandomThingGenerator[T]  {
  val ab = new ArrayBuffer[(T, Integer)](0)
  var a = new Array[(T, Integer)](0)
  var ready = false
  var totalWeight: Integer = 0

  def add(t: (T, Integer)): Unit = {
    if (ready)
      throw new RuntimeException("called WeightedRandomThingGenerator.add after use")
    ab += ((t._1, totalWeight))
    totalWeight = totalWeight + t._2
  }

  def add(thing: T, weight: Integer): Unit = add((thing, weight))

  object tupleSecondValueOrdering extends Ordering[(T, Integer)] {
    override def compare(x: (T, Integer), y: (T, Integer)): Int = x._2.compareTo(y._2)
  }

  def randomThing = {
    if (!ready) {
      a = ab.toArray
      ready = true
    }
    val key: (T, Integer) = (null, TimeUtilities.rng.nextInt(totalWeight)).asInstanceOf[(T,Integer)]
    val idx = java.util.Arrays.binarySearch(a, key, tupleSecondValueOrdering)
    if (idx >= 0) a(idx)._1 else a(-idx - 2)._1
  }

  def mkString =
    a.take(5).foldLeft("First 5 items:\n")((s:String,t:(T,Integer)) => s + "\t" + t.toString() + "\n")

}
