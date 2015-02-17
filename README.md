# Avalunche

A Clojure library designed to push large numbers of randomly generated Puppet reports into PuppetDB.

There are lots of limitations. You have almost no control over behaviour except to specify the PuppetDB instance and the number of reports you want to create.

## Usage

    lein run *puppetdb-url-prefix* *number*

    e.g. lein run http://pe:8080 100

Only the http:// interface of PuppetDB is supported.

## License

Copyright © 2015 Scott Walker

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
