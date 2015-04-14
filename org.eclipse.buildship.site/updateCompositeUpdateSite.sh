#!/bin/sh

# template for compositeArtifacts.xml
read -d '' compositeArtifactsTemplate << "EOF"
<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='REPOSITORY' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>
   <properties size='3'>
    <property name='p2.timestamp' value='TIMESTAMP'/>
    <property name='p2.compressed' value='true'/>
    <property name='p2.atomic.composite.loading' value='true'/>
  </properties>
  <children size='NUMOFCHILDREN'>
    CHILDREN
  </children>
</repository>
EOF

# template for compositeContent.xml
read -d '' compositeContentTemplate << "EOF"
<?xml version='1.0' encoding='UTF-8'?>
<?compositeMetadataRepository version='1.0.0'?>
<repository name='REPOSITORY' type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>
   <properties size='3'>
    <property name='p2.timestamp' value='TIMESTAMP'/>
    <property name='p2.compressed' value='true'/>
    <property name='p2.atomic.composite.loading' value='true'/>
  </properties>
  <children size='NUMOFCHILDREN'>
    CHILDREN
  </children>
</repository>
EOF

# count the child directories and create a 'child' node for each one
children=""
numOfChildren=0
for directory in */ ; do
    children="$children<child location='$directory'/> "
    ((numOfChildren++))
done

# define the repository name and the timestamp to replace them in the templates
repository='Buildship'
timestamp=$(date +%s)

# replace the variables in the templates
echo "Creating composite update site descriptors at $(pwd)"
echo "$compositeArtifactsTemplate" | sed -e "s|NUMOFCHILDREN|$numOfChildren|g" \
     | sed -e "s|CHILDREN|$children|g" \
     | sed -e "s|TIMESTAMP|$timestamp|g" \
     | sed -e "s|REPOSITORY|$repository|g" > compositeArtifacts.xml

echo "$compositeContentTemplate" | sed -e "s|NUMOFCHILDREN|$numOfChildren|g" \
     | sed -e "s|CHILDREN|$children|g" \
     | sed -e "s|TIMESTAMP|$timestamp|g" \
     | sed -e "s|REPOSITORY|$repository|g" > compositeContent.xml

# package the xml files into jars and delete them
jar cvMf compositeArtifacts.jar compositeArtifacts.xml > /dev/null
jar cvMf compositeContent.jar compositeContent.xml > /dev/null
rm compositeArtifacts.xml
rm compositeContent.xml
