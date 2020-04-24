#!/bin/bash

usage()
{
	echo ""
	echo "Usage: seltzer.sh [OPTIONS]"
	echo "Seltzer parses a list of targets and sends them to the Burp Suite REST API for scanning."
	echo "It will save a project file and export all scanning results in HTML and XML formats for each scan performed."
	echo ""
	echo "-s, --server	REST API host and port.  Defaults to http://127.0.0.1:1337 - OPTIONAL."
	echo "-a, --apikey	REST API key - OPTIONAL."
	echo "-t, --targets	The targets file - REQUIRED."
	echo "-h, --help	Display this help and exit."
	echo ""
	exit 1
}

if [ -z "$1" ]; then
	usage
fi

# check for BURPHOME environment variable
if [[ -z "${BURPHOME}" ]]; then
	echo ""
	echo [`date "+%F %T %Z"`][seltzer] BURPHOME is not set
	echo [`date "+%F %T %Z"`][seltzer] please set BURPHOME to your Burp Suite installation directory \(e.x. export BURPHOME=/home/myuser/BurpSuite\)
	echo [`date "+%F %T %Z"`][seltzer] exiting
	echo ""
	exit 1
fi

# command line arguements
while [ "$1" != "" ]; do
    case $1 in
        -s | --server )    	shift
				server=$1
                                ;;
        -a | --apikey )    	shift
				apikey=$1
                                ;;
        -t | --targets )        shift
                                targets=$1
                                ;;
        -h | --help )           usage
                                exit
    esac
    shift
done

# pathing
conf_dir="$(dirname $(dirname $(realpath $0)) )/conf"
log_dir="$(dirname $(dirname $(realpath $0)) )/log"
scans_dir="$(dirname $(dirname $(realpath $0)) )/scans"

# create log file and redirect copy of stdout and stderr
touch $log_dir/seltzerlog_`date "+%F"`.log;
exec > >(tee -a -i $log_dir/seltzerlog_`date "+%F"`.log);
exec 2>&1;

# confirm targets file was specified
if [ -z "$targets" ]; then
	echo ""
	echo [`date "+%F %T %Z"`][seltzer] -t, --targets is required
	echo [`date "+%F %T %Z"`][seltzer] exiting
	echo ""
	exit 1
fi

# check if targets file exists
if [ ! -f "$targets" ]; then
	echo ""
	echo [`date "+%F %T %Z"`][seltzer] specified targets file not found
	echo [`date "+%F %T %Z"`][seltzer] exiting
	echo ""
	exit 1
fi

# set default value for server if no server specified
if [ -z "$server" ]; then
	server="http://127.0.0.1:1337"
fi

# set default value for apikey if no apikey specified
if [ -z "$apikey" ]; then
	apikey="none"
fi

# start scans
echo ""
echo [`date "+%F %T %Z"`][seltzer] starting

#INPUT="$targets"
OLDIFS=$IFS
IFS=','
while read target project report username password config resource
do

if [ -z "$target" ]; then
	echo ""
	echo [`date "+%F %T %Z"`][seltzer] bad target specification...
	echo ""
	break
fi

if [ -z "$project" ]; then
	echo ""
	echo [`date "+%F %T %Z"`][seltzer] bad project specification...
	echo ""
	break
fi

if [ -z "$username" ]; then
	username="notprovided";
fi

if [ -z "$password" ]; then
	password="notprovided";
fi

if [ -z "$config" ]; then
	config="notprovided";
fi

if [ -z "$resource" ]; then
	resource="notprovided";
fi

	$BURPHOME/jre/bin/java -jar --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.desktop/javax.swing=ALL-UNNAMED -Xmx1024m -Djava.awt.headless=true  $BURPHOME/burpsuite_pro.jar --project-file=$scans_dir/$project --config-file=$conf_dir/projectoptions.json --user-config-file=$conf_dir/useroptions.json --unpause-spider-and-scanner $server $apikey $target $scans_dir/$report $username $password $config $resource
done < $targets
IFS=$OLDIFS

echo ""
echo [`date "+%F %T %Z"`][seltzer] finished
echo ""
