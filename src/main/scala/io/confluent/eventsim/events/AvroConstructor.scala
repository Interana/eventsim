package io.confluent.eventsim.events

import java.io.ByteArrayOutputStream

import org.apache.avro.Schema
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter


/**
  * Created by jadler on 1/13/16.
  */
abstract class AvroConstructor[T]() extends Object with Constructor {

  def start(): Unit

  def schema: Schema

  def datumWriter = new SpecificDatumWriter[T](this.schema)

  def baos = new ByteArrayOutputStream(4096)

  def encoder = EncoderFactory.get().binaryEncoder(baos, null)

  def serialize(t: T): Array[Byte] = {
    baos.reset()
    datumWriter.write(t, encoder)
    encoder.flush()
    baos.toByteArray
  }

}
