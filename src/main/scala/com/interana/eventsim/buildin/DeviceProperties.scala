package com.interana.eventsim.buildin

object DeviceProperties {

  def randomProps =
    Map[String,Any](
      "location" -> RandomLocationGenerator.randomThing,
      "userAgent" -> RandomUserAgentGenerator.randomThing._1
    )

}
