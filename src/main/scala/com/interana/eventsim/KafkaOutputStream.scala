package com.interana.eventsim

import java.io.OutputStream

import kafka.producer.{KeyedMessage, Producer}

import scala.collection.mutable.ArrayBuffer

class KafkaOutputStream(val producer: Producer[Array[Byte],Array[Byte]], val topic: String) extends OutputStream {

  val buffer = new ArrayBuffer[Byte](4096)

  override def write(i: Int) = {
    buffer.append(i.toByte)
  }

  override def flush() = {
    val msg = new KeyedMessage[Array[Byte], Array[Byte]](topic, buffer.toArray[Byte] )
    producer.send(msg)
    buffer.clear()
  }

}
