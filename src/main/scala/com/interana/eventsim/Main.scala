package com.interana.eventsim

import java.io.PrintWriter

import org.apache.commons.math3.random.{MersenneTwister, RandomGenerator}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.rogach.scallop.{ScallopConf, ScallopOption}

import scala.collection.mutable

object Main extends App {
  val rng: RandomGenerator = new MersenneTwister(1L)
  private val sqrtE = Math.exp(0.5)

  def logNormalRandomValue = Math.exp(rng.nextGaussian()) / sqrtE

  val users = new mutable.PriorityQueue[User]()

  object Conf extends ScallopConf(args) {
    val nUsers: ScallopOption[Int] =
      opt[Int]("nusers", descr = "number of users",
        required = false, default = Option(1))
    val alpha: ScallopOption[Double] =
      opt[Double]("alpha", descr = "expected request inter-arrival time",
        required = false, default = Option(60000))
    val beta: ScallopOption[Double] =
      opt[Double]("beta", descr = "expected session inter-arrival time",
        required = false, default = Option(TimeUtilities.MILLISECONDS_PER_DAY * 3))
    val startTimeArg: ScallopOption[String] =
      opt[String]("start-time", descr = "start time for data",
        required = false, default = Option(new DateTime().minusDays(14).toString(ISODateTimeFormat.dateTime()))
      )
    val endTimeArg: ScallopOption[String] =
      opt[String]("end-time", descr = "end time for data",
        required = false, default = Option(new DateTime().minusDays(7).toString(ISODateTimeFormat.dateTime()))
      )

    val from: ScallopOption[Int] =
      opt[Int]("from", descr = "from x days ago", required=false,default=Option(15))

    val to: ScallopOption[Int] =
      opt[Int]("to", descr = "to y days ago", required=false,default=Option(1))

    val graph: ScallopOption[String] =
      opt[String]("graph", descr = "transition graph", required=true)

    val damping: ScallopOption[Float] =
      opt[Float]("damping", descr = "damping factor for daily traffic, between 0 and 0.125",
        required=false, validate=( (f:Float) => (f>=0 && f <=0.125)))

    val verbose = toggle("verbose", default = Some(false), descrYes = "verbose output (not implemented yet)", descrNo = "silent mode")
    val outputFile: ScallopOption[String] = trailArg[String]("output-file", required = false, descr = "File name")

  }

  val startTime = if (Conf.startTimeArg.isSupplied) {new DateTime(Conf.startTimeArg())} else {new DateTime().minusDays(Conf.from())}
  val endTime = if (Conf.endTimeArg.isSupplied) {new DateTime(Conf.endTimeArg())} else {new DateTime().minusDays(Conf.to())}

  val initialState = State.stateFileLoader(Conf.graph())

  println("initial State = " + initialState.toString())

  if (Conf.damping.isSupplied) TimeUtilities.damping = Conf.damping()

  (0 until Conf.nUsers()).foreach((_) =>
    users += new User(
      Conf.alpha() * logNormalRandomValue, // alpha = expected request inter-arrival time
      Conf.beta() * logNormalRandomValue, // beta = expected session inter-arrival time
      startTime, // start time
      initialState, // start state
      UserProperties.randomProps // properties
    ))

  val out = if (Conf.outputFile.isSupplied) {
    new PrintWriter(Conf.outputFile())
  } else {
    new PrintWriter(System.out)
  }

  var clock = startTime
  while (clock.isBefore(endTime)) {
    val u = users.dequeue()
    clock = u.session.nextEventTimeStamp
    if (clock.isAfter(startTime)) out.println(u.eventString)
    u.nextEvent()
    users += u
  }

}

