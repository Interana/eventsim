package io.confluent.eventsim.buildin

object DeviceProperties {

  def randomProps = {
    val location = RandomLocationGenerator.randomThing

    Map[String, Any](
      "zip" -> location._1,
      "city" -> location._2,
      "state" -> location._3,
      "lat" -> location._4,
      "lon" -> location._5,
      "userAgent" -> RandomUserAgentGenerator.randomThing._1
    )
  }

}
