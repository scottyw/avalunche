(ns avalunche.core
  (:require [avalunche.facts :refer [facts]]
            [avalunche.catalog :refer [catalog]]
            [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-time.format :as time-fmt]
            [puppetlabs.http.client.sync :as http])
  (:import (java.util UUID)
           (java.util Date)
           (org.joda.time DateTime DateTimeZone)))

;; Configuration option

(def average-resource-per-report 100)

;;

(def report-statuses ["changed", "noop", "failed"])

(def environments ["production", "development", "test", "staging"])

(defn make-timestamp
  [ts]
  (time-fmt/unparse
    (:date-time time-fmt/formatters)
    (DateTime. ts
               #^DateTimeZone time/utc)))

(defn- generate-event-status
  [report-status]
  (case report-status
    "unchanged" "unchanged"
    "noop" (get ["unchanged" "noop"] (rand-int 2))
    "changed" (get ["unchanged" "success"] (rand-int 2))
    "failed" (get ["unchanged" "success" "skipped" "failure"] (rand-int 4))))

(defn- required-event-status
  [report-status]
  (case report-status
    "unchanged" "unchanged"
    "noop" "noop"
    "changed" "success"
    "failed" "failure"))

(defn- generate-resources
  [total report-status]
  (let [i (atom 0)]
    (repeatedly total
                (fn []
                  (let [type (get ["Service" "File" "Package"] (rand-int 3))]
                    {:resource_title (str type "[" (swap! i inc) "]")
                     :resource_type  type
                     :status         (if (= @i 1) (required-event-status report-status) (generate-event-status report-status))})))))

(defn- facts-command
  [name environment ts]
  {:command "replace facts"
   :version 4
   :payload {:certname           name
             :environment        environment
             :producer_timestamp (make-timestamp ts)
             :values             (facts)}})

(defn- catalog-command
  [resources name environment uuid config-version ts]
  {:command "replace catalog"
   :version 6
   :payload (catalog resources name environment config-version uuid (make-timestamp ts))})

(def pp-files ["/opt/puppet/share/puppet/manifests/logs1.pp"
               "/opt/puppet/share/puppet/manifests/logs2.pp"
               "/opt/puppet/share/puppet/manifests/logs3.pp"
               "/opt/puppet/share/puppet/manifests/logs4.pp"])

(defn- make-event
  [resource current-ts]
  (let [{:keys [status resource_type resource_title]} resource]
    {:containment_path [(str "Stage[" resource_type "]") (str "Puppet_enterprise::Server::" resource_type) resource_title]
     :new_value        "present"
     :resource_title   resource_title
     :property         "ensure"
     :file             (get pp-files (rand-int (count pp-files)))
     :old_value        "absent"
     :line             (inc (rand-int 200))
     :status           status
     :resource_type    resource_type
     :timestamp        (make-timestamp current-ts)
     :message          "An event occurred"}))

(defn- make-events
  [resources current-ts]
  (->> resources
       (filter #(not= "unchanged" (:status %)))
       (map #(make-event % current-ts))))

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
        :name     "total"}]
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
              total-count (+ failure-count success-count)]
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
                    (+ 1 (rand-int 100)))]
    (vec (repeatedly log-count #(make-log current-ts)))))

(defn- make-report-status
  []
  (if (< (rand-int 100) 10)                                 ; 95% of reports are unchanged
    "unchanged"
    (get report-statuses (rand-int (count report-statuses)))))

(defn- report-command
  [resources report-status name environment uuid config-version ts]
  (let [current-ts @ts
        noop? (= report-status "noop")]
    (swap! ts #(- % 1800000))
    {:command "store report"
     :version 5
     :payload {:puppet_version        "4.0.0 (Puppet Enterprise Shallow Gravy man!)"
               :report_format         5
               :end_time              (make-timestamp current-ts)
               :start_time            (make-timestamp (- current-ts 1000))
               :producer_timestamp    (make-timestamp current-ts)
               :transaction_uuid      uuid
               :status                report-status
               :environment           environment
               :configuration_version config-version
               :certname              name
               :resource_events       (make-events resources current-ts)
               :metrics               (make-metrics noop?)
               :logs                  (make-logs report-status current-ts)
               :noop                  noop?}}))

(defn- post-command
  [pdb command]
  (let [response (http/post (str pdb "/pdb/cmd/v1/commands")
                            {:headers
                                   {"Accept"       "application/json"
                                    "Content-Type" "application/json"}
                             :body (json/encode command)})]
    (if (not= 200 (:status response)) (println "Unexpected response: " response))))

(defn- generate-agent
  [pdb agent-id x now]
  (let [name (format "agent%06d" agent-id)
        ts (atom (- now 60000))                             ; Slightly randomize time
        environment (get environments (rand-int (count environments)))
        uuid (UUID/randomUUID)
        config-version (str (quot (.getTime (Date.)) 1000))
        report-status (make-report-status)
        resources (generate-resources (+ average-resource-per-report (rand-int 80) -40) report-status)]
    (post-command pdb (facts-command name environment @ts))
    (post-command pdb (catalog-command resources name environment uuid config-version @ts))
    (doall
      (repeatedly x #(post-command pdb (report-command resources report-status name environment uuid config-version ts))))
    (println "Submitted" x "reports against" name "...")))

(defn -main
  "Launches Avalunche"
  [& args]
  (if-not (<= 2 (count args) 3)
    (println "Usage: lein run <number-of-distinct-nodes> <number-of-reports-per-nodes> [<optional-puppetdb-prefix>]")
    (let [agent-id (atom 0)
          now (.getTime (Date.))
          node-count (read-string (first args))
          reports-per-node (read-string (second args))
          pdb (if (= 3 (count args))
                (nth args 2)
                "http://localhost:8080")]
      (println "Adding" reports-per-node "reports per node for" node-count "nodes")
      (doall
        (repeatedly node-count #(generate-agent pdb (swap! agent-id inc) reports-per-node now)))
      (println "Finished"))))
