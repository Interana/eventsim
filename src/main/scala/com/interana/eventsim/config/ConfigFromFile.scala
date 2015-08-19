package com.interana.eventsim.config

import com.interana.eventsim.{Constants, State, WeightedRandomThingGenerator}

import scala.collection.mutable
import scala.io.Source

/**
 *  Site configuration (loaded from JSON file, used to run simulation)
 */

object ConfigFromFile {

  val initialStates = scala.collection.mutable.HashMap[(String,String),WeightedRandomThingGenerator[State]]()

  new State(("NEW_SESSION","INITIAL_STATUS",200,"",""))
  val showUserWithState = new mutable.HashMap[String, Boolean]()
  val levelGenerator = new WeightedRandomThingGenerator[String]()
  val authGenerator = new WeightedRandomThingGenerator[String]()

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
  var newUserAuth:String = Constants.DEFAULT_NEW_USER_AUTH
  var newUserLevel:String = Constants.DEFAULT_NEW_USER_LEVEL

  var startDate:Option[String] = None
  var endDate:Option[String] = None
  var nUsers:Option[Int] = None
  var firstUserId:Option[Int] = None
  var growthRate:Option[Double] = None
  var tag:Option[String] = None

  // tags for JSON config file
  val TRANSITIONS = "transitions"
  val NEW_SESSION = "new-session"
  val NEW_USER_AUTH = "new-user-auth"
  val NEW_USER_LEVEL = "new-user-level"
  val CHURNED_STATE = "churned-state"
  val SHOW_USER_DETAILS = "show-user-details"
  val PAGE = "page"
  val AUTH = "auth"
  val SHOW = "show"
  val SOURCE = "source"
  val DEST = "dest"
  val P = "p"
  val STATUS = "status"
  val METHOD = "method"
  val LEVEL = "level"
  val LEVELS = "levels"
  val AUTHS = "auths"
  val WEIGHT = "weight"
  val SEED = "seed"
  val SESSION_GAP = "session-gap"

  val ALPHA = "alpha"
  val BETA = "beta"
  val DAMPING = "damping"
  val WEEKEND_DAMPING = "weekend-damping"
  val WEEKEND_DAMPING_OFFSET = "weekend-damping-offset"
  val WEEKEND_DAMPING_SCALE = "weekend-damping-scale"

  val START_DATE = "start-date"
  val END_DATE = "end-date"
  val N_USERS = "n-users"
  val FIRST_USER_ID = "first-user-id"
  val GROWTH_RATE = "growth-rate"
  val TAG = "tag"

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

    jsonContents.get(NEW_USER_AUTH) match {
      case x: Some[Any] => newUserAuth = x.get.asInstanceOf[String]
      case None =>
    }

    jsonContents.get(NEW_USER_LEVEL) match {
      case x: Some[Any] => newUserLevel = x.get.asInstanceOf[String]
      case None =>
    }

    jsonContents.get(START_DATE) match {
      case x: Some[Any] => startDate = Some(x.get.asInstanceOf[String])
      case None =>
    }

    jsonContents.get(END_DATE) match {
      case x: Some[Any] => endDate = Some(x.get.asInstanceOf[String])
      case None =>
    }

    jsonContents.get(N_USERS) match {
      case x: Some[Any] => nUsers = Some(x.get.asInstanceOf[Double].toInt)
      case None =>
    }

    jsonContents.get(FIRST_USER_ID) match {
      case x: Some[Any] => firstUserId = Some(x.get.asInstanceOf[Double].toInt)
      case None =>
    }

    jsonContents.get(GROWTH_RATE) match {
      case x: Some[Any] => growthRate = Some(x.get.asInstanceOf[Double])
      case None =>
    }

    jsonContents.get(TAG) match {
      case x: Some[Any] => tag = Some(x.get.asInstanceOf[String])
      case None =>
    }


    churnedState = jsonContents.get(CHURNED_STATE).asInstanceOf[Option[String]]

    val states = new mutable.HashMap[(String,String,Int,String,String), State]

    val transitions = jsonContents.getOrElse(TRANSITIONS,List()).asInstanceOf[List[Any]]
    for (t <- transitions) {
      val transition = t.asInstanceOf[Map[String,Any]]
      val source = readState(transition.getOrElse(SOURCE,List()).asInstanceOf[Map[String,Any]])
      val dest = readState(transition.getOrElse(DEST,List()).asInstanceOf[Map[String,Any]])
      val p      = transition.getOrElse(P,Unit).asInstanceOf[Double]

      if (!states.contains(source)) {
        states += (source ->
          new State(source))
      }
      if (!states.contains(dest)) {
        states += (dest ->
          new State(dest))
      }
      states(source)
        .addLateral(states(dest),p)
    }

    val initial = jsonContents.getOrElse(NEW_SESSION,List()).asInstanceOf[List[Any]]
    for (i <- initial) {
      val item = i.asInstanceOf[Map[String,Any]]
      val weight = item.get(WEIGHT).get.asInstanceOf[Double].toInt
      val stateTuple = readState(item)
      val (_,auth,_,_,level) = stateTuple
      if (!initialStates.contains((auth,level)))
        initialStates.put((auth,level), new WeightedRandomThingGenerator[State]())
      if (!states.contains(stateTuple))
        throw new Exception("Unkown state found while processing initial states: " + stateTuple.toString())

      initialStates(auth,level).add(states.get(stateTuple).get, weight)
    }

    // TODO: put in check for initial state probabilities

    val showUserDetails = jsonContents.getOrElse(SHOW_USER_DETAILS,List()).asInstanceOf[List[Any]]
    for (i <- showUserDetails) {
      val item = i.asInstanceOf[Map[String,Any]]
      val auth = item.get(AUTH).get.asInstanceOf[String]
      val show     = item.get(SHOW).get.asInstanceOf[Boolean]
      showUserWithState += (auth -> show)
    }

    val levels = jsonContents.getOrElse(LEVELS,List()).asInstanceOf[List[Any]]
    for (level <- levels) {
      val item = level.asInstanceOf[Map[String,Any]]
      val levelName = item.getOrElse(LEVEL,"").asInstanceOf[String]
      val levelWeight = item.getOrElse(WEIGHT,0.0).asInstanceOf[Double].toInt
      levelGenerator.add(levelName,levelWeight)
    }

    val auths = jsonContents.getOrElse(AUTHS,List()).asInstanceOf[List[Any]]
    for (auth <- auths) {
      val item = auth.asInstanceOf[Map[String,Any]]
      val levelName = item.getOrElse(AUTH,"").asInstanceOf[String]
      val levelWeight = item.getOrElse(WEIGHT,0.0).asInstanceOf[Double].toInt
      authGenerator.add(levelName,levelWeight)
    }

  }

  def readState(m: Map[String,Any]) =
    (m.get(PAGE).get.asInstanceOf[String],
     m.get(AUTH).get.asInstanceOf[String],
     m.getOrElse(STATUS,"").asInstanceOf[Double].toInt,
     m.getOrElse(METHOD,"").asInstanceOf[String],
     m.getOrElse(LEVEL, "").asInstanceOf[String])

}
