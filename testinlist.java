/*
    
Example to show the difference in SQL between WHERE IN textlist and WHERE IN (SELECT FROM ... )
Just a quick and dirty test

Oracle does the WHERE after JOIN

https://asktom.oracle.com/pls/apex/asktom.search?tag=join-with-where-clause

Idea with creating temp. table from here:

https://asktom.oracle.com/pls/asktom/f?p=100:11:0%3a%3a%3a%3aP11_QUESTION_ID:666224436920

For portability reasons, temp. table is created with JDBC. At Mariadb a temp. table lifes for a session, a oracle the rows until commit.


Compile:
    javac testinlist.java

    Run:
    Create tables for testing
    time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' system password inittable

    Test with WHERE IN:
    time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' system password test1

    Test with temp. table:
    time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' system password test2

    Test with WHERE IN and JOIN:
    time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' system password test3

    Test with temp. table and JOIN:
    time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' system password test4

    Test with sys.odcivarchar2list and JOIN:
    time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' system password test5

    Delete tables:
    time java -cp ojdbc8-full/ojdbc8.jar:. testinlist oracle 'jdbc:oracle:thin:@//localhost:1521/XEPDB1' system password reset 

    

My results:

Oracle 18c XE running on a Centos 7 KVM on localhost with Debian 10

MariaDB 10.3.17 from Debian 10 on localhost with Debian 10:

Hardware on localhost: Desktop i7-4790S CPU @ 3.20GHz, SSDs

$ javac testinlist.java 


*/

public class testinlist {


