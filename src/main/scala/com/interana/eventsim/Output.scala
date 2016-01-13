package com.interana.eventsim

import java.io.{FileOutputStream, File}
import java.time.ZoneOffset
import java.util.Properties

import com.interana.eventsim.config.ConfigFromFile
import com.interana.eventsim.events.{JSONConstructor, Constructor}
import org.apache.kafka.clients.producer.{ProducerRecord, KafkaProducer}

/**
  * Created by jadler on 1/13/16.
  */
object Output {
  // place to put all the output related code

  trait canwrite {
    def write()
    def flushAndClose()
  }

  private class FileEventWriter(val constructor: Constructor, val file: File) extends Object with canwrite {
    val out = new FileOutputStream(file)
    def write() = out.write(constructor.end().asInstanceOf[Array[Byte]])

    override def flushAndClose(): Unit = {out.flush(); out.close()}
  }

  private class KafkaEventWriter(val constructor: Constructor, val topic: String, val brokers: String) extends Object with canwrite {

    val props = new Properties()
    props.put("key.serializer.class", "org.apache.kafka.common.serialization.ByteArraySerializer")
    props.put("value.serializer.class",
      if (constructor.isInstanceOf[JSONConstructor]) "org.apache.kafka.common.serialization.StringSerializer"
      else "io.confluent.kafka.serializers.KafkaAvroSerializer")
    props.put("metadata.broker.list", brokers)


    val producer = new KafkaProducer[Object, Object](props)

    def write() = {
      val value = constructor.end()
      val pr = new ProducerRecord[Object, Object](topic, value)
      producer.send(pr)
    }

    override def flushAndClose(): Unit = {producer.flush(); producer.close();}
  }

  val authConstructor: com.interana.eventsim.events.Auth.Constructor =
    if (Main.useAvro) new com.interana.eventsim.events.Auth.AvroConstructor()
    else new com.interana.eventsim.events.Auth.JSONConstructor()

  val listenConstructor: com.interana.eventsim.events.Listen.Constructor =
    if (Main.useAvro) new com.interana.eventsim.events.Listen.AvroConstructor()
    else new com.interana.eventsim.events.Listen.JSONConstructor()

  val pageViewConstructor: com.interana.eventsim.events.PageView.Constructor =
    if (Main.useAvro) new com.interana.eventsim.events.PageView.AvroConstructor()
    else new com.interana.eventsim.events.PageView.JSONConstructor()

  val statusChangeConstructor: com.interana.eventsim.events.StatusChange.Constructor =
    if (Main.useAvro) new com.interana.eventsim.events.StatusChange.AvroConstructor()
    else new com.interana.eventsim.events.StatusChange.JSONConstructor()

  val kbl = Main.ConfFromOptions.kafkaBrokerList
  val dirName = new File(if (Main.ConfFromOptions.outputDir.isSupplied) Main.ConfFromOptions.outputDir.get.get else "output")

  if (!dirName.exists())
    dirName.mkdir()

  val authEventWriter =
    if (kbl.isSupplied) new KafkaEventWriter(authConstructor, "auth_events", kbl.get.get)
    else new FileEventWriter(authConstructor, new File(dirName, "auth_events"))
  val listenEventWriter =
    if (kbl.isSupplied) new KafkaEventWriter(listenConstructor, "listen_events", kbl.get.get)
    else new FileEventWriter(listenConstructor, new File(dirName, "listen_events"))
  val pageViewEventWriter =
    if (kbl.isSupplied) new KafkaEventWriter(pageViewConstructor, "page_view_events", kbl.get.get)
    else new FileEventWriter(pageViewConstructor, new File(dirName, "page_view_events"))
  val statusChangeEventWriter =
    if (kbl.isSupplied) new KafkaEventWriter(statusChangeConstructor, "status_change_events", kbl.get.get)
    else new FileEventWriter(statusChangeConstructor, new File(dirName, "status_change_events"))

  def flushAndClose(): Unit = {
    authEventWriter.flushAndClose()
    listenEventWriter.flushAndClose()
    pageViewEventWriter.flushAndClose()
    statusChangeEventWriter.flushAndClose()
  }

