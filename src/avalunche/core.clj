(ns avalunche.core
  (:require [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-time.format :as time-fmt]
            [puppetlabs.http.client.sync :as http])
  (:import (java.util UUID)
           (java.util Date)
           (org.joda.time DateTime DateTimeZone)))


;; Configuration options

(def max-agents 50)                                         ; This defines the number of unique certnames that will be used

(def unchanged-report-percentage 95)                        ; This percentage of reports (roughly) will be unchanged

(def average-events-per-report 10)                          ; On average, if a report is not unchanged it will have this many events

(def average-logs-per-report 10)                            ; On average, if a report is not unchanged it will have this many events

;;

(def agent-id (atom 0))

(def counter (atom 0))

(def ts (atom (.getTime (Date.))))

(def report-statuses ["changed", "noop", "failed"])

(def environments ["production", "development", "test", "staging"])

(defn- make-timestamp
  []
  (time-fmt/unparse
    (:date-time time-fmt/formatters)
    (DateTime.
      (swap! ts #(- % 1000))
      #^DateTimeZone time/utc)))

(defn- make-facts
  [name environment]
  {:command "replace facts"
   :version 4
   :payload {:certname           name
             :environment        environment
             :producer_timestamp (make-timestamp)
             :values             {"foo" "bar"}}})

(defn- make-catalog
  [name environment uuid config-version]
  {:command "replace catalog"
   :version 6
   :payload {:certname           name
             :environment        environment
             :version            config-version
             :transaction_uuid   uuid
             :producer_timestamp (make-timestamp)
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
  [report-status]
  (let [file           (str "/var/log/foo/" (UUID/randomUUID) " .log")
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
     :timestamp        (make-timestamp)
     :message          "blah blah blah something happened"}))

(defn- make-events
  [report-status]
  (vec (repeatedly (inc (rand-int (* 2 average-events-per-report))) #(make-event report-status))))

(defn- make-metrics
  []
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
   {:category "events"
    :value    (rand-int 100)
    :name     "failure"}
   {:category "events"
    :value    (rand-int 100)
    :name     "success"}
   {:category "events"
    :value    (rand-int 100)
    :name     "total"}
   {:category "changes"
    :value    (rand-int 100)
    :name     "total"
    }])

(defn- make-log
  []
  (let [file (str "/var/log/foo/" (UUID/randomUUID) " .log")]
    {:file    file
     :line    (inc (rand-int 200))
     :level   "info"
     :message "This is a log message that says all is well"
     :source  "/opt/puppet/share/puppet/manifests/logs.pp"
     :tags    ["tag1", "tag2"]
     :time    (make-timestamp)}))

(defn- make-logs
  [report-status]
  (let [log-count (case report-status
                    "unchanged" 6
                    (+ 6 (rand-int (* 2 average-logs-per-report))))]
    (vec (repeatedly log-count #(make-log)))))

(defn- make-report
  [name environment uuid config-version]
  (let [report-status (if (< (rand-int 100) unchanged-report-percentage)
                        "unchanged"
                        (get report-statuses (rand-int (count report-statuses))))]
    {:command "store report"
     :version 5
     :payload {:puppet_version        "4.0.0 (Puppet Enterprise Shallow Gravy man!"
               :report_format         5
               :start_time            (make-timestamp)
               :end_time              (make-timestamp)
               :transaction_uuid      uuid
               :status                report-status
               :environment           environment
               :configuration_version config-version
               :certname              name
               :resource_events       (make-events report-status)
               :metrics               (make-metrics)
               :logs                  (make-logs report-status)
               :noop                  false}}))

(defn- post-command
  [pdb command]
  (http/post (str pdb "/v4/commands")
    {:headers
           {"Accept"       "application/json"
            "Content-Type" "application/json"}
     :body (json/encode command)}))

(defn- generate
  [pdb]
  (let [current        (swap! counter inc)
        name           (format "agent%06d" (mod (swap! agent-id inc) max-agents))
        environment    (get environments (rand-int (count environments)))
        uuid           (UUID/randomUUID)
        config-version (str (quot (.getTime (Date.)) 1000))]
    (if (<= current max-agents)
      (do
        (post-command pdb (make-facts name environment))
        (post-command pdb (make-catalog name environment uuid config-version))))
    (post-command pdb (make-report name environment uuid config-version))
    (if (= 0 (rem current 100))
      (println "Completed ... " current))))

(defn -main
  "Launches Avalunche"
  [& args]
  {:pre [(<= 1 (count args) 2)]}
  (let [report-count (read-string (first args))
        pdb          (if (= 2 (count args))
                       (second args)
                       "http://localhost:8080")]
    (println "Pushing" report-count "reports into" pdb)
    (doall
      (repeatedly report-count #(generate pdb))))
  (println "Finished"))
