#!/bin/bash

function usage(){
echo "
Written by Brian Bushnell
Last modified April 30, 2015

Description:  Summarizes data in multiple scafstats files.
Used for calculating cross-contamination rates.

Usage:  summarizecoverage.sh scafstats_*.txt out=summary.txt

Please contact Brian Bushnell at bbushnell@lbl.gov if you encounter any problems.
"
}

pushd . > /dev/null
DIR="${BASH_SOURCE[0]}"
while [ -h "$DIR" ]; do
  cd "$(dirname "$DIR")"
  DIR="$(readlink "$(basename "$DIR")")"
done
cd "$(dirname "$DIR")"
DIR="$(pwd)/"
popd > /dev/null

#DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/"
CP="$DIR""current/"
EA="-ea"
set=0

if [ -z "$1" ] || [[ $1 == -h ]] || [[ $1 == --help ]]; then
	usage
	exit
fi

function summarizecoverage() {
	#module load oracle-jdk/1.7_64bit
	local CMD="java $EA -Xmx120m -cp $CP fileIO.TextFile $@"
	echo $CMD >&2
	eval $CMD
}

summarizecoverage "$@"
