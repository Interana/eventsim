# Music Dataset Configuration Files

## Introduction
The eventsim tool is relatively general purpose, and the example
configuration files in the `eventsim/examples` directory are generally
simple. The set of configuration files in this directory are intended to
create a large set of data where parts of the population behave
differently from others. This enables more interesting behavioral
queries on the data and other more complex analysis.

## Music Configurations A-Z
The `eventsim/configs` directory contains a set of 26 `.json`
configuration files that are used to guide the evensim generator. Each
of these configurations has been tweaked in some way to generate
slightly different user event patterns. When combined all together, the
events look like they come from a diverse population of users who
interact with the music streaming application in slightly different ways
and have different experiences.

The set of configuration files are alphabetically named after musical
instruments and described below. The first twelve configurations are
variations on a template, with the main difference being in request and
session inter-arrival times, and dampening factors for evenings and
weekends.

1. Accordion - Acts as the basic template configuration with no
modification.
2. Banjo – Template with weekend-dampening reduced to 0.27, meaning that
users play somewhat more music than normal on the weekends.
3. Cello – Template with weekend-dampening increased to 0.79, meaning
that users play somewhat less music than normal on the weekends.
4. Drum – Template with evening dampening decreased to 0.04327, meaning
that users play somewhat more music than normal in the evenings.
5. Ektara – Template with evening dampening increased to 0.2137, meaning
that users play somewhat less music than normal in the evenings.
6. Fiddle – Template with expected request inter-arrival time decreased
to 60 seconds. In theory, this will generate more events per unit time,
but since most of the events are song plays that are gated by song
duration, the impact is small.
7. Guitar – Template with expected request inter-arrival time increased
to 120 seconds.
8. Horn – Template with expected session inter-arrival times decreased
to 3 days (259200 seconds). More sessions in the system means generally
more traffic and requests.
9. Igil – Template with expected session inter-arrival times increased
to 12 days (1036800 seconds). Fewer sessions in the system means
generally less traffic and requests.
10. Jug – Template with low dampening and less frequent new sessions.
11. Koto – Template with high dampening and more frequent new sessions.
12. Lute –Template with very low dampening and very infrequent new
sessions. This is somewhat analogous to machine "users" who might play
music in places like hotel lobbies.
13. Marimba – Modified state transition probabilities, with much higher
error rates and high chance of downgrades from paid level and
cancellations from both levels if an error is encountered.
14. Nagara – Modified state transition probabilities, with much higher
advertising rates leading to more downgrades and cancellations.
15. Oboe – Both high errors and high advertising.
16. Piano – Advertising+, Cancellation+
17. Quena – Advertising++, Cancellation++ 18. Recorder – Advertising++,
Cancellation+, Upgrade+
19. Saxophone – Advertising+++, Cancellation++, Upgrade++
20. Tuba – Advertising++++, Cancellation+++, Upgrade+
21. Ukelele – Generate the Accordion (template) data with a different
random seed. The intention is to then introduce periods of dropouts
(temporary churn) by using another tool to pull out events for certain
period of time. Something like jq filtering based on the timestamp (ts)
field.
22. Viola – Generate the Jug data and later introduce dropouts.
23. Whistle – Modified state transitions with higher probability of
ThumbsUp events, leading to higher probability of Upgrade events. These
are generally "happy" users.
24. Xylophone – Very "happy" users (ThumbsUp++, Upgrade++,
Cancellation-)
25. Yu – Generally "grumpy" users (ThumbsDown+, Cancellation+, Upgrade-)
26. Zither – Very "grumpy" users (ThumbsDown++,Cancellation++, Upgrade–)

You can pick and choose from these configurations to generate a set of
events, or generate events using all the configurations for a more
diverse set of events. 

## Before Generating the Events

Before diving in to generating the events, it may be useful to perform
some pre-work. In particular:

### Generate a Similar Songs Data File

The eventsim generator has the ability to randomly select a next song
that is similar to the current song. This is an *optional* step, and
eventsim will work fine if the Similar Songs file is missing. The main
consequence is that all users and sessions are likely to contain the
same distribution of songs, randomly selected based on overall listen
counts.

