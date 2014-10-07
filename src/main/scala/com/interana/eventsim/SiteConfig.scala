package com.interana.eventsim

import scala.collection.mutable
import scala.io.Source

/**
 *  Site configuration (loaded from JSON file, used to run simulation)
 */

object SiteConfig {

  val initialState = new State("NEW_SESSION","INITIAL_STATUS",200,"")
  val newUserState = new State("NEW_USER","INITIAL_STATUS",200,"")
  val showUserWithState = new mutable.HashMap[String, Boolean]()

  // optional config values
  var alpha:Double = 60.0
  var beta:Double  = Constants.SECONDS_PER_DAY * 3
  var damping:Double = Constants.DEFAULT_DAMPING
  var weekendDamping:Double = Constants.DEFAULT_WEEKEND_DAMPING
  var weekendDampingOffset:Int = Constants.DEFAULT_WEEKEND_DAMPING_OFFSET
  var weekendDampingScale:Int = Constants.DEFAULT_WEEKEND_DAMPING_SCALE
  var sessionGap:Int = Constants.DEFAULT_SESSION_GAP
  var churnedState:Option[String] = None
  var seed = 0L

  // tags for JSON config file
  val TRANSITIONS = "transitions"
  val NEW_SESSION = "new-session"
  val NEW_USER = "new-user"
  val CHURNED_STATE = "churned-state"
  val SHOW_USER_DETAILS = "show-user-details"
  val PAGE = "page"
  val STATE = "state"
  val SHOW = "show"
  val SOURCE = "source"
  val DEST = "dest"
  val P = "p"
  val STATUS = "status"
  val METHOD = "method"
  val SEED = "seed"
  val SESSION_GAP = "session-gap"

  val ALPHA = "alpha"
  val BETA = "beta"
  val DAMPING = "damping"
  val WEEKEND_DAMPING = "weekend-damping"
  val WEEKEND_DAMPING_OFFSET = "weekend-damping-offset"
  val WEEKEND_DAMPING_SCALE = "weekend-damping-scale"


