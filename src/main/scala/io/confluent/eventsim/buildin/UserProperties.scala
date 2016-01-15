package io.confluent.eventsim.buildin

import java.time.{LocalDateTime, ZoneOffset}

import io.confluent.eventsim.{Constants, Main, TimeUtilities}

object UserProperties {
  // utilities for generating random properties for users

  def randomProps = {
    val secondsSinceRegistration =
      Math.min(
        TimeUtilities.exponentialRandomValue(Main.growthRate.getOrElse(0.0) * Constants.SECONDS_PER_YEAR).toInt,
        (Constants.SECONDS_PER_YEAR * 5).toInt)

    val registrationTime = Main.startTime.minusSeconds(secondsSinceRegistration)
    val firstNameAndGender = RandomFirstNameGenerator.randomThing
    val location = RandomLocationGenerator.randomThing

    Map[String, Any](
      "lastName" -> RandomLastNameGenerator.randomThing,
      "firstName" -> firstNameAndGender._1,
      "gender" -> firstNameAndGender._2,
      "registration" -> registrationTime.toInstant(ZoneOffset.UTC).toEpochMilli
    )
  }

  def randomNewProps(dt: LocalDateTime) =
    randomProps + ("registration" -> dt.toInstant(ZoneOffset.UTC).toEpochMilli)

}