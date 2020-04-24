# Seltzer
A Burp Suite extension for mass site scanning.

# Content
- [Description](#Description)
- [seltzer.jar](#seltzer.jar)
- [seltzer.sh](#seltzer.sh)
- [BURPHOME](#BURPHOME)
- [Burp REST API](#Burp-REST-API)
- [Configuration Files](#Configuration-Files)
- [Folder Structure](#Folder-Structure)
- [Targets File](#Targets-File)
- [Examples](#Examples)
- [Installation](#Installation)
- [Download](#Download)
- [Build](#Build-seltzer.jar)

## Description

Seltzer parses a list of targets and sends them to the Burp Suite REST API for scanning.
<br/>It opens an instance of Burp Suite in headless mode, passing user and project options found in the seltzer /conf directory.
<br/>The targets file is a CSV file containing a list of targets to scan and scan configuration parameters.
<br/>Targets are scanned serially, one scan starting after the previous scan has completed.
<br/>A project file, HTML report and XML export for each target is saved into the Seltzer /scans directory.
<br/>A log file is created in the Seltzer /log directory.  New log files are created daily.
<br/>The two primary components of Seltzer are the seltzer.jar Burp extension and the seltzer.sh Bash shell script.

## seltzer.jar

seltzer.jar is the Burp extension that handles creating the scan, monitoring its progress and exporting the results.
<br/>Burp Suite must be configured to use the seltzer.jar file included with this project.
<br/>The extension should be configured to direct all output to the console.

## seltzer.sh
seltzer.sh is a Bash shell script that handles reading the targets file, starting Burp, creating the log file and exiting Burp.

setlzer.sh accepts the following options:

-t, --targets	The targets file - REQUIRED.
<br/>-s, --server	REST API host and port.  Defaults to http://<span>127.0.0.1:1337</span> - OPTIONAL.
<br/>-a, --apikey	REST API key - OPTIONAL.
<br/>-h, --help	Displays help and exits.

## BURPHOME

Seltzer requires a local environment variable named BURPHOME to locate your Burp Suite JAR file.
<br/>If this environment variable is not set seltzer will instruct the user to set it and exit.
<br/>Example: export BURPHOME=/home/myuser/BurpSuite.

## Burp REST API

The Burp REST API must be configured and running.
<br/>Seltzer supports the use of API keys and running the REST API on non-default interfaces and ports.

## Configuration Files

By default, seltzer uses the useroptions.json and projectoptions.json files located in the seltzer /conf directory.
<br/>These files can be modified or replaced.
<br/>In order to not use these options files, modify seltzer.sh and remove the --config-file and --user-config-file parameters passed to Burp.

## Folder Structure

Seltzer uses multiple files and folders including the following:

/bin - Contains the seltzer.sh BASH script.
<br/>/conf - Contains configuration files seltzer needs including Burp Suite user and project options files.
<br/>/doc - Contains documentation including this README.
<br/>/log - Seltzer logs all scanning activities and will save log files to this directory.
<br/>/scans - Seltzer outputs all scan reports and project files to this directory.
<br/>/source - Contains seltzer.jar, gradle components and related Java source code files.
<br/>/targets - Contains targets files.

## Targets File

The targets file is a list of scanning targets in CSV format.
<br/>Parameters in the targets file must be in the correct order.
<br/>The PROJECT parameter may unique or may be reused to have multiple targets in a single project file.
<br/>The REPORTNAME parameter must be unique for each target or the exported files will get overwritten.
<br/>The RESOURCE parameter is experiemental and should only be used if you have created a named resource pool in an existing Burp project file that exists inside the Seltzer /scans directory.  The default "Default resource pool" can be used or the entry can be left blank.
<br/>An empty field (i.e. ',,') should be used for parameters that wil not be passed.
<br/>
<br/>The targets file supports the following parameters in the order listed:

TARGET,REPORTNAME,USERNAME,PASSWORD,CONFIGURATION,RESOURCE

TARGET		The individual target to scan - REQUIRED.
<br/>PROJECT	The name of the Burp project file use - REQUIRED.
<br/>REPORTNAME	The name used to create the report files - MUST BE UNIQUE PER TARGET - REQUIRED.
<br/>USERNAME	The username for credentialed scans - OPTIONAL.
<br/>PASSWORD	The password for credentialed scans - OPTIONAL.
<br/>CONFIGURATION	The named configuration to use for the scan.  Only named configurations are supported at this time - OPTIONAL.
<br/>RESOURCE	The resource pool to use for the scan.  Only named configurations are supported at this time - OPTIONAL.

Example targets file:

<br/>http://<span>192.168.1.102</span>,BurpReport2,,,Audit checks - light active,Default resource pool
<br/>http://<span>192.168.1.102</span>,BurpReport3,someuser,somepass,Audit checks - light active,Default resource pool
<br/>http://<span>192.168.1.103:9090</span>,BurpReport5,someuser,somepass,Audit checks - light active,Default resource pool

## Examples

Run seltzer with only a targets file:
<br/>./seltzer.sh -t ../targets/targets.txt

Run seltzer using the REST API on a non-default port and with a targets file:
<br/>./seltzer.sh -s http://<span>127.0.0.1:4444</span> -t ../targets/targets.txt

Run seltzer using the REST API on a non-default port, with an API key and with a targets file:
<br/>./seltzer.sh -s http://<span>127.0.0.1:4444</span> -a PjmM7JKWPtLa4YKnI2UoR5BTaCosXdgrS -t ../targets/targets.txt

## Installation 

1. Download and extract the pre-compiled binaries or build them from source.  The latest release of seltzer can be at https://github.com/10goto20/seltzer/releases.
2. Configure Burp to use the seltzer extension.  Configure the extension to direct all output to the console.

## Download

The most recent release can be found here: https://github.com/10goto20/seltzer/releases

## Build seltzer.jar

1. git clone https://github.com/10goto20/seltzer.git
2. Install gradle for your distribution (https://gradle.org/install/)
3. cd seltzer
4. gradle build
5. Jar file will be in the build/libs directory

