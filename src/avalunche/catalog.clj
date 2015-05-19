(ns avalunche.catalog)

(defn catalog
  [name environment config-version uuid producer_timestamp]
  {:certname           name
   :environment        environment
   :version            config-version
   :transaction_uuid   uuid
   :producer_timestamp producer_timestamp
   :edges              [{:relationship "contains"
                         :target       {:title "Service[1]"
                                        :type  "Service"}
                         :source       {:title "File[2]"
                                        :type  "File"}}
                        {:relationship "contains"
                         :target       {:title "Service[2]"
                                        :type  "Service"}
                         :source       {:title "File[2]"
                                        :type  "File"}}]
   :resources          [{:exported   false
                         :title      "Service[1]"
                         :line       60
                         :parameters {"group"  "root"
                                      :mode    "0644",
                                      :content "Package: *\nPin: origin \"apt.puppetlabs.com\"\nPin-Priority: 900\n",
                                      :ensure  "present",
                                      :owner   "root"}
                         :tags       ["role::server", "server"]
                         :type       "Service"
                         :file       "/Users/projects/manifests/foo1.pp"}
                        {:exported   false
                         :title      "File[2]"
                         :line       260
                         :parameters {"group"  "root"
                                      :mode    "0644",
                                      :content "Package: *\nPin: origin \"apt.puppetlabs.com\"\nPin-Priority: 900\n",
                                      :ensure  "present",
                                      :owner   "root"}
                         :tags       ["role::server", "server"]
                         :type       "File"
                         :file       "/Users/projects/manifests/foo2.pp"}
                        {:exported   false
                         :title      "Service[2]"
                         :line       360
                         :parameters {"group"  "root"
                                      :mode    "0644",
                                      :content "Package: *\nPin: origin \"apt.puppetlabs.com\"\nPin-Priority: 900\n",
                                      :ensure  "present",
                                      :owner   "root"}
                         :tags       ["role::server", "server"]
                         :type       "Service"
                         :file       "/Users/projects/manifests/foo3.pp"}]})
