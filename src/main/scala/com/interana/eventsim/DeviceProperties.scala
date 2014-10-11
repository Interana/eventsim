package com.interana.eventsim

import com.interana.eventsim.buildin.{RandomLocationGenerator, RandomUserAgentGenerator}

/**
 * Created by jadler on 9/16/14.
 */
object DeviceProperties {

  def randomProps: scala.collection.immutable.Map[String,Any] = {
    val userAgent = RandomUserAgentGenerator.randomThing
    Map[String,Any](
      "location" -> RandomLocationGenerator.randomThing,
      "userAgent" -> userAgent._1 //,
      //"browser" -> userAgent._2,
      //"OS" -> userAgent._3
    )
  }
}
