# Difference between `WHERE IN` with list and `SYS.ODCIVARCHAR2LIST` after JOIN

[Disussion at Oracle](https://asktom.oracle.com/pls/apex/asktom.search?tag=limit-and-conversion-very-long-in-list-where-x-in)

* Looks like a normal run is faster, but measurement shows not.
* See run.sh for test
* Each measurement here runs 1000 queries with a list of 200 after a JOIN
* Using `SYS.ODCIVARCHAR2LIST` is faster and uses less CPU on the DB-server.
* While it looks factor 2 faster, it is in reality factor 4 on server side, as the sqlplus-client and server wait for each other.

```
SQL> set autotrace traceonly explain
SQL> set linesize 200
SQL> @list200_1.sql     

Ausfuhrungsplan
----------------------------------------------------------
Plan hash value: 3176853232

---------------------------------------------------------------------------------------------
| Id  | Operation		      | Name	    | Rows  | Bytes | Cost (%CPU)| Time     |
---------------------------------------------------------------------------------------------
|   0 | SELECT STATEMENT	      | 	    |	200 |	213K|	610   (1)| 00:00:01 |
|   1 |  NESTED LOOPS OUTER	      | 	    |	200 |	213K|	610   (1)| 00:00:01 |
|   2 |   INLIST ITERATOR	      | 	    |	    |	    |		 |	    |
|   3 |    TABLE ACCESS BY INDEX ROWID| TEST	    |	200 |	107K|	408   (1)| 00:00:01 |
|*  4 |     INDEX UNIQUE SCAN	      | SYS_C008049 |	200 |	    |	200   (1)| 00:00:01 |
|   5 |   TABLE ACCESS BY INDEX ROWID | TEST2	    |	  1 |	545 |	  2   (0)| 00:00:01 |
|*  6 |    INDEX UNIQUE SCAN	      | SYS_C008058 |	  1 |	    |	  1   (0)| 00:00:01 |
---------------------------------------------------------------------------------------------

Predicate Information (identified by operation id):
---------------------------------------------------

   4 - access("TEST"."ID"='000000000000000000000000000009DC' OR
	      "TEST"."ID"='000000000000000000000000000027F6' OR
	      "TEST"."ID"='000000000000000000000000000032ED' OR
[...]
	      "TEST"."ID"='00000000000000000000000000067341' OR
	      "TEST"."ID"='00000000000000000000000000069EC0' OR)
   6 - access("TEST"."ID"="TEST2"."ID"(+))
       filter("TEST2"."ID"(+)='000000000000000000000000000009DC' OR
	      "TEST2"."ID"(+)='000000000000000000000000000027F6' OR
	      "TEST2"."ID"(+)='000000000000000000000000000032ED' OR
[...]
	      "TEST2"."ID"(+)='0000000000000000000000000006164C' OR
	      "TEST2"."ID"(+)='0000000000000000000000000006187D' OR "TE)

Note
-----
   - this is an adaptive plan


Transaktion mit COMMIT abgeschlossen.

SQL> @odcivarchar2list200_1.sql

Ausfuhrungsplan
----------------------------------------------------------
Plan hash value: 2280042179

--------------------------------------------------------------------------------------------------------
| Id  | Operation				 | Name        | Rows  | Bytes | Cost (%CPU)| Time     |
--------------------------------------------------------------------------------------------------------
|   0 | SELECT STATEMENT			 |	       |   255 |   273K|   811	 (1)| 00:00:01 |
|   1 |  NESTED LOOPS OUTER			 |	       |   255 |   273K|   811	 (1)| 00:00:01 |
|   2 |   NESTED LOOPS				 |	       |   255 |   137K|   540	 (1)| 00:00:01 |
|   3 |    SORT UNIQUE				 |	       |  8168 | 16336 |    29	 (0)| 00:00:01 |
|   4 |     COLLECTION ITERATOR CONSTRUCTOR FETCH|	       |  8168 | 16336 |    29	 (0)| 00:00:01 |
|   5 |    TABLE ACCESS BY INDEX ROWID		 | TEST        |     1 |   550 |     2	 (0)| 00:00:01 |
|*  6 |     INDEX UNIQUE SCAN			 | SYS_C008049 |     1 |       |     1	 (0)| 00:00:01 |
|   7 |   TABLE ACCESS BY INDEX ROWID		 | TEST2       |     1 |   545 |     2	 (0)| 00:00:01 |
|*  8 |    INDEX UNIQUE SCAN			 | SYS_C008058 |     1 |       |     1	 (0)| 00:00:01 |
--------------------------------------------------------------------------------------------------------

Predicate Information (identified by operation id):
---------------------------------------------------

   6 - access("TEST"."ID"=VALUE(KOKBF$))
   8 - access("TEST"."ID"="TEST2"."ID"(+))


Transaktion mit COMMIT abgeschlossen.

SQL> set autotrace traceonly stat
SQL> @list200_1.sql

200 Zeilen ausgewahlt.


Statistiken
----------------------------------------------------------
	  0  recursive calls
	  0  db block gets
	689  consistent gets
	195  physical reads
	  0  redo size
     123647  bytes sent via SQL*Net to client
	767  bytes received via SQL*Net from client
	 15  SQL*Net roundtrips to/from client
	  0  sorts (memory)
	  0  sorts (disk)
	200  rows processed


Transaktion mit COMMIT abgeschlossen.

SQL> @odcivarchar2list200_1.sql

200 Zeilen ausgewahlt.


Statistiken
----------------------------------------------------------
	  0  recursive calls
	  0  db block gets
	687  consistent gets
	200  physical reads
	  0  redo size
     122310  bytes sent via SQL*Net to client
	767  bytes received via SQL*Net from client
	 15  SQL*Net roundtrips to/from client
	  1  sorts (memory)
	  0  sorts (disk)
	200  rows processed


Transaktion mit COMMIT abgeschlossen.

```
First run:
```
$ ./run.sh 
Using sys.odcivarchar2list:

real	0m16.721s
user	0m7.881s
sys	0m1.197s

real	0m14.817s
user	0m7.534s
sys	0m0.988s

real	0m14.762s
user	0m7.544s
sys	0m0.962s

real	0m14.714s
user	0m7.514s
sys	0m1.018s

real	0m14.692s
user	0m7.498s
sys	0m0.977s

real	0m14.683s
user	0m7.507s
sys	0m0.980s

real	0m14.838s
user	0m7.548s
sys	0m0.980s

real	0m15.099s
user	0m7.671s
sys	0m1.068s

real	1m8.029s
user	0m8.861s
sys	0m1.213s

real	0m15.813s
user	0m7.940s
sys	0m1.061s
Using list:

real	0m32.653s
user	0m7.674s
sys	0m0.966s

real	0m32.382s
user	0m7.620s
sys	0m1.037s

real	0m32.152s
user	0m7.611s
sys	0m0.956s

real	0m32.039s
user	0m7.595s
sys	0m1.054s

real	0m32.026s
user	0m7.604s
sys	0m1.026s

real	0m32.075s
user	0m7.585s
sys	0m1.034s

real	0m32.548s
user	0m7.673s
sys	0m1.048s

real	0m32.309s
user	0m7.659s
sys	0m0.983s

real	0m32.211s
user	0m7.645s
sys	0m1.116s

real	0m32.067s
user	0m7.564s
sys	0m0.981s
```

```
$ ./run.sh 
Using sys.odcivarchar2list:

real	0m16.566s
user	0m7.731s
sys	0m1.058s

real	0m15.126s
user	0m7.459s
sys	0m1.009s

real	0m14.877s
user	0m7.509s
sys	0m1.013s

real	0m15.003s
user	0m7.630s
sys	0m0.972s

real	0m14.875s
user	0m7.498s
sys	0m0.984s

real	0m15.138s
user	0m7.739s
sys	0m1.010s

real	0m14.866s
user	0m7.480s
sys	0m1.013s

real	0m14.830s
user	0m7.487s
sys	0m0.984s

real	0m14.704s
user	0m7.433s
sys	0m0.968s

real	0m14.796s
user	0m7.455s
sys	0m1.017s
Using list:

real	1m39.477s
user	0m8.352s
sys	0m1.132s

real	0m31.740s
user	0m7.551s
sys	0m0.954s

real	0m31.527s
user	0m7.455s
sys	0m1.053s

real	0m31.309s
user	0m7.531s
sys	0m0.950s

real	0m31.431s
user	0m7.432s
sys	0m1.007s

real	0m31.462s
user	0m7.562s
sys	0m0.959s

real	0m31.516s
user	0m7.516s
sys	0m0.988s

real	0m38.809s
user	0m8.011s
sys	0m1.116s

real	0m34.764s
user	0m7.865s
sys	0m1.063s

real	0m32.863s
user	0m7.791s
sys	0m1.092s
```

