import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/**
 * This class allows the caller to create a text file that contain the date and one to read the date from it
 * used for checking if the program has moved the files today on startup
 * @author EthanMaxm
 * @version 09/12/23
 */
public class Time {

    public Time(){}
    /**
     * creates a file with the date
     */
    public void storeCurrentDate() {
        LocalDate date = LocalDate.now();
        WriteFiles writeFiles = new WriteFiles();
        String auditText = null;
        try {
            writeFiles.createTextFile( new Constants().thisPath+ new Constants().slash + "Previous_File_Move_Date", date.toString());
            auditText = date +" replaced Previous_File_Move_Date text";
        }
        catch (IOException ioException){
            String text = "Error trying to write "+ date +" to Previous_File_Move_Date";
            System.out.println(text);
            auditText = text;
        }
        finally{
            writeFiles.appendAuditLog(auditText);
        }
    }
    /**
     * converts a string date to a local date using "yyyy-MM-dd" format
     * @param date the date the caller wishes to be converted
     * @return the LocalDate
     */
    public LocalDate stringToDate(String date) throws NullPointerException{
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }

    /**
     * checks to see if the date stored in "Previous_File_Move_Date" is the next day or later
     * @return true if it is the next day or later
     */
    public boolean isDayAfter() {
        Constants constants = new Constants();
        String timeFilePath = constants.thisPath + constants.slash+"Previous_File_Move_Date";
        boolean isNextDay = false;
        WriteFiles writeFiles = new WriteFiles();
        String auditText = null;
        try {

            isNextDay = LocalDate.now().isAfter(new Time().stringToDate(new ReadFiles().readFileString(timeFilePath)));
        }
        catch(NullPointerException nullPointerException){
            System.out.println("There was no time file to read");
            auditText = "nullPointerException trying to read Previous_File_Move_Date";
        }
        catch(IOException ioException){
            auditText = "IOException trying to read Previous_File_Move_Date";
        }
        finally{
            writeFiles.appendAuditLog(auditText);
        }
        return isNextDay;
    }

    /**
     * converts a string date to a local date using "d MMM uuuu" format
     * @param date the date the caller wishes to be converted
     * @return the LocalDate
     */
    public LocalDate stringToDateMonth(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu");

        return LocalDate.parse(date, formatter);

    }

}
