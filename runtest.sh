#!/bin/bash

USER=$1
PASSWORD=$2
TEST=$3


time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' ${USER} ${PASSWORD} inittable

for a in $(seq 1  10) ; do
  time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' ${USER} ${PASSWORD} test${TEST}
done

time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' ${USER} ${PASSWORD} reset



