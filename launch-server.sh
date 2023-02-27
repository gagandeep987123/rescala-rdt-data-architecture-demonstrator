#!/bin/bash

sbtn "replicationExamplesJS/deploy; replicationExamplesJVM/stageJars"
java --class-path "jvm/target/jars/*" replication.cli conn --webserver-listen-port 3004 --webserver-static-path js/target/scala-3.2.2/replicationexamples-fastopt --northwind-path "northwind.db" --tcp-listen-port 3005
