package com.interana.eventsim

import com.interana.eventsim.buildin._
import org.joda.time.DateTime

object UserProperties {
  // utilities for generating random properties for users

  val randomLastNameGenerator = new RandomLastNameGenerator
  val randomFirstNameGenerator = new RandomFirstNameGenerator

  def randomProps: scala.collection.immutable.Map[String,Any] = {
    val secondsSinceRegistration =
      Math.min(
        TimeUtilities.exponentialRandomValue(Main.Conf.growthRate.get.getOrElse(0.2)*Constants.SECONDS_PER_YEAR).toInt,
        (Constants.SECONDS_PER_YEAR * 5).toInt)

    val registrationTime = new DateTime().minusSeconds(secondsSinceRegistration)
    val firstNameAndGender = randomFirstNameGenerator.randomThing

    Map[String,Any](
      "lastName" -> randomLastNameGenerator.randomThing,
      "firstName" -> firstNameAndGender._1,
      "gender" -> firstNameAndGender._2,
      "registration" -> registrationTime.getMillis(),
      "level" -> SiteConfig.levelGenerator.randomThing
    )
  }

  def randomNewProps = {
    val p = randomProps
    p + ("level" -> SiteConfig.newUserLevel)
  }


}