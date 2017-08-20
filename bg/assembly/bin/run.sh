#! /bin/sh

# add libs to CLASSPATH
for f in lib/*.jar; do
    CLASSPATH=${CLASSPATH}:$f;
done
for f in build/*.jar; do
    CLASSPATH=${CLASSPATH}:$f;
done
CLASSPATH=${CLASSPATH}:lib/classes;
CLASSPATH=${CLASSPATH}:conf;

export CLASSPATH

java -Xms512m -Xmx512m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps com.github.apache9.wxbot.Driver $@