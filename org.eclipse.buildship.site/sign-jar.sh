#!/bin/bash

set -e

# print instructions
if [ $# -lt 6 ]; then
        echo "Usage: ./sign-jar.sh <host> <username> <password> <knownHostsFile> <jar> <signedJar>"
  exit 1
fi

host=$1
username=$2
password=$3
knownHosts=$4
jar=$5
signedJar=$6

echo "Creating clean working directory"
remoteFolder="tmp/signing"
 sshpass -p "$password" ssh $username@$host -o UserKnownHostsFile=$knownHosts -C "rm -rf $remoteFolder"
 sshpass -p "$password" ssh $username@$host -o UserKnownHostsFile=$knownHosts -C "mkdir -p $remoteFolder"

echo "Uploading $jar to $remoteFolder"
 sshpass -p "$password" scp -o UserKnownHostsFile=$knownHosts $jar $username@$host:$remoteFolder/unsigned.jar

echo "Signing jar"
 sshpass -p "$password" ssh $username@$host -o UserKnownHostsFile=$knownHosts -C "curl -X POST -o $remoteFolder/signed.jar -F file=@$remoteFolder/unsigned.jar https://cbi.eclipse.org/jarsigner/sign"

echo "Downloading signed jar to $signedJar"
 sshpass -p "$password" scp -o UserKnownHostsFile=$knownHosts $username@$host:$remoteFolder/signed.jar $signedJar
 
echo "Cleaning up working directory"
 sshpass -p "$password" ssh $username@$host -o UserKnownHostsFile=$knownHosts -C "rm -rf $remoteFolder"