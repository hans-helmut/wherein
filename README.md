'' Testing WHERE IN <LIST> performance

This code joins two tables, and then selecting small parts using `WHERE IN`.

* DB was restartet before the test
* Test creates two tables, 2^20 and 2^16 lines
* Test runs 10 times
* Each test runs with different lengths of list
* Test deletes tables

* IDs are pseudo-random to avoid improper caching
* SQLs are running until 30 seconds reached, so take care, that nothing is missed.

Results on my PCs, including times for creating and deleting tables. Detailed output in result*.txt. (Test 1 and 2 do not use `JOIN`, and not in scope)

Test 3: WHERE (class IN ('def','jkl')) AND ((test.id IN ('s1', ... ) OR (test.id IN ('s1000'...))

```
$ time ./runtest.sh system password 3 >& result3_IN_LIST.txt

real	10m18.263s
user	0m53.939s
sys	0m23.220s
```

Test 4: Creating a temporary table and JOIN

```
time ./runtest.sh system password 4 >& result4_temptable.txt

real	7m19.898s
user	0m52.479s
sys	0m23.118s
```

Test 5: WHERE (class IN (SELECT * FROM sys.odcivarchar2list('def','jkl'))) AND (test.id IN SELECT * from sys.odcivarchar2list('s1', ... ) UNION ALL SELECT * from sys.odcivarchar2list('s1000', ...)))

```
$ time ./runtest.sh system password 5 >& result5_odcivarchar2list.txt

real	4m20.077s
user	0m55.423s
sys	0m23.179s
```

