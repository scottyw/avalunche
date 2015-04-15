(ns avalunche.core
  (:require [avalunche.facts :refer [facts]]
            [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-time.format :as time-fmt]
            [puppetlabs.http.client.sync :as http])
  (:import (java.util UUID)
           (java.util Date)
           (org.joda.time DateTime DateTimeZone)))

;; Configuration options

(def average-events-per-report 10)                          ; On average, if a report is not unchanged it will have this many events

(def average-logs-per-report 10)                            ; On average, if a report is not unchanged it will have this many events

;;

(def agent-id (atom 0))

(def report-statuses ["changed", "noop", "failed"])

(def environments ["production", "development", "test", "staging"])

(defn- make-timestamp
  [ts]
  (time-fmt/unparse
    (:date-time time-fmt/formatters)
    (DateTime. ts
               #^DateTimeZone time/utc)))


(defn- facts-command
  [name environment ts]
  {:command "replace facts"
   :version 4
   :payload {:certname           name
             :environment        environment
             :producer_timestamp (make-timestamp ts)
             :values             (facts)}})

(defn- catalog-command
  [name environment uuid config-version ts]
  {:command "replace catalog"
   :version 6
   :payload {:certname           name
             :environment        environment
             :version            config-version
             :transaction_uuid   uuid
             :producer_timestamp (make-timestamp ts)
             :edges              {}
             :resources          [{:exported   false
                                   :title      "/etc/apt/preferences.d/puppetlabs.pref"
                                   :line       60
                                   :parameters {"group"  "root"
                                                :mode    "0644",
                                                :content "Package: *\nPin: origin \"apt.puppetlabs.com\"\nPin-Priority: 900\n",
                                                :ensure  "present",
                                                :owner   "root"}
                                   :tags       ["file", "apt::pin", "apt", "pin", "puppetlabs", "class", "os::linux::debian", "os", "linux", "debian", "os::linux", "role::base", "role", "base", "role::server", "server", "node", "myhost.localdomain"]
                                   :type       "File"
                                   :file       "/Users/projects/manifests/foo.pp"}]}})

(defn- make-event
  [report-status current-ts]
  (let [file (str "/var/log/foo/" (UUID/randomUUID) " .log")
        event-statuses (case report-status
                         "noop" ["unchanged", "noop"]
                         "unchanged" ["unchanged"]
                         "changed" ["unchanged", "changed"]
                         "failed" ["unchanged", "changed", "skipped", "failed"])]
    {:containment_path ["Stage[main]" "Puppet_enterprise::Server::Logs" (str "File[" file "]")]
     :new_value        "present"
     :resource_title   file
     :property         "ensure"
     :file             "/opt/puppet/share/puppet/manifests/logs.pp"
     :old_value        "absent"
     :line             (inc (rand-int 200))
     :status           (get event-statuses (rand-int (count event-statuses)))
     :resource_type    "File"
     :timestamp        (make-timestamp current-ts)
     :message          "blah blah blah something happened"}))

(defn- make-events
  [report-status current-ts]
  (let [event-count (case report-status
                      "unchanged" 6
                      (inc (rand-int (* 2 average-events-per-report))))]
    (vec (repeatedly event-count #(make-event report-status current-ts)))))

(defn- make-metrics
  [noop?]
  (vec
    (concat
      [{:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "anchor"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "config_retrieval"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "exec"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "file"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "filebucket"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "gnupg_key"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "ini_setting"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "notify"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "package"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "schedule"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "service"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "total"}
       {:category "time"
        :value    (float (/ (rand-int 100) (inc (rand-int 20))))
        :name     "vcsrepo"}
       {:category "resources"
        :value    (rand-int 100)
        :name     "changed"}
       {:category "resources"
        :value    (rand-int 100)
        :name     "failed"}
       {:category "resources"
        :value    (rand-int 100)
        :name     "failed_to_restart"}
       {:category "resources"
        :value    (rand-int 100)
        :name     "out_of_sync"}
       {:category "resources"
        :value    (rand-int 100)
        :name     "restarted"}
       {:category "resources"
        :value    (rand-int 100)
        :name     "scheduled"}
       {:category "resources"
        :value    (rand-int 100)
        :name     "skipped"}
       {:category "resources"
        :value    (rand-int 100)
        :name     "total"}
       {:category "changes"
        :value    (rand-int 100)
        :name     "total"
        }]
      (if noop?
        (let [noop-count (rand-int 100)]
          [{:category "events"
            :value    0
            :name     "failure"}
           {:category "events"
            :value    0
            :name     "success"}
           {:category "events"
            :value    noop-count
            :name     "noop"}
           {:category "events"
            :value    noop-count
            :name     "total"}])
        (let [failure-count (rand-int 100)
              success-count (rand-int 100)
              total-count (+ failure-count success-count)
              ]
          [{:category "events"
            :value    failure-count
            :name     "failure"}
           {:category "events"
            :value    success-count
            :name     "success"}
           {:category "events"
            :value    total-count
            :name     "total"}])))))

(defn- make-log
  [current-ts]
  (let [file (str "/var/log/foo/" (UUID/randomUUID) ".log")]
    {:file    file
     :line    (inc (rand-int 200))
     :level   "info"
     :message "This is a log message that says all is well"
     :source  "/opt/puppet/share/puppet/manifests/logs.pp"
     :tags    ["tag1", "tag2"]
     :time    (make-timestamp current-ts)}))

(defn- make-logs
  [report-status current-ts]
  (let [log-count (case report-status
                    "unchanged" 6
                    (+ 6 (rand-int (* 2 average-logs-per-report))))]
    (vec (repeatedly log-count #(make-log current-ts)))))

(defn- make-report-status
  [noop?]
  (if noop? "noop")
  (if (< (rand-int 100) 95)                                 ; 95% of reports are unchanged
    "unchanged"
    (get report-statuses (rand-int (count report-statuses)))))

(defn- noop?
  []
  (< (rand-int 100) 5))                                     ; 5% of reports are noop

(defn- report-command
  [name environment uuid config-version ts]
  (let [current-ts @ts
        noop? (noop?)
        report-status (make-report-status noop?)]
    (swap! ts #(- % 1800000))
    {:command "store report"
     :version 5
     :payload {:puppet_version        "4.0.0 (Puppet Enterprise Shallow Gravy man!)"
               :report_format         5
               :end_time              (make-timestamp current-ts)
               :start_time            (make-timestamp (- current-ts 1000))
               :transaction_uuid      uuid
               :status                report-status
               :environment           environment
               :configuration_version config-version
               :certname              name
               :resource_events       (make-events report-status current-ts)
               :metrics               (make-metrics noop?)
               :logs                  (make-logs report-status current-ts)
               :noop                  noop?}}))

(defn- post-command
  [pdb command]
  (http/post (str pdb "/v4/commands")
             {:headers
                    {"Accept"       "application/json"
                     "Content-Type" "application/json"}
              :body (json/encode command)}))

(defn- generate-agent
  [pdb x]
  (let [name (format "agent%06d" (swap! agent-id inc))
        ts (atom (.getTime (Date.)))
        environment (get environments (rand-int (count environments)))
        uuid (UUID/randomUUID)
        config-version (str (quot (.getTime (Date.)) 1000))]
    (post-command pdb (facts-command name environment @ts))
    (post-command pdb (catalog-command name environment uuid config-version @ts))
    (doall
      (repeatedly x #(post-command pdb (report-command name environment uuid config-version ts))))
    (println "Submitted" x "reports against" name "...")))

(defn -main
  "Launches Avalunche"
  [& args]
  {:pre [(<= 2 (count args) 3)]}                            ; fixme error message here
  (let [node-count (read-string (first args))
        reports-per-node (read-string (second args))
        pdb (if (= 3 (count args))
              (nth args 2)
              "http://localhost:8080")]
    (println "Adding" reports-per-node "reports per node for" node-count "nodes")
    (doall
      (repeatedly node-count #(generate-agent pdb reports-per-node)))
    (println "Finished")))
