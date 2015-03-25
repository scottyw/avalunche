(ns avalunche.facts
  (:import (java.util UUID)))

(defn facts
  []
  {:architecture              "x86_64"
   :augeasversion             "1.2.0"
   :bios_release_date         "07/31/2013"
   :bios_vendor               "Phoenix Technologies LTD"
   :bios_version              "6.00"
   :blockdevice_sda_model     "VMware Virtual S"
   :blockdevice_sda_size      "21474836480"
   :blockdevice_sda_vendor    "VMware,"
   :blockdevice_sr0_model     "VMware IDE CDR10"
   :blockdevice_sr0_size      "1073741312"
   :blockdevice_sr0_vendor    "NECVMWar"
   :blockdevices              "sda,sr0"
   :boardmanufacturer         "Intel Corporation"
   :boardproductname          "440BX Desktop Reference Platform"
   :boardserialnumber         "None"
   :facterversion             "2.3.0"
   :filesystems               "ext4,iso9660"
   :fqdn                      "pe"
   :gid                       "root"
   :hardwareisa               "x86_64"
   :hardwaremodel             "x86_64"
   :hostname                  "pe"
   :id                        "root"
   :interfaces                "eth0,lo,pan0"
   :ipaddress                 (str "192.168.13." (rand-int 200))
   :ipaddress_eth0            (str "192.168.13." (rand-int 200))
   :ipaddress_lo              "127.0.0.1"
   :is_virtual                "true"
   :kernel                    "Linux"
   :kernelmajversion          "2.6"
   :kernelrelease             "2.6.32-431.29.2.el6.x86_64"
   :kernelversion             "2.6.32"
   :lsbdistcodename           "Final"
   :lsbdistdescription        "CentOS release 6.5 (Final)"
   :lsbdistid                 "CentOS"
   :lsbdistrelease            "6.5"
   :lsbmajdistrelease         "6"
   :lsbminordistrelease       "5"
   :lsbrelease                ":base-4.0-amd64:base-4.0-noarch:core-4.0-amd64:core-4.0-noarch:graphics-4.0-amd64:graphics-4.0-noarch:printing-4.0-amd64:printing-4.0-noarch"
   :macaddress                "00:0C:29:A8:B0:59"
   :macaddress_eth0           "00:0C:29:A8:B0:59"
   :macaddress_pan0           "DA:7B:5D:1B:39:19"
   :manufacturer              "VMware, Inc."
   :memoryfree                (str (rand-int 4096) " MB")
   :memoryfree_mb             (rand-int 4096)
   :memorysize                (str (rand-int 4096) " MB")
   :memorysize_mb             (rand-int 4096)
   :mtu_eth0                  "1500"
   :mtu_lo                    "16436"
   :mtu_pan0                  "1500"
   :netmask                   "255.255.255.0"
   :netmask_eth0              "255.255.255.0"
   :netmask_lo                "255.0.0.0"
   :network_eth0              "192.168.13.0"
   :network_lo                "127.0.0.0"
   :operatingsystem           "CentOS"
   :operatingsystemmajrelease "6"
   :operatingsystemrelease    "6.5"
   :os                        {:name    "CentOS"
                               :family  "RedHat"
                               :release {:major 6
                                         :minor 5
                                         :full  6.5}
                               :lsb     {:distcodename     "Final"
                                         :distid           "CentOS"
                                         :distdescription  "CentOS release 6.5 (Final)"
                                         :release          ":base-4.0-amd64:base-4.0-noarch:core-4.0-amd64:core-4.0-noarch:graphics-4.0-amd64:graphics-4.0-noarch:printing-4.0-amd64:printing-4.0-noarch"
                                         :distrelease      6.5
                                         :majdistrelease   6
                                         :minordistrelease 5}}
   :osfamily                  (get-in (rand-int 7) ["RedHat1", "RedHat2", "RedHat3", "RedHat4", "RedHat5", "RedHat6", "RedHat7"])
   :partitions                {:sda1 {
                                      :uuid       "cf270c46-ea33-46d7-904a-66c8647060e8"
                                      :size       1024000
                                      :mount      "/boot"
                                      :filesystem "ext4"}
                               :sda2 {
                                      :uuid       "cf270c46-ea33-46d7-904a-66c8647060e8"
                                      :size       1024000
                                      :mount      "/boot"
                                      :filesystem "ext4"}
                               }
   :path                      "/usr/lib64/qt-3.3/bin:/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:/root/bin"
   :physicalprocessorcount    "2"
   :processor0                "Intel(R) Core(TM) i7-4870HQ CPU @ 2.50GHz"
   :processor1                "Intel(R) Core(TM) i7-4870HQ CPU @ 2.50GHz"
   :processorcount            "2"
   :processors                {:models        ["Intel (R) Core (TM) i7-4870HQ CPU @2.50GHz"
                                               "Intel (R) Core (TM) i7-4870HQ CPU @2.50GHz"]
                               :count         (rand-int 16)
                               :physicalcount (rand-int 16)}
   :productname               "VMware Virtual Platform"
   :ps                        "ps -ef"
   :puppetversion             "3.7.4 (Puppet Enterprise 3.7.2)"
   :rubyplatform              "x86_64-linux"
   :rubysitedir               "/opt/puppet/lib/ruby/site_ruby/1.9.1"
   :rubyversion               "1.9.3"
   :selinux                   "true"
   :selinux_config_mode       "enforcing"
   :selinux_config_policy     "targeted"
   :selinux_current_mode      "enforcing"
   :selinux_enforced          "true"
   :selinux_policyversion     "24"
   :serialnumber              "VMware-56 4d 8e 9d 30 de b9 f5-c1 3e 26 18 89 a8 b0 59"
   :sshdsakey                 "AAAABc4g/4LQ/8BihkU2N54Ptcl1b+s"
   :sshfp_dsa                 "SSHFP 2 1 298658169b6ca670cfa6ca80da9670be03"
   :sshfp_rsa                 "SSHFP 1 1 656b31ff25d50ff52cb7642a50cf2add91"
   :sshrsakey                 "AAAAWeBULyhsew=="
   :swapfree                  "858.04 MB"
   :swapfree_mb               "858.04"
   :swapsize                  "2.00 GB"
   :swapsize_mb               "2048.00"
   :system_uptime             {:seconds 517184
                               :hours   143
                               :days    5
                               :uptime  "5 days"}
   :timezone                  "GMT"
   :type                      "Other"
   :uniqueid                  "a8c0030d"
   :uptime                    "5 days"
   :uptime_days               5
   :uptime_hours              143
   :uptime_seconds            517184
   :uuid                      (UUID/randomUUID)})