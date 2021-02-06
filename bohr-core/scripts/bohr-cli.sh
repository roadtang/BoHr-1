#!/bin/sh

# change work directory
cd "$(dirname "$0")"

# default JVM options
jvm_options=`java -cp bohr.jar org.bohr.JvmOptions --cli`

# start kernel
java ${jvm_options} -cp bohr.jar org.bohr.Main --cli "$@"
