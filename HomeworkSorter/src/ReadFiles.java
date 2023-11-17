import java.io.File;
import java.io.IOException;
import java.util.*;
/**
 * contains methods to read files in different ways
 *
 * @author EthanMaxm
 * @version 09/12/23
 */
public class ReadFiles {
    /**
     * returns all the file names in a given directory as a set
     * @param folderPath The directory the caller would like
     * @return a set of all the file names in the directory
     */
    public Set<String> filesInFolder(String folderPath) {
        File folder = new File(folderPath);
        String[] folderList = folder.list();
        assert folderList != null;
        Set<String> folderSet = new HashSet<>(Arrays.asList(folderList).subList(0, Objects.requireNonNull(folderList).length));
        folderSet.remove(".DS_Store");
        return folderSet;
    }
        /**
         * reads and returns all the text in a given file as a set
         * uses the delimiter "; "
         * @param filePath the file the caller wishes to be read
         * @return A set containing all the items in the file
         */
    public Set<String> readFileSet(String filePath) throws IOException{
        Set<String> text = new HashSet<>();
//        try {
            File file =new File(filePath);
            Scanner reader = new Scanner(file);
            reader.useDelimiter("; ");
            while(reader.hasNext()){
                text.add(reader.next());
            }
//        }
//        catch (IOException ioException){System.out.println("there was an error reading the file (set), "+ filePath);}
        return text;
    }

    /**
     * reads and returns only everything before the first instance of "; "
     * @param filePath the file the caller wishes to be read
     * @return The first string given in the file
     */
    public String readFileString(String filePath) throws IOException{
        String text;
//        try {
            File file = new File(filePath);
            Scanner reader = new Scanner(file);
            reader.useDelimiter("; ");
            text = reader.next();
//
//        }
//        catch (IOException ioException){System.out.println("there was an error reading a file (string)");}
        return text;
    }
    /**
     * reads and returns only everything before the first instance of "; " with built-in error handling
     * @param filePath the file the caller wishes to be read
     * @param method the method this method was called from
     * @return The first string given in the file
     */
    public String readFileString(String filePath, String method){
        String text = null;
        try {
        File file = new File(filePath);
        Scanner reader = new Scanner(file);
        reader.useDelimiter("; ");
        text = reader.next();

        }
        catch (IOException ioException){
            String auditText =  method + " caused an error trying to read " + filePath;
            System.out.println(auditText);
            new WriteFiles().appendAuditLog(auditText);
        }
        return text;
    }
    /**
     * reads a specific item from a file
     * @param filePath the file the caller wishes to be read
     * @param stringPlace the location of the string the caller would like in the file
     * @return The string in the given location in the file
     */
    public String readSelectFileString(String filePath, int stringPlace) throws IOException{
        String text = null;
//        try {
        File file = new File(filePath);
        Scanner reader = new Scanner(file);
        reader.useDelimiter("; ");
        for (int i = 0; i < stringPlace; i++){
            text = reader.next();
        }
//
//        }
        return text;
    }
}