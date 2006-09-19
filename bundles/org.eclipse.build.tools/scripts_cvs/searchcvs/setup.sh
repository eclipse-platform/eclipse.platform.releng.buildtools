#!/bin/bash

# setup script for use with Neil Skrypuch's Search CVS tool
# used to check out cvs content so that it can be logged by parsecvs.sh

checkoutdir="$HOME/searchcvs/cvssrc";
quietcvs="-Q";

mkdir -p $checkoutdir;

checkoutProjects () 
{
  CVSROOT=$1;
  project=$2;
  cd $checkoutdir;
  echo "[`date +%H:%M:%S`] Check out $checkoutdir/$project from $CVSROOT ...";
  cvs -d :pserver:anonymous@dev.eclipse.org:$CVSROOT $quietcvs co $project;
}

# list projects to extract; add other cvs roots (eg. /cvsroot/modeling) as necessary below
webprojects="emf emft uml2";
toolsprojects="org.eclipse.emf org.eclipse.xsd org.eclipse.emf.ecore.sdo org.eclipse.emf.releng.build org.eclipse.uml2 org.eclipse.uml2.releng";
techprojects="org.eclipse.emft org.eclipse.gmf";

# do checkouts for each cvs root's projects
for f in $webprojects;   do checkoutProjects /cvsroot/org.eclipse  www/$f; done
for f in $toolsprojects; do checkoutProjects /cvsroot/tools            $f; done
for f in $techprojects;  do checkoutProjects /cvsroot/technology       $f; done

echo "[`date +%H:%M:%S`] Done.";
