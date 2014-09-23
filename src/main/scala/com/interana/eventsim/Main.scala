package com.interana.eventsim

import java.io.PrintWriter

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.rogach.scallop.{ScallopConf, ScallopOption}

import scala.collection.mutable

object Main extends App {
  private val sqrtE = Math.exp(0.5)

  def logNormalRandomValue = Math.exp(TimeUtilities.rng.nextGaussian()) / sqrtE

  val users = new mutable.PriorityQueue[User]()

  object Conf extends ScallopConf(args) {
    val nUsers: ScallopOption[Int] =
      opt[Int]("nusers", descr = "initial number of users",
        required = false, default = Option(1))

    val growthRate: ScallopOption[Double] =
      opt[Double]("growth-rate", descr = "annual user growth rate (as a fraction of current, so 1% => 0.01)",
        required = false, default = Option(0.0))

    val attritionRate: ScallopOption[Double] =
      opt[Double]("attrition-rate", descr = "annual user attrition rate (as a fraction of current, so 1% => 0.01)",
        required = false, default = Option(0.0))

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

    val configFile: ScallopOption[String] =
      opt[String]("config", descr = "config file", required=true)

    val verbose = toggle("verbose", default = Some(false), descrYes = "verbose output (not implemented yet)", descrNo = "silent mode")
    val outputFile: ScallopOption[String] = trailArg[String]("output-file", required = false, descr = "File name")

  }

  val startTime = if (Conf.startTimeArg.isSupplied) {new DateTime(Conf.startTimeArg())} else {new DateTime().minusDays(Conf.from())}
  val endTime = if (Conf.endTimeArg.isSupplied) {new DateTime(Conf.endTimeArg())} else {new DateTime().minusDays(Conf.to())}

  SiteConfig.configFileLoader(Conf.configFile())

  var nUsers = Conf.nUsers()

  (0 until nUsers).foreach((_) =>
    users += new User(
      SiteConfig.alpha * logNormalRandomValue, // alpha = expected request inter-arrival time
      SiteConfig.beta * logNormalRandomValue, // beta = expected session inter-arrival time
      startTime, // start time
      SiteConfig.initialState, // initial session states
      UserProperties.randomProps,
      DeviceProperties.randomProps
    ))

  // val durationInSeconds = new Interval(startTime, endTime).toDuration().getStandardSeconds
  // val fractionOfYear = durationInSeconds / Constants.SECONDS_PER_YEAR
  if (Conf.growthRate() > 0) {
    var current = startTime
    while (current.isBefore(endTime)) {
      val mu = Constants.SECONDS_PER_YEAR / (nUsers * Conf.growthRate())
      current = current.plusSeconds(TimeUtilities.exponentialRandomValue(mu).toInt)
      users += new User(
        SiteConfig.alpha * logNormalRandomValue, // alpha = expected request inter-arrival time
        SiteConfig.beta * logNormalRandomValue, // beta = expected session inter-arrival time
        current, // start time
        SiteConfig.newUserState, // initial session states
        UserProperties.randomProps,
        DeviceProperties.randomProps
      )
      nUsers += 1
    }
  }
  System.err.println("Initial number of users: " + Conf.nUsers() + ", Final number of users: " + nUsers)

  val out = if (Conf.outputFile.isSupplied) {
    new PrintWriter(Conf.outputFile())
  } else {
    new PrintWriter(System.out)
  }

  val startTimeString = startTime.toString(ISODateTimeFormat.dateHourMinuteSecond())
  val endTimeString = endTime.toString(ISODateTimeFormat.dateHourMinuteSecond())
  def showProgress(n: DateTime, users: Int): Unit = {
    var message = "Start: " + startTimeString + ", End: " + endTimeString +
      ", Now: " + n.toString(ISODateTimeFormat.dateHourMinuteSecond()) + ", Events:" + events
    System.err.write("\r".getBytes)
    System.err.write(message.getBytes)
  }
  System.err.println("Starting to generate events.")

  // TODO: Add attrition
  var clock = startTime
  var events = 1
  while (clock.isBefore(endTime)) {

    showProgress(clock, users.length)
    val u = users.dequeue()
    val prAttrition = nUsers * Conf.attritionRate() * ( endTime.getMillis - startTime.getMillis / Constants.SECONDS_PER_YEAR)
    clock = u.session.nextEventTimeStamp.get

    if (clock.isAfter(startTime)) out.println(u.eventString)
    u.nextEvent(prAttrition)
    users += u
    events += 1
  }
  println("")

}

