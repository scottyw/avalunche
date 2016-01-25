# Avalunche

Thanks for looking at Avalunche. The project is no longer supported. For all your PDB load testing needs please check out the official PDB load testing tool here:

https://docs.puppetlabs.com/puppetdb/latest/load_testing_tool.html#running-the-load-testing-tool

If you're using Puppet Enterprise then you can run the tool against the default facts/catalogs/reports something like this:

    unzip -q /opt/puppetlabs/server/apps/puppetdb/puppetdb.jar benchmark/*
    /opt/puppetlabs/server/apps/java/lib/jvm/java/bin/java
        -cp /opt/puppetlabs/server/apps/puppetdb/puppetdb.jar clojure.main
        -m puppetlabs.puppetdb.cli.benchmark
        --config /etc/puppetlabs/puppetdb/conf.d/
        -C benchmark/samples/catalogs
        -R benchmark/samples/reports/
        -F benchmark/samples/facts/
        -n 3
        -N 12

This last bit:

    "-n 3 -N 12"

means:

    "give me 12 total reports across 3 nodes"

The last version of Avalunche is available in the `final` branch if you've a particular need for it.
