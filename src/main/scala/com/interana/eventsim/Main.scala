package com.interana.eventsim

import java.io.FileOutputStream
import java.util.Properties

import com.interana.eventsim.Utilities.trackListenCount
import com.interana.eventsim.buildin.{UserProperties, DeviceProperties}
import kafka.producer.{Producer, ProducerConfig}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import org.rogach.scallop.{ScallopConf, ScallopOption}

import scala.collection.mutable

object Main extends App {
  private val sqrtE = Math.exp(0.5)

  def logNormalRandomValue = Math.exp(TimeUtilities.rng.nextGaussian()) / sqrtE

  val users = new mutable.PriorityQueue[User]()

  object ConfFromOptions extends ScallopConf(args) {
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
        required = false, default = Option(new DateTime().minusDays(14).toString(ISODateTimeFormat.dateTime())))

    val endTimeArg: ScallopOption[String] =
      opt[String]("end-time", descr = "end time for data",
        required = false, default = Option(new DateTime().minusDays(7).toString(ISODateTimeFormat.dateTime())))

    val from: ScallopOption[Int] =
      opt[Int]("from", descr = "from x days ago", required = false, default = Option(15))

    val to: ScallopOption[Int] =
      opt[Int]("to", descr = "to y days ago", required = false, default = Option(1))

    val firstUserId: ScallopOption[Int] =
      opt[Int]("userid", descr = "first user id", required = false, default = Option(1))

    val randomSeed: ScallopOption[Int] =
      opt[Int]("randomseed", descr = "random seed", required = false)

    val configFile: ScallopOption[String] =
      opt[String]("config", descr = "config file", required = true)

    val tag: ScallopOption[String] =
      opt[String]("tag", descr = "tag applied to each line", required = false)

    val verbose = toggle("verbose", default = Some(false),
      descrYes = "verbose output (not implemented yet)", descrNo = "silent mode")
    val outputFile: ScallopOption[String] = trailArg[String]("output-file", required = false, descr = "File name")

    val kafkaTopic: ScallopOption[String] =
      opt[String]("kafkaTopic", descr = "kafka topic", required = false)

    val kafkaBrokerList: ScallopOption[String] =
      opt[String]("kafkaBrokerList", descr = "kafka broker list", required = false)

    val compute = toggle("compute", default = Some(false),
      descrYes = "create listen counts file then stop", descrNo = "run normally")

    val realTime = toggle("continuous", default = Some(false),
      descrYes = "continuous output", descrNo = "run all at once")

  }

  val startTime = if (ConfFromOptions.startTimeArg.isSupplied) {
    new DateTime(ConfFromOptions.startTimeArg())
  } else if (ConfigFromFile.startDate.nonEmpty) {
    new DateTime(ConfigFromFile.startDate.get)
  } else {
    new DateTime().minusDays(ConfFromOptions.from())
  }

  val endTime = if (ConfFromOptions.endTimeArg.isSupplied) {
    new DateTime(ConfFromOptions.endTimeArg())
  } else if (ConfigFromFile.endDate.nonEmpty) {
    new DateTime(ConfigFromFile.endDate.get)
  } else {
    new DateTime().minusDays(ConfFromOptions.to())
  }

  ConfigFromFile.configFileLoader(ConfFromOptions.configFile())

  var nUsers = ConfigFromFile.nUsers.getOrElse(ConfFromOptions.nUsers())

  val seed = if (ConfFromOptions.randomSeed.isSupplied) {
    ConfFromOptions.randomSeed.get.get.toLong
  } else {
    ConfigFromFile.seed
  }

  val kafkaProducer = if (ConfFromOptions.kafkaBrokerList.isDefined) {
    val kafkaProperties = new Properties()
    kafkaProperties.setProperty("metadata.broker.list", ConfFromOptions.kafkaBrokerList.get.get)
    val producerConfig = new ProducerConfig(kafkaProperties)
    new Some(new Producer[Array[Byte],Array[Byte]](producerConfig))
  } else None

  val realTime = ConfFromOptions.realTime.get.get

  def generateEvents = {

    val out = if (kafkaProducer.nonEmpty) {
      new KafkaOutputStream(kafkaProducer.get, ConfFromOptions.kafkaTopic.get.get)
    } else if (ConfFromOptions.outputFile.isSupplied) {
      new FileOutputStream(ConfFromOptions.outputFile())
    } else {
      System.out
    }

    (0 until nUsers).foreach((_) =>
      users += new User(
        ConfigFromFile.alpha * logNormalRandomValue,
        ConfigFromFile.beta * logNormalRandomValue,
        startTime,
        ConfigFromFile.initialStates,
        ConfigFromFile.authGenerator.randomThing,
        UserProperties.randomProps,
        DeviceProperties.randomProps,
        ConfigFromFile.levelGenerator.randomThing,
        out
      ))

    if (ConfFromOptions.growthRate() > 0) {
      var current = startTime
      while (current.isBefore(endTime)) {
        val mu = Constants.SECONDS_PER_YEAR / (nUsers * ConfFromOptions.growthRate())
        current = current.plusSeconds(TimeUtilities.exponentialRandomValue(mu).toInt)
        users += new User(
          ConfigFromFile.alpha * logNormalRandomValue,
          ConfigFromFile.beta * logNormalRandomValue,
          current,
          ConfigFromFile.initialStates,
          ConfigFromFile.newUserAuth,
          UserProperties.randomNewProps(current),
          DeviceProperties.randomProps,
          ConfigFromFile.newUserLevel,
          out
        )
        nUsers += 1
      }
    }
    System.err.println("Initial number of users: " + ConfFromOptions.nUsers() + ", Final number of users: " + nUsers)

    val startTimeString = startTime.toString(DateTimeFormat.shortDateTime())
    val endTimeString = endTime.toString(DateTimeFormat.shortDateTime())
    System.err.println("Start: " + startTimeString + ", End: " + endTimeString)

    var lastTimeStamp = System.currentTimeMillis()
    def showProgress(n: DateTime, users: Int, e: Int): Unit = {
      if ((e % 10000) == 0) {
        val now = System.currentTimeMillis()
        val rate = 10000000 / (now - lastTimeStamp)
        lastTimeStamp = now
        val message = // "Start: " + startTimeString + ", End: " + endTimeString + ", " +
          "Now: " + n.toString(DateTimeFormat.shortDateTime()) + ", Events:" + e + ", Rate: " + rate + " eps"
        System.err.write("\r".getBytes)
        System.err.write(message.getBytes)
      }
    }
    System.err.println("Starting to generate events.")
    System.err.println("Damping=" + ConfigFromFile.damping + ", Weekend-Damping=" + ConfigFromFile.weekendDamping)

    var clock = startTime
    var events = 1

    while (clock.isBefore(endTime)) {

      if (realTime) {
        val now = new DateTime()
        val dif = clock.getMillis - now.getMillis
        if (dif > 0)
          Thread.sleep(dif / 1000)
      }

      showProgress(clock, users.length, events)
      val u = users.dequeue()
      val prAttrition = nUsers * ConfFromOptions.attritionRate() *
        (endTime.getMillis - startTime.getMillis / Constants.SECONDS_PER_YEAR)
      clock = u.session.nextEventTimeStamp.get

      if (clock.isAfter(startTime)) u.writeEvent
      u.nextEvent(prAttrition)
      users += u
      events += 1
    }

    System.err.println("")
    System.err.println()

    out.flush()
    out.close()

  }

  if (ConfFromOptions.compute())
    trackListenCount.compute
  else
    this.generateEvents

}

