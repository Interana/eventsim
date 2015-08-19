package com.interana.eventsim.buildin

import com.interana.eventsim.{Constants, Main, TimeUtilities}
import org.joda.time.DateTime

object UserProperties {
  // utilities for generating random properties for users

  def randomProps = {
    val secondsSinceRegistration =
      Math.min(
        TimeUtilities.exponentialRandomValue(Main.ConfFromOptions.growthRate.get.getOrElse(0.2)*Constants.SECONDS_PER_YEAR).toInt,
        (Constants.SECONDS_PER_YEAR * 5).toInt)

    val registrationTime = new DateTime().minusSeconds(secondsSinceRegistration)
    val firstNameAndGender = RandomFirstNameGenerator.randomThing
    val location = RandomLocationGenerator.randomThing

    Map[String,Any](
      "lastName" -> RandomLastNameGenerator.randomThing,
      "firstName" -> firstNameAndGender._1,
      "gender" -> firstNameAndGender._2,
      "registration" -> registrationTime.getMillis(),
      "location" -> location
    )
  }

  def randomNewProps(dt: DateTime) =
    randomProps + ("registration" -> dt.getMillis())

}