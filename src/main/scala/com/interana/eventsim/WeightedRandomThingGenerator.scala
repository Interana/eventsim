package com.interana.eventsim

import org.apache.commons.math3.random.{MersenneTwister, RandomGenerator}

/**
 * Created by jadler on 9/2/14.
 * Class to randomly return a weighted thing from a list of things
 */
class WeightedRandomThingGenerator[T]  {
  var a = new Array[Tuple2[T, Integer]](0)
  var totalWeight: Integer = 0

  def add(t: Tuple2[T, Integer]): Unit = {
    a = a :+ (t._1, totalWeight)
    totalWeight = totalWeight + t._2
  }

  def add(thing: T, weight: Integer): Unit = add((thing, weight))

  object tupleSecondValueOrdering extends Ordering[Tuple2[T, Integer]] {
    override def compare(x: (T, Integer), y: (T, Integer)): Int = x._2.compareTo(y._2)
  }

  val rng: RandomGenerator = new MersenneTwister(0L)

  def randomThing: T = {
    val key: Tuple2[T, Integer] = (null, rng.nextInt(totalWeight)).asInstanceOf[Tuple2[T,Integer]]
    val idx = java.util.Arrays.binarySearch(a, key, tupleSecondValueOrdering)
    if (idx >= 0) a(idx)._1 else a(-idx - 2)._1
  }

}
