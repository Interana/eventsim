package com.interana.eventsim

import org.apache.commons.math3.random.{MersenneTwister, RandomGenerator}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.rogach.scallop.{ValueConverter, ScallopOption, ScallopConf}

import scala.collection.mutable

object Main extends App {
  val rng: RandomGenerator = new MersenneTwister(1L)
  private val sqrtE = Math.exp(0.5)

  def logNormalRandomValue = Math.exp(rng.nextGaussian()) / sqrtE

  val users = new mutable.PriorityQueue[User]()

  object Conf extends ScallopConf(args) {
    val nUsers: ScallopOption[Int] =
      opt[Int]("nusers", descr = "number of users",
        required=false, default=Option(1))
    val alpha: ScallopOption[Double] =
      opt[Double]("alpha", descr = "expected session length",
        required=false, default=Option(300000))
    val beta: ScallopOption[Double] =
      opt[Double]("beta", descr = "expected number of pages per session",
        required=false,default=Option(5))
    val gamma: ScallopOption[Double] =
      opt[Double]("gamma", descr = "expected session inter-arrival time",
        required=false,default=Option(TimeUtilities.MILLISECONDS_PER_DAY * 3))
    val startTimeArg: ScallopOption[String] =
      opt[String]("starttime", descr = "start time for data",
        required=false,default=Option(new DateTime().minusDays(14).toString(ISODateTimeFormat.dateTime()))
      )
    val endTimeArg: ScallopOption[String] =
      opt[String]("endtime", descr = "end time for data",
        required=false,default=Option(new DateTime().minusDays(7).toString(ISODateTimeFormat.dateTime()))
      )
  }

  val startTime = new DateTime(Conf.startTimeArg())
  val endTime = new DateTime(Conf.endTimeArg())

  (0 until Conf.nUsers()).foreach((_) =>
    users += new User(
      Conf.alpha() * logNormalRandomValue, // alpha = expected session length
      Conf.beta() * logNormalRandomValue, // beta = expected number of pages per session
      Conf.gamma() * logNormalRandomValue, // gamma = expected session inter-arrival time
      startTime, // start time
      ExampleSite.homePage, // start state
      UserProperties.randomProps // properties
    ))

  var clock = startTime
  while (clock.isBefore(endTime)) {
    val u = users.dequeue()
    clock = u.session.nextEventTimeStamp
    if (clock.isAfter(startTime)) println(u.eventString)
    u.nextEvent
    users += u
  }

}

