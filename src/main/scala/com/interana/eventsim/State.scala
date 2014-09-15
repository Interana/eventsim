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
  var transitions: Map[State, (Double,Double)] = scala.collection.immutable.ListMap()

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

  override def toString =
  "name: "  +  name + ", transitions: " +
     transitions.foldLeft("")( (s:String, t:(State, (Double, Double))) =>
     (if (s != "") {s + ", "} else {""}) + t._1.name + ": " + t._2.toString)

}

object State {

  def stateFileLoader(fn: String): State = {

    // simple JSON based state file format (to maximize readability)

    val s = Source.fromFile(fn)
    val rawContents = s.mkString
    // println(rawContents)

    val jsonContents = (scala.util.parsing.json.JSON.parseFull(rawContents) match {
      case e: Some[Any] => {
        e.get
      }
      case _ => throw new Exception("Could not parse the state file")
    }).asInstanceOf[Map[String,Any]]
    s.close()

    val states = new mutable.HashMap[String, State]

    val transitions = jsonContents.get("transitions").getOrElse(List()).asInstanceOf[List[Any]]
    for (t <- transitions) {
      val transition = t.asInstanceOf[Map[String,Any]]
      val source = transition.get("source").get.asInstanceOf[String]
      val dest   = transition.get("dest").get.asInstanceOf[String]
      val p      = transition.get("p").get.asInstanceOf[Double]
      if (!states.contains(source)) {
        states += (source -> new State(source))
      }
      if (!states.contains(dest)) {
        states += (dest -> new State(dest))
      }
      states(source).addTransition(states(dest),p)
    }

    val initialState = new State("INITIAL_STATE")
    val initial = jsonContents.get("initial").getOrElse(List()).asInstanceOf[List[Any]]
    for (i <- initial) {
      val transition = i.asInstanceOf[Map[String,Any]]
      val state = transition.get("state").get.asInstanceOf[String]
      val p     = transition.get("p").get.asInstanceOf[Double]
      initialState.addTransition(states.get(state).get, p)
    }

    if (initialState.maxP < 1.0)
      throw new Exception("invalid initial states (total probability < 1.0)")

    initialState

  }

}
