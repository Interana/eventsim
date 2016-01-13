name := "eventsim"

version := "2.0"

scalaVersion := "2.11.6"

resolvers += Resolver.url("confluent", url("http://packages.confluent.io/maven"))

libraryDependencies += "org.apache.avro" % "avro" % "1.7.7"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6"

libraryDependencies += "de.jollyday" % "jollyday" % "0.5.1"

libraryDependencies += "org.rogach" % "scallop_2.11" % "0.9.5"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.7.0"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.0"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.9.0.0"

libraryDependencies += "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.4"

libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "2.0.0"

seq( sbtavro.SbtAvro.avroSettings : _*)