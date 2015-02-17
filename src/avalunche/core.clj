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

(defn- make-report [name environment uuid config-version]
  {:command "store report"
   :version 4
   :payload {:puppet-version        "3.7.2 (Puppet Enterprise 3.7.0-rc2-18-gff57637)"
             :report-format         4
             :start-time            (make-timestamp)
             :end-time              (make-timestamp)
             :transaction-uuid      uuid
             :status                (get report-statuses (rand-int (count report-statuses)))
             :environment           environment
             :configuration-version config-version
             :certname              name
             :resource-events       (make-events (rand-int 20))}})

(defn- make-facts [name environment]
  {:command "replace facts"
   :version 3
   :payload {:name               name
             :environment        environment
             :producer-timestamp (make-timestamp)
             :values             {"foo" "bar"}}})


(defn- make-catalog [name environment uuid config-version]
  {:command "replace catalog"
   :version 5
   :payload {:name               name
             :environment        environment
             :version            config-version
             :transaction-uuid   uuid
             :producer-timestamp (make-timestamp)
             :edges              {}
             :resources          [{:exported   false
                                   :title      "/etc/apt/preferences.d/puppetlabs.pref"
                                   :line       60
                                   :parameters {
                                                "group"  "root"
                                                :mode    "0644",
                                                :content "Package: *\nPin: origin \"apt.puppetlabs.com\"\nPin-Priority: 900\n",
                                                :ensure  "present",
                                                :owner   "root"
                                                }
                                   :tags       ["file", "apt::pin", "apt", "pin", "puppetlabs", "class", "os::linux::debian", "os", "linux", "debian", "os::linux", "role::base", "role", "base", "role::server", "server", "node", "myhost.localdomain"]
                                   :type       "File"
                                   :file       "/Users/nicklewis/projects/puppetlabs-modules/dist/apt/manifests/pin.pp"}]}})

(defn- post-command [pdb]
  (let [name (format "agent%06d" (mod (swap! id inc) 200))
        environment (get environments (rand-int (count environments)))
        uuid (UUID/randomUUID)
        config-version (str (quot (.getTime (Date.)) 1000))
        facts (make-facts name environment)
        catalog (make-catalog name environment uuid config-version)
        report (make-report name environment uuid config-version)]
    (println (get-in report [:payload :transaction-uuid]))
    (http/post (str pdb "/v4/commands")
               {:headers
                      {"Accept"       "application/json"
                       "Content-Type" "application/json"}
                :body (json/encode facts)})
    (http/post (str pdb "/v4/commands")
               {:headers
                      {"Accept"       "application/json"
                       "Content-Type" "application/json"}
                :body (json/encode catalog)})
    (http/post (str pdb "/v4/commands")
               {:headers
                      {"Accept"       "application/json"
                       "Content-Type" "application/json"}
                :body (json/encode report)})))

(defn -main
  "Launches Avalunche"
  [& args]
  {:pre [(= 2 (count args))
         (every? string? args)]}
  (let [pdb (first args)
        count (read-string (second args))]
    (println "Pushing" count "reports into" pdb)
    (doall
      (repeatedly count #(post-command pdb)))
    (println "Finished")))