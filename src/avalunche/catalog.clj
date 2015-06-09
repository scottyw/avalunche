(ns avalunche.catalog)

(defn- make-catalog-edge
  [target source]
  {:relationship "contains"
   :target       {:title (:resource_title target)
                  :type  (:resource_type target)}
   :source       {:title (:resource_title source)
                  :type  (:resource_type source)}})

(defn- make-catalog-edges
  [resources]
  (let [root (first resources)
        leaves (rest resources)]
    (vec (map #(make-catalog-edge % root) leaves))))

(defn- make-catalog-resource
  [resource]
  (let [{:keys [resource_type resource_title]} resource]
    {:exported   false
     :title      resource_title
     :line       (rand-int 1000)
     :parameters {"group"  "root"
                  :mode    "0644",
                  :content "Package: *\nPin: origin \"apt.puppetlabs.com\"\nPin-Priority: 900\n",
                  :ensure  "present",
                  :owner   "root"}
     :tags       ["role::server", "server"]
     :type       resource_type
     :file       "/Users/projects/manifests/foo1.pp"}))

(defn- make-catalog-resources
  [resources]
  (vec (map make-catalog-resource resources)))

(defn catalog
  [resources name environment config-version uuid producer_timestamp]
  {:certname           name
   :environment        environment
   :version            config-version
   :transaction_uuid   uuid
   :producer_timestamp producer_timestamp
   :edges              (make-catalog-edges resources)
   :resources          (make-catalog-resources resources)})