Using the Similar Songs data is more realistic behavior, since most
listeners will generally listen to similar songs during a music
streaming session. The data file used to map song similarities is large
(e.g., 516MiB) and needs to be generated from a smaller training data
file before it can be used. To generate it, use the eventsim binary with
the `--generate-similars` flag. You must still pass a configuration file
to get the process started: 

```
   bin/eventsim --config examples/example-config.json --generate-similars
``` 

The process takes a while to run and starts silently, so be patient.
When completed, you should have a file named `similar_songs.csv.gz` in
the `eventsim\data` directory.

Once the file exists, invoking the generator will use the file. Since
the file is large and needs to be loaded into memory to be used, it will
add significant startup overhead to the process. You can rename it to
something else (e.g., `__similar_songs.csv.gz`) so that it's ignored
while testing and fine-tuning your scripts and configuration files. 

### Automate Generating Multiple Configurations

The generator is currently single-threaded and doesn't fully take
advantage of multiple cores in modern servers. If you're planning to
generate several configurations (tags), it's useful to prepare some
simple scripts that parallelize the process. An example is provided in
`eventsim\examples\generate_tags.sh` for reference. Be sure to modify
the paths used to match your system.

The example configuration will generate around 1.1B events and use
approximately 226GiB of disk space before compression. If you want to
generate more, increase the starting number of users per configuration
(`-n 5000`) and the growth rate (`--growth-rate 0.8`). The example has
all the user counts and growth rates equal, but there's no reason why
some configurations can't start out with more users than others or grow
faster than others.

## Generating the Events

Once ready, generate the events as described elsewhere. 

## After Generating the Events

After generating the events, you may need to do some additional
processing to make the data ready to ingest. For example, you may need
to split the generated files into multiple smaller pieces if the
generated files are too large to be ingested all at once. If you're
following the configuration outline above, you may also need to filter
out certain timestamp ranges from some of the tags (e.g., Ukelele and
Viola).

## Using the Spreadsheet to Generate Transitions

Finally, if you want to generate your own page transition probabilities,
you can use the Microsoft Excel spreadsheet
(`eventsim/configs/Eventsim_Transitions_Worksheets.xlsx`) to simplify
the process:

1. The `Work` tab is the place to make adjustment to the transition
probabilities. For a fresh start, copy in the probabilities in the
locked `Template` tab. Then make the desired adjustments, checking that
value in the `TOTAL` column on the right doesn't exceed 1.0.

2. When ready, click over to the `Results` tab. Copy the values
generated for all the page rows and columns. There should be an equal
number of rows and columns selected.

3. Open up a new Microsoft Word document and use the `Paste Special...`
option from the `Edit` menu. Select `Unformatted Text` as the format so
that the copied transitions are pasted into the blank document as text.
This will dump all the text into the Word doc, and we'll need to clean
it up so that it's more readable.

4. Starting a the top of the document, choose `Replace` from the `Edit`
menu. This should open up a sidebar. Enter `^t` in the `Search` field,
and leave the `Replace` field blank. Click `Find` button to find and
highlight all the tabs in the pasted text. Click the `Replace All`
button to delete all the tabs from the document.

5. We now need to insert a line feed between each of the transitions. Go
back to the top of the document and into the `Find` field paste
`},{"source":` to find all the places where each transition entry ends
with a comma, and the next begins with the source page. Paste the same
string into the `Replace` field, then place your cursor after the comma,
enter `Ctrl-Q` to quote the next character, and finally hit `Enter`.
Click the `Find` button, followed by the `Replace All` button. This will
insert a line break after the end of each transition.

6. Go to the end of the file, and delete the last comma from the last
transition entry. It's not needed since there are no more transition
entried.

7. Choose `Save As...` from the `Edit` menu and select `Plain Text` as
the output format. Give it a meaningful name.

8. Open a copy of some existing configuration file in a text editor and
find the place where the page transitions are defined. Look for the line
beginning with `"transitions" : [`. Delete all the old transitions
between the square brackets. Insert the new transitions that you saved
as plain text. Save the new configuration with an appropriate name. It
should now be ready to use with the eventsim generator. 



 