    public static void main(String[] args) {
        java.sql.Connection conn = null;
        long nano1, nano2, testnano1, testnano2; 
        int bits = 20; // create 2^bits documents in the table
        int bits2 = 16; // create 2^bits documents in the table test2
        int prime1 = 17; // smaler than 2^bits // FIXME: Calculate from bits
        int prime2 = 11;
        java.util.Random rnd = new java.util.Random();
        String ses = "a"; // "Some session key, e.g user_id + timeofday"



        try {
            if (args.length != 5 ) {
                System.out.println("Needed parameters: dbtype connectionstring user password test");
                System.out.println("dbtype: oracle or mariadb");
                System.out.println("test: inittable - Intialize test table");
                System.out.println("test: test1- test WHERE IN textlist");
                System.out.println("test: test2 - test with tmp table");
                System.out.println("test: test3 - test LEFT JOIN and WHERE IN textlist");
                System.out.println("test: test4 - test LEFT JOIN with tmp table");
                System.exit(0);
            }

           String paramDbtype=args[0];
           String paramTest=args[4];



            if (args[0].contentEquals("mariadb")) {
                Class.forName("org.mariadb.jdbc.Driver");
            }
            else if (args[0].contentEquals("oracle")) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
            }
            else {
                System.out.println("give Databasetype as first argument");
                System.out.println(args[0]);
                System.exit(0);
            }

            conn = java.sql.DriverManager.getConnection(args[1], args[2], args[3]);
            java.sql.PreparedStatement pstatement = null;
            java.sql.PreparedStatement pstatement2 = null;

            // disable autocommit to ensure not to write on disk
            conn.setAutoCommit(false);

            if (args[4].contentEquals("inittable")) {
                // init a test table
               
                // Some
                nano1 = System.nanoTime();
                if (paramDbtype.contentEquals("oracle")) {
                pstatement=conn.prepareStatement("CREATE TABLE test(id CHAR(32) PRIMARY KEY, class VARCHAR2(32), data VARCHAR2(1024) )");
                }
                else if (paramDbtype.contentEquals("mariadb")) {
                    pstatement=conn.prepareStatement("CREATE TABLE test(id CHAR(32) PRIMARY KEY, class VARCHAR(32), data VARCHAR(1024) )");
                }
                else {
                    System.out.println("give Databasetype as first argument");
                    System.exit(0);
                }

                nano2 = System.nanoTime();
                System.out.println("prep CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                nano1 = System.nanoTime();
                pstatement.execute();
                nano2 = System.nanoTime();
                System.out.println("exec CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
               
           
                
                int id = 2;

                String idString; 
                String dataString; // FIXME Stringbuffer
                String classString;

                String randomString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. " +
                "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.  " +
                "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. " +
                "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper su dolor sit amet";

                nano1 = System.nanoTime();
                pstatement=conn.prepareStatement("INSERT INTO test (id, class, data) VALUES(?,?,?)");
                nano2 = System.nanoTime();
                System.out.println("prep INSERT INTO " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                String classes[] = new String[5];
                classes[0] = "abc";
                classes[1] = "def";
                classes[2] = "gih";
                classes[3] = "jkl";
                classes[4] = "mno";


                for (int n = 0 ; n< (1<< bits); n++) {
                    idString = "0000000000000000" + String.format("%1$016X", id);
                    dataString = randomString.substring(rnd.nextInt(1024)).substring(0, rnd.nextInt(1024));
                    classString =  classes[rnd.nextInt(5)];

                    // System.out.println(idString + " " + dataString); 

                    nano1 = System.nanoTime();
                    pstatement.setString(1, idString); // Starts at 1 not 0 !!!
                    pstatement.setString(2, classString);
                    pstatement.setString(3, dataString); 
                    pstatement.execute();
                    nano2 = System.nanoTime();
                    if (n % 100000 == 0) System.out.println("exec INSERTINTO " + n + ": " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                    // LCG, so each ID is unique
                    id = (5 * id + prime1) % (1 << bits);
                } 

                if (paramDbtype.contentEquals("oracle")) {

                    nano1 = System.nanoTime();
                    pstatement=conn.prepareStatement("CREATE GLOBAL TEMPORARY TABLE listhelper(ses CHAR(32), id CHAR(32)) ON COMMIT DELETE ROWS");
                    nano2 = System.nanoTime();
                    System.out.println("prep CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                    nano1 = System.nanoTime();
                    pstatement.execute();
                    nano2 = System.nanoTime();
                    System.out.println("exec CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
               
                    

                    /*
                    // not working, for now using batched inserts.
                    
                    StringBuilder st = new StringBuilder();
                    st.setLength(0);
                   
                    st.append("CREATE OR REPLACE PROCEDURE csvtotable (s IN CHAR(32), csv IN CLOB) AS ");
                    st.append("firstchar NUMBER;");
                    st.append("lastchar NUMBER;");
                    st.append("data CHAR(32);");
                    
                    st.append("firstchar := 1;");
                    st.append("BEGIN " );
                    st.append("LOOP ");
                    st.append("lastchar := INSTR(csv, ',', firstchar);");
                    st.append("IF lastchar IS 0 THEN ");
                    // Last ID, take the rest
                    st.append("data := SUBSTR(csv, firstchar);");
                    st.append("ELSE ");
                    st.append("data := SUBSTR(csv, firstchar, lastchar-firstchar)");
                    st.append("firstchar := lastchar + 1;");
                    st.append("END IF;");
                    st.append("INSERT INTO listhelper (session, id) VALUES (s, data);");
                    st.append("EXIT WHEN lastchar IS 0;");
                    st.append("END LOOP;");
                    st.append("END csvtotable");
                    
                    nano1 = System.nanoTime();
                    pstatement=conn.prepareStatement(st.toString());
                    nano2 = System.nanoTime();
                    System.out.println("prep Procedure: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                    nano1 = System.nanoTime();
                    pstatement.execute();
                    nano2 = System.nanoTime();
                    System.out.println("exec proc " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms"); 
                    */
                } else if (paramDbtype.contentEquals("mariadb")) {
                    // not necessary, as mariadb removes table after ending of the session
                    nano1 = System.nanoTime();
                    pstatement=conn.prepareStatement("CREATE TEMPORARY TABLE listhelper(ses CHAR(32), id CHAR(32))");
                    nano2 = System.nanoTime();
                    System.out.println("prep CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                    nano1 = System.nanoTime();
                    pstatement.execute();
                    nano2 = System.nanoTime();
                    System.out.println("exec CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
               
                }

                // Some
                nano1 = System.nanoTime();
                if (paramDbtype.contentEquals("oracle")) {
                    pstatement=conn.prepareStatement("CREATE TABLE test2(id CHAR(32) PRIMARY KEY, data VARCHAR2(1024) )");
                }
                else if (paramDbtype.contentEquals("mariadb")) {
                    pstatement=conn.prepareStatement("CREATE TABLE test2(id CHAR(32) PRIMARY KEY, data VARCHAR(1024) )");
                }
                else {
                    System.out.println("give Databasetype as first argument");
                    System.exit(0);
                }

                nano2 = System.nanoTime();
                System.out.println("prep CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                nano1 = System.nanoTime();
                pstatement.execute();
                nano2 = System.nanoTime();
                System.out.println("exec CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
               
           
                
                id = 2;

                nano1 = System.nanoTime();
                pstatement=conn.prepareStatement("INSERT INTO test2 (id, data) VALUES(?,?)");
                nano2 = System.nanoTime();
                System.out.println("prep INSERT INTO " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");


                for (int n = 0 ; n< (1<< bits2); n++) {
                    idString = "0000000000000000" + String.format("%1$016X", id);
                    dataString = randomString.substring(rnd.nextInt(1024)).substring(0, rnd.nextInt(1024));

                    // System.out.println(idString + " " + dataString); 

                    nano1 = System.nanoTime();
                    pstatement.setString(1, idString); // Starts at 1 not 0 !!!
                    pstatement.setString(2, dataString); 
                    pstatement.execute();
                    nano2 = System.nanoTime();
                    if (n % 10000 == 0) System.out.println("exec INSERTINTO " + n + ": " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                    // LCG, so each ID is unique
                    id = (4001 * id + prime1) % (1 << bits2);
                } 

            }
            else if (args[4].contentEquals("reset")) {
                // Delete Table
                nano1 = System.nanoTime();
                pstatement=conn.prepareStatement("DROP TABLE test");
                nano2 = System.nanoTime();
                System.out.println("prep DROP TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                nano1 = System.nanoTime();
                pstatement.execute();
                nano2 = System.nanoTime();
                System.out.println("exec DROP TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                // Delete Table
                 nano1 = System.nanoTime();
                 pstatement=conn.prepareStatement("DROP TABLE listhelper");
                 nano2 = System.nanoTime();
                 System.out.println("prep DROP TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
 
                 nano1 = System.nanoTime();
                 pstatement.execute();
                 nano2 = System.nanoTime();
                 System.out.println("exec DROP TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                
                // Delete Table
                nano1 = System.nanoTime();
                pstatement=conn.prepareStatement("DROP TABLE test2");
                nano2 = System.nanoTime();
                System.out.println("prep DROP TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                nano1 = System.nanoTime();
                pstatement.execute();
                nano2 = System.nanoTime();
                System.out.println("exec DROP TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
               
               

            }             
            else if (paramTest.contentEquals("test1") || paramTest.contentEquals("test2")) {

                int id = rnd.nextInt(100000); // start somewhere, set to fix value for exact same requests between test, but then cache is used on db side.

                int t=2;
                double lastexectime = 0; // stop, when over 10 seconds
                String idString;
                StringBuilder st = new StringBuilder();
                java.sql.ResultSet rs;
                
                if (paramTest.contentEquals("test2")) {

                    if (paramDbtype.contentEquals("mariadb")) {
                        // here necessary, as mariadb DROPs table after ending of the session
                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement("CREATE TEMPORARY TABLE listhelper(ses CHAR(32), id CHAR(32))");
                        nano2 = System.nanoTime();
                        System.out.println("prep CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
    
                        nano1 = System.nanoTime();
                        pstatement.execute();
                        nano2 = System.nanoTime();
                        System.out.println("exec CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                    }

                    nano1 = System.nanoTime();
                    pstatement2=conn.prepareStatement("INSERT INTO listhelper (ses, id) VALUES (?, ?)");
                    nano2 = System.nanoTime();
                    System.out.println("prep INSERT INTO listhelper " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                    // FIXME: use something like the now depreciated setExecuteBatch
                    // https://docs.oracle.com/database/121/JJDBC/oraperf.htm#JJDBC28753

                }



                while ((t <= 20000 ) && (lastexectime < 30000 /* milliseconds */ )) {
                    testnano1 = System.nanoTime();

                    if (paramTest.contentEquals("test1")) {
                        nano1 = System.nanoTime();
                        st.setLength(0);
                        st.append("SELECT id,class,data FROM test WHERE (class IN ('def','jkl')   ) AND (");
                        // st.append("SELECT id,class,data FROM test WHERE (");

                        for (int n = 0 ; n<t; n++) {
                            idString = "0000000000000000" + String.format("%1$016X", id);
                            
                            if (n % 999 == 0) { // "only" 1000 list elements allowed
                                if (n == 0) {
                                    st.append("(id IN ('"+idString+"'");
                                }
                                else {
                                    
                                    st.append(")) OR (id IN ('"+idString+"'");
                                }
                            }
                            else {
                                st.append(",'"+idString+"'");
                            }

                            // LCG, so each ID is unique, nut not in same order as at creation time
                            id = (9 * id + prime2) % (1 << bits);
                        }
                        st.append( ")))");
                        nano2 = System.nanoTime();
                        System.out.println("IDs prepared: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                        System.out.println("Trying to get " + t + " IDs, expecting " + t*2/5); 
        


                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement(st.toString());
                        nano2 = System.nanoTime();
                        System.out.println("prep SELECT id,class,data FROM test WHERE id IN: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        nano1 = System.nanoTime();
                        rs = pstatement.executeQuery();
                        nano2 = System.nanoTime();

                        lastexectime = ((double)(nano2-nano1))/1000000.0;
                        System.out.println("exec SELECT id,class,data FROM test WHERE id IN: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                        String data;
                        String cl;
                        int n = 0;
                        nano1 = System.nanoTime();

                        // really get all data, in case some streaming tricks are done
                        while(rs.next()){
                            //Retrieve by column name
                            idString  = rs.getString("id");
                            cl=rs.getString("class");
                            data = rs.getString("data");
                        
                            //Display values
                            if (n==0) {
                                System.out.println(idString + " " + cl + " " + ((data.length() < 20)?data:data.substring(0, 20)) );
                            }
                            n++;
                        }
                        rs.close();
                        nano2 = System.nanoTime();

                        System.out.println(n + " Results in total in " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                    }

                    else if (paramTest.contentEquals("test2")) {
                        
                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement("DELETE FROM listhelper WHERE ses = CAST( ? AS CHAR(32))");
                        nano2 = System.nanoTime();
                        System.out.println("prep DELETE FROM listhelper " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
    
                        nano1 = System.nanoTime();
                        pstatement.setString(1, ses);
                        pstatement.execute();
                        nano2 = System.nanoTime();
                        System.out.println("exec DELETE FROM listhelper  " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        nano1 = System.nanoTime();

                        pstatement2.setString(1,ses);
                            
                        for (int n = 0 ; n<t; n++) {
                            idString = "0000000000000000" + String.format("%1$016X", id);
                
                            pstatement2.setString(2,idString);
                            pstatement2.addBatch();
                           
                            // LCG, so each ID is unique, nut not in same order as at creation time
                            id = (9 * id + prime2) % (1 << bits);
                        }

                        pstatement2.executeBatch();


                         /* 
                         // SLOW, only 50% improvement

                        for (int n = 0 ; n<t; n++) {
                            idString = "0000000000000000" + String.format("%1$016X", id);
                
                            pstatement2.setString(1,ses);
                            pstatement2.setString(2,idString);
                            pstatement2.execute();
                           
                            // LCG, so each ID is unique, nut not in same order as at creation time
                            id = (9 * id + prime2) % (1 << bits);
                        }

                        */


                        /*

                        // Very SLOW

                        st.setLength(0);
                        st.append("INSERT ALL ");

                        for (int n = 0 ; n<t; n++) {
                            idString = "0000000000000000" + String.format("%1$016X", id);
                            st.append("INTO listhelper VALUES ('a','" + idString + "') " );
                           
                            // LCG, so each ID is unique, nut not in same order as at creation time
                            id = (9 * id + prime2) % (1 << bits);
                        }

                        st.append(" SELECT 1 FROM DUAL");

                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement(st.toString());
                        nano2 = System.nanoTime();
                        System.out.println("prep INSERT ALL: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        nano1 = System.nanoTime();
                        rs = pstatement.executeQuery();
                        nano2 = System.nanoTime();
                        System.out.println("exec INSERT ALL: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                        */


                       

                        nano2 = System.nanoTime();
                        System.out.println("Sum exec INSERT INTO listhelper " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                                                
                        System.out.println("Trying to get " + t + " IDs, expecting " + t*2/5); 
        
                        
                        nano1 = System.nanoTime();
                        // FIXME: Why CAST is necessary? 
                        // pstatement=conn.prepareStatement("SELECT id,class,data FROM test WHERE (class IN ('def','jkl')) AND (id IN (SELECT id FROM listhelper WHERE ses=CAST( ? AS CHAR(32))))");
                        pstatement=conn.prepareStatement("SELECT test.id,class,data FROM test INNER JOIN listhelper ON ((class IN ('def','jkl')) AND test.id=listhelper.id AND ses=CAST( ? AS CHAR(32)))");
                        nano2 = System.nanoTime();
                        System.out.println("prep SELECT test.id,class,data FROM test INNER JOIN listhelper ON: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        nano1 = System.nanoTime();
                        pstatement.setString(1, ses);
                        rs = pstatement.executeQuery();
                        nano2 = System.nanoTime();

                        lastexectime = ((double)(nano2-nano1))/1000000.0;
                        System.out.println("exec SELECT test.id,class,data FROM test INNER JOIN listhelper ON: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        /*
                        // Now a temp. table , done at beginning 
                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement("DELETE FROM listhelper WHERE ses = CAST( ? AS CHAR(32))");
                        nano2 = System.nanoTime();
                        System.out.println("prep DELETE FROM listhelper " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
    
                        nano1 = System.nanoTime();
                        pstatement.setString(1, ses);
                        pstatement.execute();
                        nano2 = System.nanoTime();
                        System.out.println("exec DELETE FROM listhelper  " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                        */


                        String data;
                        String cl;
                        int n = 0;
                        nano1 = System.nanoTime();
                        while(rs.next()){
                            //Retrieve by column name
                            idString  = rs.getString("id");
                            cl=rs.getString("class");
                            data = rs.getString("data");
                        
                            //Display values
                            if (n==0) {
                                System.out.println(idString + " " + cl + " " + ((data.length() < 20)?data:data.substring(0, 20)) );
                            }
                            n++;
                        }
                        rs.close();
                        nano2 = System.nanoTime();
                        System.out.println(n + " Results in total in " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");



                        
                   
                    }
                    testnano2 = System.nanoTime();
                    System.out.println("Test Time for " + t + ": " + String.format("%f", ((double)(testnano2-testnano1))/1000000.0) + " ms");
                    System.out.println();

                    t=t * 10;
                }

            }

            else if (paramTest.contentEquals("test3") || paramTest.contentEquals("test4")|| paramTest.contentEquals("test5")) {

                int id = rnd.nextInt(100000); // start somewhere, set to fix value for exact same requests between test, but then cache is used on db side.

                int t=2;
                double lastexectime = 0; // stop, when over 10 seconds
                String idString;
                StringBuilder st = new StringBuilder();
                java.sql.ResultSet rs;
                
                if (paramTest.contentEquals("test4")) {

                    if (paramDbtype.contentEquals("mariadb")) {
                        // here necessary, as mariadb DROPs table after ending of the session
                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement("CREATE TEMPORARY TABLE listhelper(ses CHAR(32), id CHAR(32))");
                        nano2 = System.nanoTime();
                        System.out.println("prep CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
    
                        nano1 = System.nanoTime();
                        pstatement.execute();
                        nano2 = System.nanoTime();
                        System.out.println("exec CREATE TABLE " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                    }

                    nano1 = System.nanoTime();
                    pstatement2=conn.prepareStatement("INSERT INTO listhelper (ses, id) VALUES (?, ?)");
                    nano2 = System.nanoTime();
                    System.out.println("prep INSERT INTO listhelper " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                    // FIXME: use something like the now depreciated setExecuteBatch
                    // https://docs.oracle.com/database/121/JJDBC/oraperf.htm#JJDBC28753

                }



                while ((t <= 20000 ) && (lastexectime < 30000 /* milliseconds */ )) {
                    testnano1 = System.nanoTime();

                    if (paramTest.contentEquals("test3")) {
                        nano1 = System.nanoTime();
                        st.setLength(0);
                        st.append("SELECT test.id AS id,class,test.data AS data1, test2.data AS data2 FROM test LEFT JOIN test2 ON (test.id = test2.id ) WHERE (class IN ('def','jkl')   ) AND (");
                        // st.append("SELECT id,class,data FROM test WHERE (");

                        for (int n = 0 ; n<t; n++) {
                            idString = "0000000000000000" + String.format("%1$016X", id);
                            
                            if (n % 999 == 0) { // "only" 1000 list elements allowed
                                if (n == 0) {
                                    st.append("(test.id IN ('"+idString+"'");
                                }
                                else {
                                    
                                    st.append(")) OR (test.id IN ('"+idString+"'");
                                }
                            }
                            else {
                                st.append(",'"+idString+"'");
                            }

                            // LCG, so each ID is unique, nut not in same order as at creation time
                            id = (9 * id + prime2) % (1 << bits);
                        }
                        st.append( ")))");
                        nano2 = System.nanoTime();
                        System.out.println("IDs prepared: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                        System.out.println("Trying to get " + t + " IDs, expecting " + t*2/5); 
        


                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement(st.toString());
                        nano2 = System.nanoTime();
                        System.out.println("prep SELECT id,class,data FROM test WHERE id IN: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        nano1 = System.nanoTime();
                        rs = pstatement.executeQuery();
                        nano2 = System.nanoTime();

                        lastexectime = ((double)(nano2-nano1))/1000000.0;
                        System.out.println("exec SELECT id,class,data FROM test WHERE id IN: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                        String data;
                        String data2;
                        String cl;
                        int n = 0;
                        nano1 = System.nanoTime();

                        // really get all data, in case some streaming tricks are done
                        while(rs.next()){
                            //Retrieve by column name
                            idString  = rs.getString("id");
                            cl=rs.getString("class");
                            data = rs.getString("data1");
                            data2=rs.getString("data2");
                        
                            //Display values
                            if (n==0) {
                                System.out.println(idString + " " + cl + " " + ((data.length() < 20)?data:data.substring(0, 20)) + " " + (data2!=null?((data2.length() < 20)?data2:data2.substring(0, 20)):"NULL"));
                            }
                            n++;
                        }
                        rs.close();
                        nano2 = System.nanoTime();

                        System.out.println(n + " Results in total in " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                    }

                    else if (paramTest.contentEquals("test4")) {
                        
                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement("DELETE FROM listhelper WHERE ses = CAST( ? AS CHAR(32))");
                        nano2 = System.nanoTime();
                        System.out.println("prep DELETE FROM listhelper " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
    
                        nano1 = System.nanoTime();
                        pstatement.setString(1, ses);
                        pstatement.execute();
                        nano2 = System.nanoTime();
                        System.out.println("exec DELETE FROM listhelper  " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        nano1 = System.nanoTime();

                        pstatement2.setString(1,ses);
                            
                        for (int n = 0 ; n<t; n++) {
                            idString = "0000000000000000" + String.format("%1$016X", id);
                
                            pstatement2.setString(2,idString);
                            pstatement2.addBatch();
                           
                            // LCG, so each ID is unique, nut not in same order as at creation time
                            id = (9 * id + prime2) % (1 << bits);
                        }

                        pstatement2.executeBatch();


                         /* 
                         // SLOW, only 50% improvement

                        for (int n = 0 ; n<t; n++) {
                            idString = "0000000000000000" + String.format("%1$016X", id);
                
                            pstatement2.setString(1,ses);
                            pstatement2.setString(2,idString);
                            pstatement2.execute();
                           
                            // LCG, so each ID is unique, nut not in same order as at creation time
                            id = (9 * id + prime2) % (1 << bits);
                        }

                        */


                        /*

                        // Very SLOW

                        st.setLength(0);
                        st.append("INSERT ALL ");

                        for (int n = 0 ; n<t; n++) {
                            idString = "0000000000000000" + String.format("%1$016X", id);
                            st.append("INTO listhelper VALUES ('a','" + idString + "') " );
                           
                            // LCG, so each ID is unique, nut not in same order as at creation time
                            id = (9 * id + prime2) % (1 << bits);
                        }

                        st.append(" SELECT 1 FROM DUAL");

                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement(st.toString());
                        nano2 = System.nanoTime();
                        System.out.println("prep INSERT ALL: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        nano1 = System.nanoTime();
                        rs = pstatement.executeQuery();
                        nano2 = System.nanoTime();
                        System.out.println("exec INSERT ALL: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                        */


                       

                        nano2 = System.nanoTime();
                        System.out.println("Sum exec INSERT INTO listhelper " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                                                
                        System.out.println("Trying to get " + t + " IDs, expecting " + t*2/5); 
        
                        
                        nano1 = System.nanoTime();
                        // FIXME: Why CAST is necessary? 
                        // pstatement=conn.prepareStatement("SELECT id,class,data FROM test WHERE (class IN ('def','jkl')) AND (id IN (SELECT id FROM listhelper WHERE ses=CAST( ? AS CHAR(32))))");
                        pstatement=conn.prepareStatement("SELECT test.id AS id,class,test.data AS data1, test2.data AS data2 FROM test LEFT JOIN test2 ON (test.id = test2.id ) INNER JOIN listhelper ON ((class IN ('def','jkl')) AND test.id=listhelper.id AND ses=CAST( ? AS CHAR(32)))");
                        nano2 = System.nanoTime();
                        System.out.println("prep SELECT test.id,class,data FROM test INNER JOIN listhelper ON: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        nano1 = System.nanoTime();
                        pstatement.setString(1, ses);
                        rs = pstatement.executeQuery();
                        nano2 = System.nanoTime();

                        lastexectime = ((double)(nano2-nano1))/1000000.0;
                        System.out.println("exec SELECT test.id,class,data FROM test INNER JOIN listhelper ON: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        /*
                        // Now a temp. table , done at beginning 
                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement("DELETE FROM listhelper WHERE ses = CAST( ? AS CHAR(32))");
                        nano2 = System.nanoTime();
                        System.out.println("prep DELETE FROM listhelper " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
    
                        nano1 = System.nanoTime();
                        pstatement.setString(1, ses);
                        pstatement.execute();
                        nano2 = System.nanoTime();
                        System.out.println("exec DELETE FROM listhelper  " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                        */


                        String data;
                        String data2;
                        String cl;
                        int n = 0;
                        nano1 = System.nanoTime();
                        while(rs.next()){
                            //Retrieve by column name
                            idString  = rs.getString("id");
                            cl=rs.getString("class");
                            data = rs.getString("data1");
                            data2=rs.getString("data2");
                        
                            //Display values
                            if (n==0) {
                                System.out.println(idString + " " + cl + " " + ((data.length() < 20)?data:data.substring(0, 20)) + " " + (data2!=null?((data2.length() < 20)?data2:data2.substring(0, 20)):"NULL"));
                            }
                            n++;
                        }
                        rs.close();
                        nano2 = System.nanoTime();
                        System.out.println(n + " Results in total in " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");



                        
                   
                    }
		    else if (paramTest.contentEquals("test5")) {
                        // SELECT value FROM json_table('["def","jkl"]', '$[*]' COLUMNS (value PATH '$'))

                        nano1 = System.nanoTime();
                        st.setLength(0);
                        st.append("SELECT test.id AS id,class,test.data AS data1, test2.data AS data2 FROM test LEFT JOIN test2 ON (test.id = test2.id)");
			st.append(" WHERE (class IN (SELECT * FROM sys.odcivarchar2list('def','jkl'))) AND (test.id IN (");
                        // st.append("SELECT id,class,data FROM test WHERE (");

                        for (int n = 0 ; n<t; n++) {
                            idString = "0000000000000000" + String.format("%1$016X", id);

 			    if (n % 999 == 0) { // max. 32 k text
                                if (n == 0) {
                                    st.append("SELECT * from sys.odcivarchar2list('"+idString+"'");
                                }
                                else {
                                    st.append(") UNION ALL SELECT * from sys.odcivarchar2list('"+idString+"'");
                                }
                            }
                            else {
                                st.append(",'"+idString+"'");
                            }        


                        
                            // LCG, so each ID is unique, nut not in same order as at creation time
                            id = (9 * id + prime2) % (1 << bits);
                        }
                        st.append(" )))");
			//System.out.println("Query: " + st.toString());

                        nano2 = System.nanoTime();
                        System.out.println("IDs prepared: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                        System.out.println("Trying to get " + t + " IDs, expecting " + t*2/5); 
        


                        nano1 = System.nanoTime();
                        pstatement=conn.prepareStatement(st.toString());
                        nano2 = System.nanoTime();
                        System.out.println("prep SELECT id,class,data FROM test WHERE id IN: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");

                        nano1 = System.nanoTime();
                        rs = pstatement.executeQuery();
                        nano2 = System.nanoTime();

                        lastexectime = ((double)(nano2-nano1))/1000000.0;
                        System.out.println("exec SELECT id,class,data FROM test WHERE id IN: " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                        String data;
                        String data2;
                        String cl;
                        int n = 0;
                        nano1 = System.nanoTime();

                        // really get all data, in case some streaming tricks are done
                        while(rs.next()){
                            //Retrieve by column name
                            idString  = rs.getString("id");
                            cl=rs.getString("class");
                            data = rs.getString("data1");
                            data2=rs.getString("data2");
                        
                            //Display values
                            if (n==0) {
                                System.out.println(idString + " " + cl + " " + ((data.length() < 20)?data:data.substring(0, 20)) + " " + (data2!=null?((data2.length() < 20)?data2:data2.substring(0, 20)):"NULL"));
                            }
                            n++;
                        }
                        rs.close();
                        nano2 = System.nanoTime();

                        System.out.println(n + " Results in total in " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");
                   
                    }


                    testnano2 = System.nanoTime();
                    System.out.println("Test Time for " + t + ": " + String.format("%f", ((double)(testnano2-testnano1))/1000000.0) + " ms");
                    System.out.println();

                    t=t * 10;
                }

            }

            else {
                System.out.println("Give command as 5. argument");
                System.exit(0);
            }

            nano1 = System.nanoTime();
            conn.commit();
            nano2 = System.nanoTime();
            System.out.println("commit " + String.format("%f", ((double)(nano2-nano1))/1000000.0) + " ms");





        } catch(Exception e){
            e.printStackTrace();
            try {
                if(conn!=null) {
                    System.out.println("Rollback!");
                    conn.rollback();
                    conn.close();
                }
            }
            catch(java.sql.SQLException sqle) {
                sqle.printStackTrace();
            }
            
        }
        finally {
            // FIXME
        }


    }




}
