import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Contains methods to write or append to files
 * @author EthanMaxm
 * @version 09/16/23
 */
public class WriteFiles {


  /**
   * creates/replaces a file with text
   * @param filePath The path the caller wishes the file to be stored(including the file name)
   * @param text The text the caller wishes to be stored in the file
   */
  public void createTextFile(String filePath, String text) throws IOException {

      FileWriter writerObject = new FileWriter(filePath);

      writerObject.write(text);

      writerObject.close();

  }
    /**
     * creates/replaces a file with text with built-in exception handling
     * @param filePath The path the caller wishes the file to be stored(including the file name)
     * @param text The text the caller wishes to be stored in the file
     * @param method the method this method was called from
     */
    public void createTextFile(String filePath, String text, String method){
      String auditText;
      try {
          createTextFile(filePath,text);
          auditText = method + " created file " + filePath;
      }
      catch (IOException ioException){
          auditText = method + " caused error trying to write file " + filePath;
          System.out.println(auditText);
      }
      appendAuditLog(auditText);
    }

    /**
     * adds-to/creates a file in the given path (fileName) and adds text to it
     * @param fileName The path the caller wishes the file to be stored(including the file name)
     * @param text The text the caller wishes to be added to the file
     */
  public void appendTextFile(String fileName, String text) throws IOException{
//    try {

      FileWriter writerObject = new FileWriter(fileName, true);

      writerObject.write(text);

      writerObject.close();
  }

    /**
     * appends text to the audit log including the date it occurred
     * @param text the text the caller wishes to store in auditLog
     */
  public void appendAuditLog(String text){
    try {
    FileWriter writerObject = new FileWriter("Audit Log.txt", true);
    writerObject.write(text+": " +LocalDate.now() + "\n");
   // System.out.println(text);
    writerObject.close();
    } catch (IOException ioException) {
      System.out.println("There was an error appending to the Audit log");
   }
  }

}
