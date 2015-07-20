# Avalunche

A Clojure library designed to push large numbers of randomly generated Puppet reports into PuppetDB.

It is very rough-and-ready: the code is not production quality and there are lots of limitations. You have almost no
control over behaviour except to specify the PuppetDB instance and the number of nodes/reports you want to create. 
Super basic hardcoded facts and catalogs are also included.

## Usage

To create reports on a PuppetDB instance located at http://localhost:8080:

    lein run <number-of-distinct-nodes> <number-of-reports-per-nodes>

To create 48 reports for each of 20 nodes, for a total of 960 reports:

    e.g. lein run 20 48

To create reports on a PuppetDB instance located elsewhere:

    lein run <number-of-distinct-nodes> <number-of-reports-per-nodes> <optional-puppetdb-prefix>

    e.g. lein run 20 48 http://pe:8080

Only the http interface of PuppetDB is supported, not https.

If you're happy to edit the source you have some more options, which are listed at the top of the core namespace:

    (def average-events-per-report 50)    ; On average, if a report is not unchanged it will have this many events

## Limitations

There are lots of limitations that it would be great to address, in rough order of priority:

* The number of events should be much less than the number of resources but they are currently the same
* Unchanged counts can be negative but shouldn't be
* The randomization thresholds for statuses should be configurable
* Metrics are totally random and consequently are not internally consistent
* Facts are very static - varied selection of facts would be useful
* Node/Resource/File names are unrealistic
* Node/Resource/File names don't vary much in length - very short and very long names would be useful
* The catalog is the simplest possible tree - it should be more complex or of configurable complexity and shape
* The relationship between skipped and failed resources is not correct
* SSL not supported

## License

Copyright © 2015 Scott Walker

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
