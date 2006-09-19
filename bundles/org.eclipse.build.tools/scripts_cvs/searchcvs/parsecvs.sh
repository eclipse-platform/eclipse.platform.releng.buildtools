#/bin/bash

CVSOPTS=-Q

cd ${0%/*}; # cd to directory where this script is located; cvssrc/ must be beneath it
for i in $(ls -d cvssrc/*)
do
	echo "[`date +%H:%M:%S`] Processing $i"
	cd $i
	cvs $CVSOPTS up
	cvs $CVSOPTS log | php ../../parsecvs.php
	cd ../..
	echo "[`date +%H:%M:%S`] done."
done
