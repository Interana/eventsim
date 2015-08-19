package com.interana.eventsim.buildin

/**
 * Created by jadler on 9/16/14.
 */
object DeviceProperties {

  def randomProps =
    Map[String,Any](
      "location" -> RandomLocationGenerator.randomThing,
      "userAgent" -> RandomUserAgentGenerator.randomThing._1
    )

}
