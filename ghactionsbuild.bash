#!/bin/bash
set -e
curl https://cdn.azul.com/zulu/bin/zulu6.22.0.3-jdk6.0.119-linux_x64.tar.gz -o zulu6.tar.gz
tar -xzvf zulu6.tar.gz
export JAVA6=$(readlink -f ./zulu6.22.0.3-jdk6.0.119-linux_x64/bin/java)
export JAVAC6=$(readlink -f ./zulu6.22.0.3-jdk6.0.119-linux_x64/bin/javac)
./build.bash
