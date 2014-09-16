package com.interana.eventsim

import scala.collection.mutable
import scala.io.Source

/**
 * Created by jadler on 9/16/14.
 */

object SiteConfig {

  val initialState = new State("NEW_SESSION","INITIAL_STATUS")
  val newUserState = new State("NEW_USER","INITIAL_STATUS")
  val showUserWithStatus = new mutable.HashMap[String, Boolean]()

  // optional config values
  var alpha:Double = 60000.0
  var beta:Double  = Constants.MILLISECONDS_PER_DAY * 3
  var damping:Double = Constants.DEFAULT_DAMPING

  // tags for JSON config file
  val TRANSITIONS = "transitions"
  val NEW_SESSION = "new-session"
  val NEW_USER = "new-user"
  val SHOW_USER_DETAILS = "show-user-details"
  val PAGE = "page"
  val STATUS = "status"
  val SHOW = "show"
  val SOURCE_PAGE = "sourcePage"
  val SOURCE_STATUS = "sourceStatus"
  val DEST_PAGE = "destPage"
  val DEST_STATUS = "destStatus"
  val P = "p"

  val ALPHA = "alpha"
  val BETA = "beta"
  val DAMPING = "damping"

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

    val states = new mutable.HashMap[(String,String), State]

    val transitions = jsonContents.get(TRANSITIONS).getOrElse(List()).asInstanceOf[List[Any]]
    for (t <- transitions) {
      val transition = t.asInstanceOf[Map[String,Any]]
      val sourcePage = transition.get(SOURCE_PAGE).get.asInstanceOf[String]
      val sourceStatus = transition.get(SOURCE_STATUS).get.asInstanceOf[String]
      val destPage   = transition.get(DEST_PAGE).get.asInstanceOf[String]
      val destStatus   = transition.get(DEST_STATUS).get.asInstanceOf[String]
      val p      = transition.get(P).get.asInstanceOf[Double]
      if (!states.contains((sourcePage, sourceStatus))) {
        states += ((sourcePage, sourceStatus) -> new State(sourcePage, sourceStatus))
      }
      if (!states.contains((destPage,destStatus))) {
        states += ((destPage,destStatus) -> new State(destPage,destStatus))
      }
      states((sourcePage, sourceStatus)).addTransition(states((destPage,destStatus)),p)
    }

    val initial = jsonContents.get(NEW_SESSION).getOrElse(List()).asInstanceOf[List[Any]]
    for (i <- initial) {
      val item = i.asInstanceOf[Map[String,Any]]
      val page = item.get(PAGE).get.asInstanceOf[String]
      val status = item.get(STATUS).get.asInstanceOf[String]
      val p     = item.get(P).get.asInstanceOf[Double]
      initialState.addTransition(states.get((page,status)).get, p)
    }

    if (initialState.maxP < 1.0)
      throw new Exception("invalid initial session states (total probability < 1.0)")


    val newUser = jsonContents.get(NEW_USER).getOrElse(List()).asInstanceOf[List[Any]]
    for (i <- newUser) {
      val item = i.asInstanceOf[Map[String,Any]]
      val page = item.get(PAGE).get.asInstanceOf[String]
      val status = item.get(STATUS).get.asInstanceOf[String]
      val p     = item.get(P).get.asInstanceOf[Double]
      newUserState.addTransition(states.get((page,status)).get, p)
    }

    if (newUserState.maxP < 1.0)
      throw new Exception("invalid new user states (total probability < 1.0)")


    val showUserDetails = jsonContents.get(SHOW_USER_DETAILS).getOrElse(List()).asInstanceOf[List[Any]]
    for (i <- showUserDetails) {
      val item = i.asInstanceOf[Map[String,Any]]
      val status = item.get(STATUS).get.asInstanceOf[String]
      val show     = item.get(SHOW).get.asInstanceOf[Boolean]
      showUserWithStatus += (status -> show)
    }

  }

}
