package com.interana.eventsim

import com.interana.eventsim.generators._

object UserProperties {
  // utilities for generating random properties for users


  val randomLocationGenerator = new RandomLocationGenerator
  val randomLastNameGenerator = new RandomLastNameGenerator
  val randomFirstNameGenerator = new RandomFirstNameGenerator
  val randomUserAgentGenerator = new RandomUserAgentGenerator

  def randomProps: scala.collection.immutable.Map[String,Any] = {
    val firstNameAndGender = randomFirstNameGenerator.randomThing
    val userAgent = randomUserAgentGenerator.randomThing
    Map[String,Any](
      "location" -> randomLocationGenerator.randomThing,
      "lastName" -> randomLastNameGenerator.randomThing,
      "firstName" -> firstNameAndGender._1,
      "gender" -> firstNameAndGender._2,
      "userAgent" -> userAgent._1,
      "browser" -> userAgent._2,
      "OS" -> userAgent._3
    )
  }

}