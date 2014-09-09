package com.interana.eventsim

import org.apache.commons.math3.random.RandomGenerator

import scala.collection.mutable
import scala.io.Source

/**
 * Created by jadler on 8/29/14.
 *
 * Used in the Markov Process; models a single state and transitions to other states
 *
 */

class State(val name: String) {
  var transitions: Map[State, Tuple2[Double,Double]] = scala.collection.immutable.ListMap()

  def maxP = if (transitions.nonEmpty) transitions.values.map(_._2).max else 0.0

  def addTransition(s: State, p: Double) = {
    val oldMax = this.maxP
    if (oldMax + p > 1.0)
      throw new Exception(
        "Adding a transition from " + s.name +
          " to " + name + " with probability " + p +
          " would make the total transition probability greater than 1")
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

  override def toString = name
  /* "name: "  +  name + ", transitions: " +
     transitions.foldLeft("")( (s:String, t:(Double, State)) =>
     (if (s != "") {s + ", "} else {""}) + t._2.name + ": " + t._1.toString)
   */
}

object State {
  def stateFileLoader(fn: String): State = {
    // A state file consists of a set of weighted edges.
    // Each line contains <source>,<dest>,<probability>. Specify
    // node names without quotes.
    // To specify the probability that a session starts at a node,
    // use the special source * (example: "*,a,1.0" means "start at a")
    // Empty lines and lines beginning with # are ignored.
    // Transitions from a node to itself must be explicitly added.
    // If the combined probabilities for a node are p <  1.0,
    // then for that node the probability that the session terminates
    // is (1 - p).
    // Returns set of initial states.

    val s = Source.fromFile(fn)
    val lines = s.getLines()
    // val states = new mutable.HashMap[String, mutable.HashMap[String, Double]]()
    val states = new mutable.HashMap[String, State]
    for (l <- lines) {
      if (l.length() > 0 && !l.startsWith("#")) {
        val fields = l.split(",")
        val source = fields(0)
        val dest = fields(1)
        val p = fields(2).toDouble
        assert(dest != "*")
        if (!states.contains(source)) {
          states += (source -> new State(source))
        }
        if (!states.contains(dest)) {
          states += (dest -> new State(dest))
        }
        states(source).addTransition(states(dest),p)
      }
    }
    s.close()
    val initialState = states("*")
    assert(initialState.maxP == 1.0)
    states("*")
  }
}
