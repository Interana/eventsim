package com.interana.eventsim

import org.apache.commons.math3.random.{RandomGenerator}

/**
 * Created by jadler on 8/29/14.
 *
 * Used in the Markov Process; models a single state and transitions to other states
 *
 */

class State(val name: String) {
  var transitions: Map[State, Tuple2[Double,Double]] = scala.collection.immutable.ListMap()

  def addTransition(s: State, p: Double) = {
    val oldMax = if (transitions.nonEmpty) transitions.values.map(_._2).max else 0.0
    if (oldMax + p > 1.0)
      throw new Exception(
        "Adding a transition from " + s.name +
          " to " + name + " with probability " + p +
          " would make the total transition probability greater than 1")
    val newKey = (oldMax, oldMax + p)
    transitions = transitions.+(s -> newKey)
  }

  private def inRange(v: Double, s:(State,(Double,Double))) = v >= s._2._1 && v < s._2._2

  def nextState(rng: RandomGenerator) = {
    val x = rng.nextDouble()
    transitions.find(inRange(x,_)).getOrElse((this,(0.0,0.0)))._1
  }

  override def toString = name
  /* "name: "  +  name + ", transitions: " +
     transitions.foldLeft("")( (s:String, t:(Double, State)) =>
     (if (s != "") {s + ", "} else {""}) + t._2.name + ": " + t._1.toString)
   */

}
