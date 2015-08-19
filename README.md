eventsim
========

Eventsim is a program  that generates event data for testing and demos. It's written in Scala (which provides
a good balance of scalability, brevity, and clarity). It's designed to replicate page requests for a fake music
web site (picture something like Spotify); it's designed to look like real use data, but be totally fake.

Statistical Model
=================

I wrote this simulator based on observations about how real users behave. I wanted to make sure that data looked
real: users would come and go randomly, some users would stay much longer than others, users would be more likely to
use the service in the middle of the day than the middle of the night, and much less likely to use the service on
weekends and holidays.

To make this work, I did the following:

* If you set the "damping" factors to zero, then users randomly arrive at the site according to a Poisson (memoryless)
process, but with a minimum gap of 30 minutes between sessions.
* The time between events for a use is given by a log-normal disribution
* Once a sessions has started, the user will randomly traverse a set of states until the session ends. The probability
of each state transition (including end of session) depends on the current state.
* On average, users will behave the same way in a session, regardless of the time of day or day of week
* If you enable damping for weekends and holidays, the probability that a user arrives on weekends and holidays drops.
The odds are scaled linearly over a course of a few hours (by default) around midnight (by default).
* If you enable damping for nighttime, the probability that a user arrives in the middle of the night is lower than
the probability that they arrive in the middle of the day. The odds roughly follow a sine wave.


How the simulation works
========================

When you run the simulator, it starts by generating a set of users with randomly picked properties. This includes
attributes like names and location as well as usage characteristics, like user engagement. Eventsim uses a
pseudo-random number generator: the generator is deterministic, but looks random.

You need to specify a configuration file (a sample is included in `examples/site.json`). This file
specifies how sessions are generated, and how the fake website works. In this file you can specify several parameters:

* Alpha. This is the expected number of seconds between events for a user. This is randomly generated from a lognormal
distrbution
* Beta. This is the expected session interarrival time (in seconds). This is thirty minutes plus a randomly selected
value from an exponential distribution
* Damping. This is the damping factor for daily cycles (larger values yield stronger cycles, smaller yield milder)
* Weekend Damping. This controls when and how quickly weekend traffic falls off
* Seed. Seed for the random number generator.

You also specify the event state machine. Each state includes a page and an HTTP status code. Status should be
used to describe a user's status: unregistered, logged in, logged out, cancelled, etc. Pages are
used to describe a user's page. Here is how you specify the state machine:

* Transitions. Describe the pair of page and status before and after each transition, and the
probability of the transition.
* New user. Describes the page and status for each new user (and probability of arriving for the
first time with one of those states).
* New session. Describes that page and status for each new session.
* Show user details. For each status, states whether or not users are shown in the generated event log.

When you run the simulator, you specify the mean values for alpha and beata and the simulator picks values for specific
users.

Configuration
=============

The current version of the simulator is hard-coded for a music web site. You can modify it to work for other types
of sites, but doing so will probably require modifications to the code (and not just to the config files).

Usage
=====

Start by installing scala. Easiest way on the Mac is

    $ brew install scala

On Linux

    $ git clone https://github.com/Interana/eventsim/
    $ echo "deb http://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    $ sudo apt-get update
    $ sudo apt-get install openjdk-7-jdk scala sbt

To build the executable, run

    $ sbt assembly
    $ # make sure the script is executable
    $ chmod +x bin/eventsim


The `bin/eventsim` script assumes a recent version of Java 8. You may have to modify it (drop the
string optimization options) for earlier versions.

You can then run the simulator from the root directory with a command like this

    $ $ bin/eventsim --help
      Java HotSpot(TM) 64-Bit Server VM warning: ignoring option UseStringCache; support was removed in 8.0
        -a, --attrition-rate  <arg>    annual user attrition rate (as a fraction of
                                       current, so 1% => 0.01) (default = 0.0)
            --compute                  compute listen counts then stop
            --nocompute                run normally
        -c, --config  <arg>            config file
            --continuous               continuous output
            --nocontinuous             run all at once
        -e, --end-time  <arg>          end time for data
                                       (default = 2015-07-29T09:39:24.448-07:00)
        -f, --from  <arg>              from x days ago (default = 15)
        -g, --growth-rate  <arg>       annual user growth rate (as a fraction of
                                       current, so 1% => 0.01) (default = 0.0)
            --kafkaBrokerList  <arg>   kafka broker list
        -k, --kafkaTopic  <arg>        kafka topic
        -n, --nusers  <arg>            initial number of users (default = 1)
        -r, --randomseed  <arg>        random seed
        -s, --start-time  <arg>        start time for data
                                       (default = 2015-07-22T09:39:24.481-07:00)
            --tag  <arg>               tag applied to each line
        -t, --to  <arg>                to y days ago (default = 1)
        -u, --userid  <arg>            first user id (default = 1)
        -v, --verbose                  verbose output (not implemented yet)
            --noverbose                silent mode
            --help                     Show help message

       trailing arguments:
        output-file (not required)   File name

Parameters from the command line override parameters in the config file. (To make it easier to recreate results,
we recommend hard-coding values in the config file when possible and saving that file.)

Example for 2.5 M events:

    $ bin/eventsim -c "examples/site.json" --from 365 --nusers 1000 --growth-rate 0.01 data/fake.json
    Initial number of users: 1000, Final number of users: 1010
    Starting to generate events.
    Damping=0.0625, Weekend-Damping=0.5
    Start: 2013-10-06T06:27:10, End: 2014-10-05T06:27:10, Now: 2014-10-05T06:27:07, Events:2468822

Example for more events:

    $ bin/eventsim -c "examples/site.json" --from 365 --nusers 30000 --growth-rate 0.30 data/fake.json

If you want to edit the code, I strongly recommend IntelliJ. Ping me for details on setting this up.

About the source data
=====================

The results of this simulation are fake... but they are based on real data. (We thought that using real data on
songs would make the simulation more colorful and interesting.)

The song data is from the Million Song Dataset, official website by Thierry Bertin-Mahieux,
available at: http://labrosa.ee.columbia.edu/millionsong/. For more information, see this paper:

> Thierry Bertin-Mahieux, Daniel P.W. Ellis, Brian Whitman, and Paul Lamere.
> The Million Song Dataset. In Proceedings of the 12th International Society
> for Music Information Retrieval Conference (ISMIR 2011), 2011.

The last names come from the US Census Bureau (see http://www.census.gov/genealogy/www/data/2000surnames/index.html).

The first names come from the Social Security Administration (see http://www.ssa.gov/oact/babynames/#&ht=1); we
took the top 1000 names for each sex from this site.

(Note that the first and last names are chosen independently. This leads to some unexpected, but awesome results.)

Location names are from the Census Bureau (see https://www.census.gov/popest/data/datasets.html).

User agents are from http://techblog.willshouse.com/2012/01/03/most-common-user-agents/