  def configFileLoader(fn: String) = {

    // simple JSON based state file format (to maximize readability)

    val s = Source.fromFile(fn)
    val rawContents = s.mkString
    val jsonContents = (scala.util.parsing.json.JSON.parseFull(rawContents) match {
      case e: Some[Any] => e.get
      case _ => throw new Exception("Could not parse the state file")
    }).asInstanceOf[Map[String,Any]]
    s.close()

    jsonContents.get(ALPHA) match {
      case x: Some[Any] => alpha = x.get.asInstanceOf[Double]
      case None =>
    }

    jsonContents.get(BETA) match {
      case x: Some[Any] => beta = x.get.asInstanceOf[Double]
      case None =>
    }

    jsonContents.get(DAMPING) match {
      case x: Some[Any] => damping = x.get.asInstanceOf[Double]
      case None =>
    }

    jsonContents.get(WEEKEND_DAMPING) match {
      case x: Some[Any] => weekendDamping = x.get.asInstanceOf[Double]
      case None =>
    }

    jsonContents.get(WEEKEND_DAMPING_OFFSET) match {
        // in minutes
      case x: Some[Any] => weekendDampingOffset = x.get.asInstanceOf[Double].toInt
      case None =>
    }

    jsonContents.get(WEEKEND_DAMPING_SCALE) match {
        // in minutes
      case x: Some[Any] => weekendDampingScale = x.get.asInstanceOf[Double].toInt
      case None =>
    }

    jsonContents.get(SEED) match {
      case x: Some[Any] => seed = x.get.asInstanceOf[Double].toLong
      case None =>
    }

    jsonContents.get(SESSION_GAP) match {
      case x: Some[Any] => sessionGap = x.get.asInstanceOf[Double].toInt
      case None =>
    }

    churnedState = jsonContents.get(CHURNED_STATE).asInstanceOf[Option[String]]

    //jsonContents.get(CHURNED_STATE) match {
    //  case x: Some[Any] => churnedState = Some(x.get.asInstanceOf[String])
    //  case None =>
    //}

    val states = new mutable.HashMap[(String,String,Int,String), State]

    val transitions = jsonContents.getOrElse(TRANSITIONS,List()).asInstanceOf[List[Any]]
    for (t <- transitions) {
      val transition = t.asInstanceOf[Map[String,Any]]

      val source = transition.getOrElse(SOURCE,List()).asInstanceOf[Map[String,Any]]
      val sourcePage = source.get(PAGE).get.asInstanceOf[String]
      val sourceState = source.get(STATE).get.asInstanceOf[String]
      val sourceStatus = source.getOrElse(STATUS,Unit).asInstanceOf[Double].toInt
      val sourceMethod = source.getOrElse(METHOD,Unit).asInstanceOf[String]

      val dest = transition.getOrElse(DEST,List()).asInstanceOf[Map[String,Any]]
      val destPage     = dest.get(PAGE).get.asInstanceOf[String]
      val destState   = dest.get(STATE).get.asInstanceOf[String]
      val destStatus = dest.getOrElse(STATUS,Unit).asInstanceOf[Double].toInt
      val destMethod = dest.getOrElse(METHOD,Unit).asInstanceOf[String]

      val p      = transition.getOrElse(P,Unit).asInstanceOf[Double]

      if (!states.contains((sourcePage, sourceState, sourceStatus, sourceMethod))) {
        states += ((sourcePage, sourceState, sourceStatus, sourceMethod) ->
          new State(sourcePage, sourceState,sourceStatus,sourceMethod))
      }
      if (!states.contains((destPage,destState,destStatus,destMethod))) {
        states += ((destPage,destState,destStatus,destMethod) ->
          new State(destPage,destState,destStatus,destMethod))
      }
      states((sourcePage, sourceState, sourceStatus, sourceMethod))
        .addTransition(states((destPage,destState,destStatus,destMethod)),p)
    }

    val initial = jsonContents.getOrElse(NEW_SESSION,List()).asInstanceOf[List[Any]]
    for (i <- initial) {
      val item = i.asInstanceOf[Map[String,Any]]
      val page = item.get(PAGE).get.asInstanceOf[String]
      val state = item.get(STATE).get.asInstanceOf[String]
      val method = item.get(METHOD).get.asInstanceOf[String]
      val status = item.get(STATUS).get.asInstanceOf[Double].toInt
      val p     = item.get(P).get.asInstanceOf[Double]
      initialState.addTransition(states.get((page,state,status,method)).get, p)
    }

    if (initialState.maxP < 1.0)
      throw new Exception("invalid initial session states (total probability < 1.0)")

    val newUser = jsonContents.getOrElse(NEW_USER,List()).asInstanceOf[List[Any]]
    for (i <- newUser) {
      val item = i.asInstanceOf[Map[String,Any]]
      val page = item.get(PAGE).get.asInstanceOf[String]
      val state = item.get(STATE).get.asInstanceOf[String]
      val method = item.get(METHOD).get.asInstanceOf[String]
      val status = item.get(STATUS).get.asInstanceOf[Double].toInt
      val p     = item.get(P).get.asInstanceOf[Double]
      newUserState.addTransition(states.get((page,state,status,method)).get, p)
    }

    if (newUserState.maxP < 1.0)
      throw new Exception("invalid new user states (total probability < 1.0)")

    val showUserDetails = jsonContents.getOrElse(SHOW_USER_DETAILS,List()).asInstanceOf[List[Any]]
    for (i <- showUserDetails) {
      val item = i.asInstanceOf[Map[String,Any]]
      val state = item.get(STATE).get.asInstanceOf[String]
      val show     = item.get(SHOW).get.asInstanceOf[Boolean]
      showUserWithState += (state -> show)
    }

  }

}