  def writeEvents(session: Session, device: scala.collection.immutable.Map[String,Any], userId: Int, props: Map[String,Any]) = {

    val showUserDetails = ConfigFromFile.showUserWithState(session.currentState.auth)
    pageViewConstructor.start
    pageViewConstructor.setTs(session.nextEventTimeStamp.get.toInstant(ZoneOffset.UTC)toEpochMilli())
    pageViewConstructor.setSessionId( session.sessionId)
    pageViewConstructor.setPage(session.currentState.page)
    pageViewConstructor.setAuth(session.currentState.auth)
    pageViewConstructor.setMethod(session.currentState.method)
    pageViewConstructor.setStatus(session.currentState.status)
    pageViewConstructor.setLevel(session.currentState.level)
    pageViewConstructor.setItemInSession(session.itemInSession)
    pageViewConstructor.setDeviceDetails(device)
    if (showUserDetails) {
      pageViewConstructor.setUserId(userId)
      pageViewConstructor.setUserDetails(props)
      if (Main.tag.isDefined)
        pageViewConstructor.setTag(Main.tag.get)
    }

    if (session.currentState.page=="NextSong") {
      pageViewConstructor.setArtist(session.currentSong.get._2)
      pageViewConstructor.setTitle(session.currentSong.get._3)
      pageViewConstructor.setDuration(session.currentSong.get._4)
      listenConstructor.start()
      listenConstructor.setArtist(session.currentSong.get._2)
      listenConstructor.setTitle(session.currentSong.get._3)
      listenConstructor.setDuration(session.currentSong.get._4)
      listenConstructor.setTs(session.nextEventTimeStamp.get.toInstant(ZoneOffset.UTC)toEpochMilli())
      listenConstructor.setSessionId( session.sessionId)
      listenConstructor.setAuth(session.currentState.auth)
      listenConstructor.setLevel(session.currentState.level)
      listenConstructor.setItemInSession(session.itemInSession)
      listenConstructor.setDeviceDetails(device)
      if (showUserDetails) {
        listenConstructor.setUserId(userId)
        listenConstructor.setUserDetails(props)
        if (Main.tag.isDefined)
          listenConstructor.setTag(Main.tag.get)
      }
      listenEventWriter.write
    }

    if (session.currentState.page=="Submit Downgrade" || session.currentState.page=="Submit Upgrade") {
      statusChangeConstructor.start()
      statusChangeConstructor.setTs(session.nextEventTimeStamp.get.toInstant(ZoneOffset.UTC)toEpochMilli())
      statusChangeConstructor.setSessionId( session.sessionId)
      statusChangeConstructor.setAuth(session.currentState.auth)
      statusChangeConstructor.setLevel(session.currentState.level)
      statusChangeConstructor.setItemInSession(session.itemInSession)
      statusChangeConstructor.setDeviceDetails(device)
      if (showUserDetails) {
        statusChangeConstructor.setUserId(userId)
        statusChangeConstructor.setUserDetails(props)
        if (Main.tag.isDefined)
          statusChangeConstructor.setTag(Main.tag.get)
      }
      statusChangeEventWriter.write
    }

    if (session.previousState.isDefined && session.previousState.get.page=="Login") {
      authConstructor.start()
      authConstructor.setTs(session.nextEventTimeStamp.get.toInstant(ZoneOffset.UTC)toEpochMilli())
      authConstructor.setSessionId( session.sessionId)
      authConstructor.setLevel(session.currentState.level)
      authConstructor.setItemInSession(session.itemInSession)
      authConstructor.setDeviceDetails(device)
      if (showUserDetails) {
        authConstructor.setUserId(userId)
        authConstructor.setUserDetails(props)
        if (Main.tag.isDefined)
          authConstructor.setTag(Main.tag.get)
      }
      authConstructor.setSuccess(session.currentState.auth == "Logged In")
      authEventWriter.write
    }

    pageViewEventWriter.write
  }
}
