package com.interana.eventsim

/**
 * Created by jadler on 9/2/14.
 * Class to randomly return a weighted thing from a list of things
 */
class WeightedRandomThingGenerator[T]  {
  var a = new Array[(T, Integer)](0)
  var totalWeight: Integer = 0

  def add(t: (T, Integer)): Unit = {
    a = a :+ (t._1, totalWeight)
    totalWeight = totalWeight + t._2
  }

  def add(thing: T, weight: Integer): Unit = add((thing, weight))

  object tupleSecondValueOrdering extends Ordering[(T, Integer)] {
    override def compare(x: (T, Integer), y: (T, Integer)): Int = x._2.compareTo(y._2)
  }

  def randomThing: T = {
    val key: (T, Integer) = (null, TimeUtilities.rng.nextInt(totalWeight)).asInstanceOf[(T,Integer)]
    val idx = java.util.Arrays.binarySearch(a, key, tupleSecondValueOrdering)
    if (idx >= 0) a(idx)._1 else a(-idx - 2)._1
  }

  def mkString =
    a.take(5).foldLeft("First 5 items:\n")((s:String,t:(T,Integer)) => s + "\t" + t.toString() + "\n")

}
