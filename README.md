# Avalunche

A Clojure library designed to push large numbers of randomly generated Puppet reports into PuppetDB.

It is very rough-and-ready: the code is not production quality and there are lots of limitations. You have almost no
control over behaviour except to specify the PuppetDB instance and the number of reports you want to create. Super basic
hardcoded facts and catalogs are also included.

If you're happy to edit the source you have some more options, which are listed at the top of the core namespace:

    (def unchanged-report-percentage 95)  ; This percentage of reports (roughly) will be unchanged

    (def average-events-per-report 10)    ; On average, if a report is not unchanged it will have this many events

## Usage

To create reports on a PuppetDB instance located at http://localhost:8080:

    lein run <number-of-distinct-nodes> <number-of-reports-per-nodes>

To create 48 reports for each of 20 nodes, for a total of 960 reports:

    e.g. lein run 20 48

To create reports on a PuppetDB instance located elsewhere:

    lein run <number-of-distinct-nodes> <number-of-reports-per-nodes> <puppetdb-prefix>

    e.g. lein run 20 48 http://pe:8080

Only the http interface of PuppetDB is supported, not https.

## Future Work

* Command line options rather than source editing to tweak behaviour
* Better variety of events
* Reasonable fact and catalog support

## License

Copyright © 2015 Scott Walker

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
