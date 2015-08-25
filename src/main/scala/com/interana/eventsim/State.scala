package com.interana.eventsim

import org.apache.commons.math3.random.RandomGenerator

/**
 * Models a single state and transitions to other states
 */

class State(val t:(String,String,Int,String,String)) {
  val page = t._1
  val auth = t._2
  val status = t._3
  val method = t._4
  val level = t._5
  var laterals: Map[State, (Double,Double)] = scala.collection.immutable.ListMap()
  var upgrades: Map[State, (Double,Double)] = scala.collection.immutable.ListMap()
  var downgrades: Map[State, (Double,Double)] = scala.collection.immutable.ListMap()

  private def maxP(transitions: Map[State, (Double,Double)]) =
    if (transitions.nonEmpty) transitions.values.map(_._2).max else 0.0

  def maxLateralsP = maxP(laterals)

  private def addTransition(s: State, p: Double, t: Map[State, (Double,Double)]) = {
    val oldMax = this.maxP(t)
    if (oldMax + p > 1.0) {
      println(this.toString)
      throw new Exception(
        "Adding a transition from " + s.page + "," + s.auth +
          " to " + page + "," + s.auth + " with probability " + p +
          " would make the total transition probability greater than 1")
    }
    val newKey = (oldMax, oldMax + p)
    t.+(s -> newKey)
  }

  def addLateral(s: State, p: Double) = {laterals = addTransition(s,p,laterals)}
  def addUpgrade(s: State, p: Double) = {laterals = addTransition(s,p,upgrades)}
  def addDowngrade(s: State, p: Double) = {laterals = addTransition(s,p,downgrades)}

  private def inRange(v: Double, s:(State,(Double,Double))) = v >= s._2._1 && v < s._2._2

  def nextState(rng: RandomGenerator) = {
    val x = rng.nextDouble()
    val r = laterals.find(inRange(x,_))
    if (r.nonEmpty)
      Some(r.get._1)
    else
      None
  }

  def upgrade(rng: RandomGenerator) = {
    val x = rng.nextDouble()
    val r = upgrades.find(inRange(x,_))
    if (r.nonEmpty)
      Some(r.get._1)
    else
      None
  }

  def downgrade(rng: RandomGenerator) = {
    val x = rng.nextDouble()
    val r = downgrades.find(inRange(x,_))
    if (r.nonEmpty)
      Some(r.get._1)
    else
      None
  }

  override def toString =
    "page: "  +  page + ",  auth: " + auth + ", transitions: " +
     laterals.foldLeft("")( (s:String, t:(State, (Double, Double))) =>
     (if (s != "") {s + ", "} else {""}) + t._1.page + "," + t._1.auth + ": " + t._2.toString)

}