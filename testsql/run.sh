#!/bin/bash
SERVER="oracle"
PASSWORD="passw0rd"
USER="system"

javac generatesql.java 
java generatesql inittable > createtable.sql
java generatesql drop > drop.sql

java generatesql list 20 10000 > list20_10000a.sql
java generatesql odcivarchar2list 20 10000 > odcivarchar2list20_10000a.sql

java generatesql list 200 1 > list200_1.sql
java generatesql odcivarchar2list 200 1 > odcivarchar2list200_1.sql

for a in a b c d e f g h i j ; do
java generatesql list 200 1000 > list200_1000${a}.sql
java generatesql odcivarchar2list 200 1000 > odcivarchar2list200_1000${a}.sql
done

for a in a b c d e f g h i j ; do
java generatesql list 2000 100 > list2000_100${a}.sql
java generatesql odcivarchar2list 2000 100 > odcivarchar2list2000_100${a}.sql
done

(cat createtable.sql ; echo exit)|sqlplus ${USER}/${PASSWORD}@//${SERVER}:1521/XEPDB1 > /dev/null

echo "Using sys.odcivarchar2list:"
for a in a b c d e f g h i j ; do
time (cat odcivarchar2list200_1000${a}.sql ; echo exit)|sqlplus ${USER}/${PASSWORD}@//${SERVER}:1521/XEPDB1 > /dev/null
done

echo "Using list:"
for a in a b c d e f g h i j ; do
time (cat list200_1000${a}.sql ; echo exit)|sqlplus ${USER}/${PASSWORD}@//${SERVER}:1521/XEPDB1 > /dev/null
done

(cat drop.sql ; echo exit)|sqlplus ${USER}/${PASSWORD}@//${SERVER}:1521/XEPDB1 > /dev/null

