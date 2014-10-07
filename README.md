eventsim
========

Eventsim is a program designed to generate event data for testing and demos. It's written in Scala (which provides
a good balance of scalability, brevity, and clarity).

How it works
============

When you run the simulator, it starts by generating a set of users with randomly picked properties. This includes
attributes like names and location as well as usage characteristics.

You need to specify a configuration file (a sample is included in `examples/site.json`). This file
specifies how sessions are generated. In this file you specify several parameters:

* Alpha. This is the expected session length for the user (in seconds). This is randomly generated from a lognormal
distrbution
* Beta. This is the expected session interarrival time (in seconds). This is thirty minutes plus a randomly selected
value from an exponential distribution
* Damping. This is the damping factor for daily cycles
* Seed. Seed for the random number generator

You also specify the event state machine. Each state includes a page and a status. Status should be
used to describe a user's status: unregistered, logged in, logged out, cancelled, etc. Pages are
used to describe a user's page. Here is how you specify the state machine:

* Transitions. Describe the pair of page and status before and after each transition, and the
probability of the transition.
* New user. Describes the page and status for each new user (and probability of arriving for the
first time with one of those states.
* New session. Describes that page and status for each new session.
* Show user details. For each status, states whether or not users are shown in the generated event log

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
      -a, --attrition-rate  <arg>   annual user attrition rate (as a fraction of
                                    current, so 1% => 0.01) (default = 0.0)
      -c, --config  <arg>           config file
      -e, --end-time  <arg>         end time for data
                                    (default = 2014-09-15T11:21:22.122-07:00)
      -f, --from  <arg>             from x days ago (default = 15)
      -g, --growth-rate  <arg>      annual user growth rate (as a fraction of current,
                                    so 1% => 0.01) (default = 0.0)
      -n, --nusers  <arg>           initial number of users (default = 1)
      -s, --start-time  <arg>       start time for data
                                    (default = 2014-09-08T11:21:22.147-07:00)
      -t, --to  <arg>               to y days ago (default = 1)
      -v, --verbose                 verbose output (not implemented yet)
          --noverbose               silent mode
          --help                    Show help message

     trailing arguments:
      output-file (not required)   File name

If you want to edit the code, I strongly recommend IntelliJ. Ping me for details on setting this up.