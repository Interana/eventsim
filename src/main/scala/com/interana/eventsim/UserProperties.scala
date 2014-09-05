package com.interana.eventsim

import com.interana.eventsim.generators.{RandomLocationGenerator, RandomLastNameGenerator, RandomFirstNameGenerator}

import scala.io.Source

object UserProperties {
  // utilities for generating random properties for users


  val randomLocationGenerator = new RandomLocationGenerator
  val randomLastNameGenerator = new RandomLastNameGenerator
  val randomFirstNameGenerator = new RandomFirstNameGenerator

  def randomProps: scala.collection.immutable.Map[String,Any] = {
    val firstNameAndGender = randomFirstNameGenerator.randomThing
    Map[String,Any](
      "location" -> randomLocationGenerator.randomThing,
      "lastName" -> randomLastNameGenerator.randomThing,
      "firstName" -> firstNameAndGender._1,
      "gender" -> firstNameAndGender._2
    )
  }

}