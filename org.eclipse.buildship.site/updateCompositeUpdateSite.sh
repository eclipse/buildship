#!/bin/sh

# count the child directories and create a 'child' node for each one
children=""
numOfChildren=0
for directory in $(LIST_COMMAND) ; do
    children="$children<child location='$directory'/> "
    ((numOfChildren++))
done

# define the repository name and the timestamp
repository='Buildship'
timestamp=$(date +%s)

# create compositeArtifacts.xml
cat > compositeArtifacts.xml <<EOF
<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='$repository' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>
   <properties size='3'>
    <property name='p2.timestamp' value='$timestamp'/>
    <property name='p2.compressed' value='true'/>
    <property name='p2.atomic.composite.loading' value='true'/>
  </properties>
  <children size='$numOfChildren'>
    $children
  </children>
</repository>
EOF

# create compositeContent.xml
cat > compositeContent.xml <<EOF
<?xml version='1.0' encoding='UTF-8'?>
<?compositeMetadataRepository version='1.0.0'?>
<repository name='$repository' type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>
   <properties size='3'>
    <property name='p2.timestamp' value='$timestamp'/>
    <property name='p2.compressed' value='true'/>
    <property name='p2.atomic.composite.loading' value='true'/>
  </properties>
  <children size='$numOfChildren'>
    $children
  </children>
</repository>
EOF

# create p2.index file
cat > p2.index <<"EOF"
version=1
metadata.repository.factory.order=compositeContent.xml,\!
artifact.repository.factory.order=compositeArtifacts.xml,\!
EOF

# package the xml files into jars and delete them
jar cvMf compositeArtifacts.jar compositeArtifacts.xml > /dev/null
jar cvMf compositeContent.jar compositeContent.xml > /dev/null
rm compositeArtifacts.xml
rm compositeContent.xml
