import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
/**
 * This class takes the url and file from the SQLite database
 * that Chromium browsers uses for local download history
 * @author EthanMaxm
 * @version 09/14/23
 */
public class UrlGetter {

    /**
     * got from <a href="https://www.javatpoint.com/java-sqlite">...</a>
     * This makes a connection to the SQLite database given in the path parameter then calls method to get url and directory
     * @param path the path of the database
     * @param searchAll if the caller desires to search the entire download table or just the ones from about the last month
     * @return returns hashmap where the file is the key and the url is the value
     */
    public HashMap<String, String> connect(String path, boolean searchAll) {
        /*
         * Connect to a sample database
         */
        Connection conn = null;
        HashMap<String, String> fileUrl = new HashMap<>();
        try {
            // db parameters
            String url = "jdbc:sqlite:" + path;
            // create a connection to the database
            conn = DriverManager.getConnection(url);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    if(searchAll){
                    fileUrl = getFileUrl(conn, "downloads");
                    }
                    else{
                        fileUrl = getFileUrlSkip(
                                conn,
                                Paths.get(path).getParent().toString() + new Constants().slash +"skipTo",
                                "downloads");
                    }

                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return fileUrl;

    }

    /**
     * Got from <a href="https://www.youtube.com/watch?v=0beocykXUag&ab_channel=LogicLambda">...</a> @4:35
     * this method reads the database and maps the file and corresponding url the file
     * is from onto a hashmap
     * @param conn the connection to the database
     * @param tableName the name of the table the caller desires to go through
     * @return returns hashmap where the file is the key and the url is the value
     */
    private HashMap<String, String> getFileUrl(Connection conn, String tableName) throws SQLException {
        HashMap<String, String> fileUrl = new HashMap<>();
        //the url that is shown when actually used when using chrome is stored in downloads_url_chains
        //and has the same id as the one in downloads
        HashMap<String, String> idUrl = getSQLHashMap(conn, "downloads_url_chains", "id","url");
        String selectSQL = "SElECT * from " + tableName;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectSQL);
        while (rs.next()) {
            //fileUrl.put(Paths.get(rs.getString(columnLabelKey)), rs.getString(columnLabelValue));
            String url = idUrl.get(rs.getString("id"));
            fileUrl.put(rs.getString("target_path"), url);
          //  System.out.println(url);
        }
        conn.close();
        return fileUrl;
    }

    /**
     * Got from <a href="https://www.youtube.com/watch?v=0beocykXUag&ab_channel=LogicLambda">...</a> @4:35
     * gets a hashmap of 2 values from different columns of the same row of a SQLite database
     * @param conn the connection to the database
     * @param tableName the name of the table the caller desires to go through
     * @param columnLabelKey the name of the column the caller desires to be used a key
     * @param columnLabelValue the name of the column the caller desires to be used a value
     * @return the hashmap
     */
    private HashMap<String, String> getSQLHashMap(Connection conn, String tableName, String columnLabelKey, String columnLabelValue) throws SQLException {
        HashMap<String, String> fileUrl = new HashMap<>();
        String selectSQL = "SElECT * from " + tableName;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectSQL);
        while (rs.next()) {
            fileUrl.put(rs.getString(columnLabelKey), rs.getString(columnLabelValue));
        }
        //does not close connection so that it can still be used for the other methods
        return fileUrl;
    }


    /**
     * Large part from ChatGPT
     * this method reads the database from a specific row onwards and maps the file and corresponding url the file
     * is from onto a hashmap
     * @param conn the connection to the database
     * @param tableName the name of the table the caller desires to go through
     * @param skipToLocation the location of the file that contains the row number the program should skip to
     * @return returns hashmap where the file is the key and the url is the value
     */
    public  HashMap<String, String> getFileUrlSkip(Connection conn, String skipToLocation, String tableName) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        HashMap<String, String> fileUrl = new HashMap<>();
        try {
            statement = conn.createStatement();
            String storedDateString = "1000-01-01";
            File storedDateFile = new File(skipToLocation);
            //2 different variables because the after looping through the program thinks the row number
            // is the amount of rows it looked at (so it does not count any skipped)
            String rowNumber = "0";
            String trueRowNum = "0";
            if (storedDateFile.exists()) {
                try {
                    trueRowNum = new ReadFiles().readFileString(skipToLocation);
                    storedDateString = new ReadFiles().readSelectFileString(skipToLocation, 2);
                } catch (IOException ioException) {
                    String text = "error trying to read skipTo in UrlGetter";
                    new WriteFiles().appendAuditLog(text);
                    System.out.println(text);
                }
            }

            String sqlQuery = "SELECT * FROM " + tableName +
                    " ORDER BY id"  // Replace id with the column you want to sort by
                    +" LIMIT " + trueRowNum + ", " + "99999999"; // 99999999 is the maximum number of rows to retrieve after skipping skipTo rows

            resultSet = statement.executeQuery(sqlQuery);
            Time timeObject = new Time();
            //System.out.println(storedDateString);
            LocalDate storedDate = timeObject.stringToDate(storedDateString);
            HashMap<String, String> idUrl = getSQLHashMap(conn, "downloads_url_chains", "id","url");

            while (resultSet.next()) {
                String dateTimeString = resultSet.getString("last_modified");
                //System.out.println(resultSet.getString("last_modified") + "     " + resultSet.getString("tab_url"));
                // the way the date is stored it should be 16 characters, but sometimes it's empty
                if (dateTimeString.length() >= 16) {
                    //shortens it to just the date because it also stores the time
                    String stringDate = dateTimeString.substring(5, 16);
                    //System.out.println(stringDate);
                    //converts the date to a localDate object
                    LocalDate sqlDate = timeObject.stringToDateMonth(stringDate);
                    //if it was closer to one month ago than the last date stored (price is right rules)
                    if (sqlDate.isBefore(LocalDate.now().minusDays(30)) && sqlDate.isAfter(storedDate)) {
                      //  System.out.println(storedDate + "      " + sqlDate + "        " + rowNumber);
                        storedDate = sqlDate;
                        rowNumber = String.valueOf(resultSet.getRow()/*+1*/);
//
                    }
                }
                //retrieves the url from download_url_chains using the id
                String url = idUrl.get(resultSet.getString("id"));
                fileUrl.put(resultSet.getString("target_path"), url);
               // System.out.println(url);
            }
            String auditText = null;
            try {
                //the row number from the program is just how many it counted as it went through the database
                //this adds the row number from skipTo
                int temp = Integer.parseInt(rowNumber) + Integer.parseInt(trueRowNum);
                trueRowNum = Integer.toString(temp);
                //stores the row number and the date that row was last modified for the next time it's run
                new WriteFiles().createTextFile(skipToLocation, trueRowNum + "; " + storedDate);
                auditText = "replaced skipTo with " + rowNumber;
            } catch (IOException ioException) {
                String text = skipToLocation+" Error trying to create new skipTo file " + rowNumber;
                auditText = text;
                System.out.println(text);
            } finally {
                new WriteFiles().appendAuditLog(auditText);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the resources
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        return fileUrl;
    }
}

