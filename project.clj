(defproject
  avalunche "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [puppetlabs/http-client "0.4.2"]
                 [clj-time "0.5.1"]
                 [cheshire "5.3.1"]]
  :repl-options {:init (do (require 'spyscope.core)
                           (use 'avalunche.testutils.repl))}
  :main avalunche.core
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.5"]
                                  [spyscope "0.1.5"]]}})
