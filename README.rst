=============================
CrateDB CSV Importer
=============================

A plugin for importing `CSV`_ data into CrateDB_.

The plugin allows you to import csv data to a table using the ``COPY FROM`` command

Prerequisites
=============

You must be using CrateDB 0.55 or newer.

A JDK needs to be installed.

On OS X, we recommend using `Oracle's Java`_. If you're using Linux, we
recommend OpenJDK_.

We recommend you use a recent Java 8 version.

Setup
=====

Clone the project::

    $ git clone git@github.com:emilywoods/crate-csv.git

Build the JAR file like so::

    $ ./gradlew jar

Copy the JAR file to CrateDB's plugins directory::

  cp build/libs/crate-csv-<version>.jar <CRATEDB_HOME>/plugins

Here, ``<CRATEDB_HOME>`` is the root of your CrateDB installation.



