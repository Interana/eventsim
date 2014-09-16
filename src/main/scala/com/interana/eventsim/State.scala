package com.interana.eventsim

import org.apache.commons.math3.random.RandomGenerator

/**
 * Created by jadler on 8/29/14.
 *
 * Models a single state and transitions to other states
 *
 */

class State(val page: String, val status: String) {
  var transitions: Map[State, (Double,Double)] = scala.collection.immutable.ListMap()

  def maxP = if (transitions.nonEmpty) transitions.values.map(_._2).max else 0.0

  def addTransition(s: State, p: Double) = {
    val oldMax = this.maxP
    if (oldMax + p > 1.0) {
      println(this.toString)
      throw new Exception(
        "Adding a transition from " + s.page + "," + s.status +
          " to " + page + "," + s.status + " with probability " + p +
          " would make the total transition probability greater than 1")
    }
    val newKey = (oldMax, oldMax + p)
    transitions = transitions.+(s -> newKey)
  }

  private def inRange(v: Double, s:(State,(Double,Double))) = v >= s._2._1 && v < s._2._2

  def nextState(rng: RandomGenerator): Option[State] = {
    val x = rng.nextDouble()
    val r = transitions.find(inRange(x,_))
    if (r.nonEmpty)
      Some(r.get._1)
    else
      None
  }

  override def toString =
    "page: "  +  page + ",  status: " + status + ", transitions: " +
     transitions.foldLeft("")( (s:String, t:(State, (Double, Double))) =>
     (if (s != "") {s + ", "} else {""}) + t._1.page + "," + t._1.status + ": " + t._2.toString)

}