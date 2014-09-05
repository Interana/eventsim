eventsim
========

Eventsim is a program designed to generate event data for testing and demos. It's written in Scala (which provides
a good balance of scalability, brevity, and clarity).

How it works
============

When you run the simulator, it starts by generating a set of users with randomly picked properties. This includes
attributes like names and location as well as usage characteristics.

There are three parameters that describe how users generate events:

* Alpha. This is the expected session length for the user. This is randomly generated from a lognormal distrbution
* Beta. This is the expected number of pages per session. This is also randomly generated from a lognormal distribution
* Gamma. This is the expected session interarrival time. This is thirty minutes plus a randomly selected value from an exponential distribution

When you run the simulator, you specify the average values and the simulator picks random values for users.

Usage
=====

Start by installing scala. Easiest way is

    $ brew install scala

To build the executable, run

    $ sbt assembly
    $ # make sure the script is executable
    $ chmod +x bin/eventsim

You can then run the simulator from the root directory with a command like this

    $ bin/eventsim --help
    [info] Running com.interana.eventsim.Main --help
      -a, --alpha  <arg>       expected session length (default = 300000.0)
      -b, --beta  <arg>        expected number of pages per session (default = 5.0)
      -e, --endtime  <arg>     end time for data
                               (default = 2014-08-29T09:44:16.287-07:00)
      -g, --gamma  <arg>       expected session inter-arrival time
                               (default = 4320000.0)
      -n, --nusers  <arg>      number of users (default = 1)
      -s, --starttime  <arg>   start time for data
                               (default = 2014-08-22T09:44:16.557-07:00)
          --help               Show help message

If you want to edit the code, I strongly recommend IntelliJ. Ping me for details on setting this up.