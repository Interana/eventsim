package com.interana.eventsim

import com.interana.eventsim.buildin._
import org.joda.time.DateTime

object UserProperties {
  // utilities for generating random properties for users


  def randomProps: Map[String, Any] = {
    val secondsSinceRegistration =
      Math.min(
        TimeUtilities.exponentialRandomValue(Main.Conf.growthRate.get.getOrElse(0.2)*Constants.SECONDS_PER_YEAR).toInt,
        (Constants.SECONDS_PER_YEAR * 5).toInt)

    val registrationTime = new DateTime().minusSeconds(secondsSinceRegistration)
    val firstNameAndGender = RandomFirstNameGenerator.randomThing
    val location = RandomLocationGenerator.randomThing

    Map[String,Any](
      "lastName" -> RandomLastNameGenerator.randomThing,
      "firstName" -> firstNameAndGender._1,
      "gender" -> firstNameAndGender._2,
      "registration" -> registrationTime.getMillis(),
      // "level" -> SiteConfig.levelGenerator.randomThing,
      "location" -> location
    )
  }

  def randomNewProps(dt: DateTime) = {
    val p = randomProps
     p + ("registration" -> dt.getMillis())
  }


}