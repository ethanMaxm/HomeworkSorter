import java.io.File;
import java.io.IOException;


/**
 * Contains many values that are needed throughout the program
 * @author EthanMaxm
 * @version 09/16/23
 */

public final class Constants {

    /**
     * the directory the program is located in
     */
    public  final String thisPath = getFileLocation();
    /**
     * the type of slash the operating system uses to separate between directories
     */
    public  final char slash = setSlash();
    /**
     * the location of all the information needed to move files using urls
     */
    public  final String pathStorage = thisPath +slash+ "pathStorage";
    /**
     * the location of all the information needed to move files using file names
     */
    public final String downloadPathStorage = thisPath +slash+ "downloadPathStorage";
    /**
     * the location of all the information needed to copy and move files based on browser profile
     */
    public final String historyPathStorage =  thisPath + slash + "historyProfiles";
    /**
     * the location of the "deleted" files
     */
    public final String deletedFiles =  thisPath + slash + "deletedFiles";



    private String getFileLocation(){
        String path = null;
        try {
            path = new File(".").getCanonicalPath();
        }
        catch(IOException ioException){
            String text = "there was an error getting the program's path";
            new WriteFiles().appendAuditLog(text);
            System.out.println(text);
        }
        return path;
    }

    private  char setSlash() {
        char theSlash = '/';
        if (thisPath.contains('\\'+"")){theSlash = '\\';}
        return theSlash;
    }
}
