#!/bin/bash

function mirrorArtifact() {
  echo Mirroring artifacts from $1 to $ROOT/eclipse/update-site/mirror/$2
  java -jar  $WD/eclipse/plugins/org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar -application -application org.eclipse.equinox.p2.artifact.repository.mirrorApplication -source $1 -destination $ROOT/eclipse/update-site/mirror/$2 -consoleLog -verbose  
  echo Mirroring $1 artifacts finished
}

function mirrorMetadata() {
  echo Mirroring metadata from $1 to $ROOT/eclipse/update-site/mirror/$2
  java -jar  $WD/eclipse/plugins/org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar -application -application org.eclipse.equinox.p2.metadata.repository.mirrorApplication -source $1 -destination $ROOT/eclipse/update-site/mirror/$2 -consoleLog -verbose  
  echo Mirroring $1 metadata finished
}

function mirror() {
  mirrorMetadata $1 $2
  mirrorArtifact $1 $2
} 

if [ $# -ne 1 ]; then
  echo "The root folder should be a parameter. Terminate."
  exit 1
fi

#
# Set working directory and step into it
#
ROOT=$1
WD=$ROOT/tmp
if [ ! -d "$WD" ]; then
  mkdir $WD
fi
cd $WD

#
# Download simple eclipse to build the update site with
#
ECLIPSE_TAR=eclipse-java-luna-SR1-linux-gtk-x86_64.tar.gz
ECLIPSE_URL=http://ftp.wh2.tu-dresden.de/pub/mirrors/eclipse/technology/epp/downloads/release/luna/SR1/eclipse-java-luna-SR1-linux-gtk-x86_64.tar.gz
if [ -e "$ECLIPSE_TAR" ]; then 
  echo Eclipse already downloaded at $WD/$ECLIPSE_TAR.
else
  wget $ECLIPSE_URL
fi

#
# extract eclipse
#
tar xzf $ECLIPSE_TAR
if [ $? -ne 0 ]; then
  echo "Eclipse download corrupted, removed. Terminate."
  rm -rf $ECLIPSE_TAR
  exit 1
fi

#
# execute update site mirroring
#
mirror http://download.eclipse.org/eclipse/updates/3.6/R-3.6.2-201102101200 release-helios-sr2
mirror http://download.eclipse.org/eclipse/updates/3.7/R-3.7.2-201202080800 release-indigo-sr2
mirror http://download.eclipse.org/eclipse/updates/4.2/R-4.2.2-201302041200 release-juno-sr2
mirror http://download.eclipse.org/eclipse/updates/4.3/R-4.3.2-201402211700 release-kepler-sr2
mirror http://download.eclipse.org/eclipse/updates/4.4/R-4.4.2-201502041700 release-luna-sr2
mirror http://download.eclipse.org/eclipse/updates/4.5milestones/S-4.5RC3-201505280700 release-mars-rc3
mirror http://download.eclipse.org/technology/swtbot/releases/2.2.1 swtbot/release
mirror http://download.eclipse.org/technology/swtbot/helios/dev-build/update-site swtbot/helios-dev
