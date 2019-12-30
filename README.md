# Seltzer: A Burp Extension for Mass Site Scanning

# Content
- [Description](#Description)
- [seltzer.jar](#seltzer.jar)
- [seltzer.sh](#seltzer.sh)
- [BURPHOME](#BURPHOME)
- [Burp REST API](#Burp-REST-API)
- [Configuration Files](#Configuration-Files)
- [Files and Folders](#Files-and-Folders)
- [Targets File](#Targets-File)
- [Examples](#Examples)
- [Installation](#Installation)
- [Download](#Download)
- [Build](#Build-seltzer.jar)

## Description

Seltzer parses a list of targets and sends them to the Burp Suite REST API for scanning.
It opens an instance of Burp Suite in headless mode, passing user and project options found in the seltzer /conf directory.
The targets file is a CSV file containing a list of targets to scan and scan configuration parameters.
Targets are scanned serially, one scan starting after the previous scan has completed.
A project file, HTML report and XML export for each target is saved into the seltzer /scans directory.
A log file is created in the seltzer /log directory.  New log files are created daily.
The two primary components of seltzer are the seltzer.jar Burp extension and the seltzer.sh Bash shell script.

## seltzer.jar

Seltzer.jar is the Burp extension that handles creating the scan, monitoring its progress and exporting the results.
Burp Suite must be configured to use the seltzer.jar file included with this project.
The extension should be configured to direct all output to the console.

## seltzer.sh
Seltzer.sh is a Bash shell script that handles reading the targets file, starting Burp and creating the log file.

setlzer.sh accepts the following options:

-t, --targets	The targets file - REQUIRED.
-s, --server	REST API host and port.  Defaults to http://127.0.0.1:1337 - OPTIONAL.
-a, --apikey	REST API key - OPTIONAL.
-h, --help	Displays help and exits.

## BURPHOME

Seltzer requires a local environment variable named BURPHOME to locate your Burp Suite JAR file.
If this environment variable is not set seltzer will instruct the user to set it and exit.
Example: export BURPHOME=/home/myuser/BurpSuite.

## Burp REST API

The Burp REST API must be configured and running.
Seltzer supports the use of API keys and running the REST API on non-default interfaces and ports.

## Configuration Files

By default, seltzer uses the useroptions.json and projectoptions.json files located in the seltzer /conf directory.
These files can be modified or replaced.
In order to not use options files, modify seltzer.sh and remove the --config-file and --user-config-file parameters passed to Burp.

## Files and Folders

Seltzer uses multiple files and folders including the following:

/bin		Contains the seltzer.sh BASH script.
/conf		Contains configuration files seltzer needs including Burp Suite user and project options files.
/doc		Contains documentation including this README.
/log		Seltzer logs all scanning activities and will save log files to this directory.
/scans		Seltzer outputs all scan reports and project files to this directory.
/source		Contains the seltzer.jar and related Java source code files.
/targets	Contains targets files.

## Targets File

The targets file is a list of scanning targets in CSV format.
Parameters in the targets file must be in the correct order.
The targets file supports the following parameters in the order listed:

TARGET,REPORTNAME,USERNAME,PASSWORD,CONFIGURATION,RESOURCE

TARGET		The individual target to scan - REQUIRED.
REPORTNAME	The name used to create the report files - MUST BE UNIQUE PER TARGET - REQUIRED.
USERNAME	The username for credentialed scans - OPTIONAL.
PASSWORD	The password for credentialed scans - OPTIONAL.
CONFIGURATION	The named configuration to use for the scan.  Only named configurations are supported at this time - OPTIONAL.
RESOURCE	The resource pool to use for the scan.  Only named configurations are supported at this time - OPTIONAL.

Example targets file:

http://192.168.0.100,BurpReport0
http://192.168.0.101,BurpReport1,someuser,somepass
http://192.168.0.102,BurpReport2,someuser,somepass,Audit checks - light active,Default resource pool

## Examples

Run seltzer with only a targets file:
./seltzer.sh -t ../targets/targets.txt

Run seltzer using the REST API on a non-default port and with a targets file:
./seltzer.sh -s http://127.0.0.1:4444 -t ../targets/targets.txt

Run seltzer using the REST API on a non-default port, with an API key and with a targets file:
./seltzer.sh -s http://127.0.0.1:4444 -a PjmM7JKWPtLa4YKnI2UoR5BTaCosXdgrS -t ../targets/targets.txt

## Installation 

1: Download and extract the pre-compiled binaries or build them from source.  The latest release of seltzer can be at https://github.com/10goto20/seltzer/releases.

1: Configure Burp to use the seltzer extension.  Configure the extension to direct all output to the console.

1: Download and install the 

## Download

The most recent tar file can be found in the releases https://github.com/10goto20/seltzer/releases

## Build seltzer.jar

1. git clone https://github.com/10goto20/seltzer.git
2. Install gradle for your distribution (https://gradle.org/install/)
3. cd seltzer
4. gradle build
5. Jar file will be in the build/libs directory

