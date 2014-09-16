package com.interana.eventsim

import com.interana.eventsim.buildin._

object UserProperties {
  // utilities for generating random properties for users

  val randomLastNameGenerator = new RandomLastNameGenerator
  val randomFirstNameGenerator = new RandomFirstNameGenerator

  def randomProps: scala.collection.immutable.Map[String,Any] = {
    val firstNameAndGender = randomFirstNameGenerator.randomThing
    Map[String,Any](
      "lastName" -> randomLastNameGenerator.randomThing,
      "firstName" -> firstNameAndGender._1,
      "gender" -> firstNameAndGender._2
    )
  }

}