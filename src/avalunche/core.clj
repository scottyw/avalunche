(ns avalunche.core
  (:require [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-time.format :as time-fmt]
            [puppetlabs.http.client.sync :as http])
  (:import (java.util UUID)
           (java.util Date)
           (org.joda.time DateTime DateTimeZone)))

(def id (atom 0))

(def ts (atom (.getTime (Date.))))

(def event-statuses ["unchanged", "changed", "noop", "skipped", "failed"])

(def report-statuses ["unchanged", "changed", "noop", "failed"])

(def environments ["production", "development", "test", "staging"])

; Original looks like "2015-07-16T16:12:07.215744634+00:00"
(defn- make-timestamp []
  (time-fmt/unparse
    (:date-time time-fmt/formatters)
    (DateTime.
      (swap! ts (partial + 10000))
      #^DateTimeZone time/utc)))

(defn- make-event []
  (let [file (str "/var/log/foo/" (UUID/randomUUID) " .log")]
    {:containment-path ["Stage[main]" "Puppet_enterprise::Mcollective::Server::Logs" (str "File[" file "]")]
     :new-value        "present"
     :resource-title   file
     :property         "ensure"
     :file             "/opt/puppet/share/puppet/modules/puppet_enterprise/manifests/foo/server/logs.pp"
     :old-value        "absent"
     :line             (rand-int 200)
     :status           (get event-statuses (rand-int (count event-statuses)))
     :resource-type    "File"
     :timestamp        (make-timestamp)
     :message          "current_value absent, should be present (noop)"}))

(defn- make-events [x]
  (vec (repeatedly x make-event)))

(defn- make-report []
  {:command "store report"
   :version 4
   :payload {:puppet-version        "3.7.2 (Puppet Enterprise 3.7.0-rc2-18-gff57637)"
             :report-format         4
             :start-time            (make-timestamp)
             :end-time              (make-timestamp)
             :transaction-uuid      (UUID/randomUUID)
             :status                (get report-statuses (rand-int (count report-statuses)))
             :environment           (get environments (rand-int (count environments)))
             :configuration-version "1421424242"
             :certname              (format "agent%06d" (swap! id inc))
             :resource-events       (make-events (rand-int 20))}})

; curl -X POST
; -H "Accept: application/json"
;  -H "Content-Type: application/json"
;  http://pe:8080/v4/commands
; -d @/Users/scott/Desktop/test-report-submission.json

(defn- push-report []
  (let [report (make-report)]
    (println (get-in report [:payload :transaction-uuid]))
    (http/post "http://pe:8080/v4/commands"
               {:headers
                      {"Accept"       "application/json"
                       "Content-Type" "application/json"}
                :body (json/encode report)}))
  )

(defn -main
  "Launches Avalunche"
  [& args]
  (let [count (read-string (first args))]
    (println "Pushing" count "reports into http://pe:8080")
    (doall
      (repeatedly count push-report))
    (println "Finished")))