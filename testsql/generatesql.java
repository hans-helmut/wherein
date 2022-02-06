public class generatesql {

    public static void main(String[] args) {
        int bits = 20; // create 2^bits documents in the table
        int bits2 = 16; // create 2^bits documents in the table test2
        int prime1 = 17; // smaler than 2^bits // FIXME: Calculate from bits
        int prime2 = 11;
	int prime3 = 13;
        java.util.Random rnd = new java.util.Random();
	int substringstart;
        int listlen = 20;
        int nlists = 1;

        try {
            if (args.length < 1) {
                System.out.println("Needed parameters: sqltype listlen repeats");
                System.out.println("sqltype: inittable or list or odcivarchar2list or reset");
                System.out.println("inittable: create table test");
                System.out.println("list listlen n: selects for n lists with listlen using lists");
                System.out.println(
                        "odcivarchar2list listlen n: selects for n lists with listlen using sys.odcivarchar2list");
                System.out.println("drop: drop table test");
                System.exit(0);
            }

            String paramTest = args[0];

            if (paramTest.contentEquals("list") || paramTest.contentEquals("odcivarchar2list")) {
                if (args.length >= 2) {
                    listlen = Integer.parseInt(args[1]);
                }
                if (args.length >= 3) {
                    nlists = Integer.parseInt(args[2]);
                }
            }

            // disable autocommit to ensure not to write on disk
            System.out.println("SET AUTOCOMMIT OFF\n");

            if (paramTest.contentEquals("inittable")) {
                System.out.println(
                        "CREATE TABLE test(id CHAR(32) PRIMARY KEY, class VARCHAR2(32), data VARCHAR2(1024));");

                int id = 2;

                String idString;
                String dataString; // FIXME Stringbuffer
                String classString;

                String randomString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. "
                        + "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.  "
                        + "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. "
                        + "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper su dolor sit amet";

                String classes[] = new String[5];
                classes[0] = "abc";
                classes[1] = "def";
                classes[2] = "gih";
                classes[3] = "jkl";
                classes[4] = "mno";

                for (int n = 0; n < (1 << bits); n++) {
                    idString = "0000000000000000" + String.format("%1$016X", id);
		    substringstart = rnd.nextInt(1024);
                    dataString = randomString.substring(substringstart, substringstart + 300);
	            // dataString = "";
                    classString = classes[rnd.nextInt(5)];

                    System.out.println("INSERT INTO test (id, class, data) VALUES('" + idString + "','" + classString
                            + "','" + dataString + "');");

                    if (n % 100000 == 0)
                        System.out.println("COMMIT;");

                    // LCG, so each ID is unique
                    id = (5 * id + prime1) % (1 << bits);
                }

		System.out.println(
                        "CREATE TABLE test2(id CHAR(32) PRIMARY KEY, data VARCHAR2(1024));");


                for (int n = 0 ; n< (1<< bits2); n++) {
                    idString = "0000000000000000" + String.format("%1$016X", id);
		    substringstart = rnd.nextInt(1024);
                    dataString = randomString.substring(substringstart, substringstart + 250);
	            // dataString = "";
              
                    System.out.println("INSERT INTO test2 (id, data) VALUES('" + idString +  "','" + dataString + "');");

                    if (n % 100000 == 0)
                        System.out.println("COMMIT;");
                   
                    // LCG, so each ID is unique
                    id = (4001 * id + prime2) % (1 << bits2);
                } 

            } else if (paramTest.contentEquals("drop")) {
                System.out.println("DROP TABLE test PURGE;");
		System.out.println("DROP TABLE test2 PURGE;");
            } else if (paramTest.contentEquals("list") || paramTest.contentEquals("odcivarchar2list")) {

                int id = rnd.nextInt(100000); // start somewhere, set to fix value for exact same requests between test,
                                              // but then cache is used on db side.

                String idString;
                java.sql.ResultSet rs;

                for (int c = 0; c < nlists; c++) {
                    if (paramTest.contentEquals("list")) {
                        System.out.println("SELECT test.id AS id,class,test.data AS data1, test2.data AS data2 FROM test LEFT JOIN test2 ON (test.id = test2.id) WHERE (");
                    } else if (paramTest.contentEquals("odcivarchar2list")) {
                        System.out.println("SELECT test.id AS id,class,test.data AS data1, test2.data AS data2 FROM test LEFT JOIN test2 ON (test.id = test2.id) WHERE (test.id IN (");
                    }

                    for (int n = 0; n < listlen; n++) {
                        idString = "0000000000000000" + String.format("%1$016X", id);

                        if (n % 999 == 0) { // "only" 1000 list elements allowed
                            if (n == 0) {
                                if (paramTest.contentEquals("list")) {
                                    System.out.println("(test.id IN ('" + idString + "'");
                                } else if (paramTest.contentEquals("odcivarchar2list")) {
                                    System.out.println("SELECT * from sys.odcivarchar2list('" + idString + "'");
                                }
                            } else {
                                if (paramTest.contentEquals("list")) {
                                    System.out.println(")) OR (test.id IN ('" + idString + "'");
                                } else if (paramTest.contentEquals("odcivarchar2list")) {
                                    System.out.println(
                                            ") UNION ALL SELECT * from sys.odcivarchar2list('" + idString + "'");
                                }
                            }

                        } else {
                            System.out.println(",'" + idString + "'");
                        }

                        // LCG, so each ID is unique, nut not in same order as at creation time
                        id = (9 * id + prime3) % (1 << bits);
                    }
                    System.out.println(")));");

                }

            }
            System.out.println("COMMIT;");

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            // FIXME
        }

    }

}
