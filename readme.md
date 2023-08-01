# SafeFBDC Programmable Architecture Example Application

This project is forked from the [github project](https://github.com/safeFBDC-TU-Darmstadt/rescala-rdt-data-architecture-demonstrator) and is intended to use along with the project - [sgx4ml-Python-priv](https://github.com/gagandeep987123/sgx4ml-Python-priv) which should be cloned in the same directory as this project.

As discussed in the HAP1 architecture document, there are multiple possible communication architectures viable for data safe data processing. Which architecture to use, depends on the chosen organization structure and operational constraints.

This case study demonstrates how the REScala project and replicated data types can be used to design an interactive application that makes use of one or more services within an arbitrary ad-hoc network. The services available for demonstration are deliberately simple as the focus is on the flexible design of the network communication.

Specifically, the supported services allow:
* Executing a remote command and displaying its result.
  Concretely, we use the `fortunes` command, which displays a random quote.
* Querying remote database. The database used is SQLite, we do not remotely connect to the database, but rather use our infrastructure to manage the request and execute it locally.

More exciting is the network communication part. The application itself is available for the Web and as a command line executable to be run on laptops or servers. The command line application is able to accept and create connections via TCP and websockets, while the Web application may only use websockets. Instances of the application can form arbitrary network topologies, such as client/server, full mesh, trees, or ring routing. The instances will automatically coordinate to ensure that each instance has a consistent and up-to-date state. Consistency between all instances is guaranteed, as long as the instances are at all possible to communicate (over arbitrary paths).

Within this network, each command line client automatically provides one or both of the above services if the necessary command or database file are found. Each Web client visualizes which services are available where and allow executing new tasks. For demonstration purposes, all Web clients show the same results demonstrating collaborative use.

## Installation

* Install sbt https://www.scala-sbt.org/
* Install a JDK (~17)
* Optionally, install the `fortune` binary (e.g., using `apt install fortune-mod`)
* Optionally, download the `northwind.db` [here](https://github.com/jpwhite3/northwind-SQLite3) and place it in this folder.
* Run the included `launch-server.sh` which uses sbt to compile the application and the starts an instance of the commandline application.
* Optionally, launch more instances of the commandline client using `java --class-path "jvm/target/jars/*" replication.cli conn --tcp-connect 127.0.0.1:3005` (adapt accordingly).
  Use `java --class-path "jvm/target/jars/*" replication.cli conn --help` to get an overview of availbale options.
* Connect to http://127.0.0.1:3004/ to access the Web application. Press the connect button to connect to the local commandline application. If any services are within the network, they will provide the `get fortune` and `query northwind` buttons.

## Notes

Only a single global result per service is considered. If multiple instances provide the same services they can be executed/queried independently, but the result they provide will override any other prior result of the same service. (Even if the UI does display the result per instance).

For demonstration purposes, routing of messages beyond direct neighbors has a very high communication delay (10 seconds). This allows starting multiple requests simultaneously, adding and removing various connections, and confirming that the end result is still consistent.

The overall infrastructure works in such a way that each client can store each step of the history. This is useful to roll back to a prior state, or to audit what happened. History states are stored in an efficient delta format, but can still be disabled if undesirable.

The application state of the shown application is very simple. However, the provided infrastructure is able to handle arbitrary “data classes” (product types).




