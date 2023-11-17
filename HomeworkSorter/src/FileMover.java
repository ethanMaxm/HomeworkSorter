import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Set;

/**
 * This class contains methods for moving and copying files
 *
 * @author EthanMaxm
 * @version 11/15/23
 */
public class FileMover {
    /**
     * moves a file from the given path to the other given path
     * @param source The location of the file you wish to move (including the file name)
     * @param destination The location that you wish to move the file to (including the file name)
     * @return returns true if a file was actually moved
     */
    public boolean moveFile(String source, String destination) {
        boolean fileMoved = false;
        String auditText = null;
        try {
            Files.move(Paths.get(source), Paths.get(destination));
            auditText = source + " was moved to " + destination;
            fileMoved = true;

        }
        catch (IOException ioException){
            //System.out.println(source.toFile().exists() +"   " + destination.toFile().exists());
            String text = "There was an error moving a file, "+ source + ", " + destination;
            System.out.println(text);
            auditText = text;
        }
        finally {
            new WriteFiles().appendAuditLog(auditText);
        }
        return fileMoved;

    }
    /**
     * copies a file from the given path to the other given path
     * IF THERE IS A FILE IN THE GIVEN DESTINATION IT WILL REPLACE IT
     * @param source The location of the file you wish to copy (including the file name)
     * @param destination The location that you wish to copy the file to (including the file name)
     */
    public void copyFile(String source, String destination){
        try {
            Files.copy(Paths.get(source), Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ioException){
            System.out.println("There was an error copying a file \nSource: " + source + "\ndestination: " + destination);
            new WriteFiles().appendAuditLog("There was an error copying a file \nSource: " + source + "\ndestination: " + destination);
        }
    }

    /**
     * copies the History file of a given profile to the programs storage
     * @param historyProfile the title of the profile (typically profile 1,2, etc. or default)
     */
     public void copyHistoryDataFile(String historyProfile){
        Constants constants = new Constants();
        String auditText = null;
        //the directory of a certain profile in the programs storage
        String historyProfileDirectory = constants.historyPathStorage + constants.slash + historyProfile;
        //the location of the actual history database read from the programs storage
        String historyFilePath = new ReadFiles().readFileString( historyProfileDirectory + constants.slash + "History","copyHistoryDataFile");
        try {
            copyFile(historyFilePath, historyProfileDirectory + constants.slash + "History.db");
            auditText = "history file copied from " + historyFilePath/*historyFileLocation*/;
        }
        catch (NullPointerException nullPointerException){auditText = "nullPointerException trying to copy history file";}
        finally{
            new WriteFiles().appendAuditLog(auditText);
        }
    }

    /**
     * looks at the history databases for each profile for urls containing substrings given by the user
     * @param searchAll allows the caller to toggle between searching through all downloads in the history file and just the one's from about the last month
     * @return returns true if a file was actually moved
     */
    public boolean moveWantedFiles(boolean searchAll ) {
        Constants constants = new Constants();
        ReadFiles readFiles = new ReadFiles();
        boolean fileMoved = false;

        //loops for each profile the program saw
        for (String historyFileString : readFiles.filesInFolder(constants.historyPathStorage)) {
            // the directory of the history database that will soon be copied
            String historyDataBase = constants.historyPathStorage +constants.slash+ historyFileString + constants.slash+"History.db";

            //makes sure the program is looking at an up-to-date history
        copyHistoryDataFile(historyFileString);
        //A hashmap that contains the file locations as the key and the url as the value
        HashMap<String, String> urlFiles = new UrlGetter().connect(historyDataBase, searchAll);
        WriteFiles writeFiles = new WriteFiles();
        Set<String> filesInFolder = readFiles.filesInFolder(constants.pathStorage);
        filesInFolder.remove(".gitignore");
        //System.out.println(urlFiles);
            //loops for each url in the history database
        for (String filePath : urlFiles.keySet()) {
            //loops for each destination folder the user has selected
            for (String file : filesInFolder) {
                Set<String> textOfFile = null;
                try {
                    textOfFile = readFiles.readFileSet(constants.pathStorage +constants.slash+  file);
                } catch (IOException ioException) {
                    writeFiles.appendAuditLog("ioException reading " + file + " while trying to move files");
                }
                //loops for each substring the user has given for the destination folder
                assert textOfFile != null;
                for (String urlSubString : textOfFile) {
                    //if the url contains the current user given substring and the file still exists where the history database says it still is
                    if (urlFiles.get(filePath).contains(urlSubString) && new File(filePath).exists()) {
                        //System.out.println("hi");
                        String fileDestination = null;
                        try {
                            //System.out.println(constants.pathStorage.toString()+file);
                            //reads the file that contains the directory that the user wished the file to be moved
                            fileDestination = readFiles.readFileString(constants.pathStorage + constants.slash + file);
                            // System.out.println(fileDestination);
                        } catch (IOException ioException) {
                            writeFiles.appendAuditLog("error reading " + file + " in file mover");
                            System.out.println("error reading " + file + " in file mover");
                        }
                        String fileName = Paths.get(filePath).getFileName().toString();
                        String endPath = fileDestination + constants.slash + fileName;
                        fileMoved = new FileMover().moveFile(filePath, endPath);
                    }
                }
            }
        }
    }
        return fileMoved;
    }

    /**
     * looks at all file names in all download folders and checks to see if they contain any substrings given by the user
     * @param searchAll allows the caller to toggle between searching through all files in the download folder and just the new ones since the last time it was run
     * @return returns true if a file was actually moved
     */
    public boolean moveNameFiles(boolean searchAll){
        final String previousDownloadFolderName = "previousDownloadFolder";
        ReadFiles readFiles = new ReadFiles();
        boolean filesMoved = false;
        Constants constants = new Constants();
        //all the names download folders the user selected
        Set<String> downloadPathStorageFolderNames = readFiles.filesInFolder(constants.downloadPathStorage);
        // loops for every download folder
        for (String downloadFolderName: downloadPathStorageFolderNames) {
            //the directory for a specific download folder in storage
            String downloadPathStorageFolderPath = constants.downloadPathStorage + constants.slash + downloadFolderName;
            //the file inside the storage for a download folder that contains the directory for the actual download folder
            String downloadPathStorageFilePath = downloadPathStorageFolderPath + constants.slash + downloadFolderName;
            Set<String> filesInDownloadFolder;
            //will contain all the file names that were inside the download folder after last time the program attempted to move it
            Set<String> filesInLastDownloadFolder;
            String downloadFolderPath;
            try {
                //reads to get the location of the download folder
                downloadFolderPath = readFiles.readFileString(downloadPathStorageFilePath);
                //gets the names of all the files in the actual download folder
                filesInDownloadFolder = readFiles.filesInFolder(downloadFolderPath);
            } catch (IOException ioException) {
                String text = "error reading " + downloadPathStorageFilePath;
                System.out.println(text);
                new WriteFiles().appendAuditLog(text);
                //if the program could not read the download the file it won't work so this skips to the next loop
                continue;
            }

            //the directory for the file that contains the file names of what was inside the download folder
            String previousDownloadFolderPath = downloadPathStorageFolderPath + constants.slash + previousDownloadFolderName;
            if(new File(previousDownloadFolderPath).exists()) {
            try {
                filesInLastDownloadFolder = readFiles.readFileSet(previousDownloadFolderPath);
            } catch (IOException ioException) {
                String text = "Ioexception reading " + previousDownloadFolderPath;
                System.out.println(text);
                new WriteFiles().appendAuditLog(text);
                continue;
            }
            //if the caller only wants to look at recent files this removes all the files that were there the last time the program moved files from that folder
            if(!searchAll){
            filesInDownloadFolder.removeAll(filesInLastDownloadFolder);
            }
        }
            //if there are files left after the filesInLastDownloadFolder may have been removed
            if (!filesInDownloadFolder.isEmpty()) {
                //A set of containing the all the destination folder info storages as well as a file for the download folder path
                Set<String> filesInDownloadStorage = readFiles.filesInFolder(downloadPathStorageFolderPath);
                filesInDownloadStorage.remove(downloadFolderName);
                //loops through the name of each file in the set above
                for (String file : filesInDownloadStorage) {
                    Set<String> subStrings;
                    //the path to the file
                    String destinationDirectoryStorage = downloadPathStorageFolderPath + constants.slash + file;
                    try {
                        //all the name substrings the user has added
                        subStrings = readFiles.readFileSet(destinationDirectoryStorage);
                    } catch (IOException ioException) {
                        String text = "error reading " + downloadPathStorageFolderPath + constants.slash + file;
                        System.out.println(text);
                        new WriteFiles().appendAuditLog(text);
                        continue;
                    }
                    //loops through each substring
                    for (String subString : subStrings) {
                        //loops through all the files that are in the actual download folder
                        for (String downloadedFileName : filesInDownloadFolder) {
                            if (downloadedFileName.contains(subString)) {
                                String downloadFolderFile = downloadFolderPath + constants.slash + downloadedFileName;
                                String destination;

                                try {
                                    destination = readFiles.readFileString(destinationDirectoryStorage) + constants.slash + downloadedFileName;
                                } catch (IOException ioException) {
                                    String text = "error reading " + destinationDirectoryStorage;
                                    System.out.println(text);
                                    new WriteFiles().appendAuditLog(text);
                                    continue;
                                }
                                //System.out.println("Source: " + downloadFolderFile +"     Destination: "+destination);

                                filesMoved = moveFile(downloadFolderFile, destination);

                            }
                        }
                    }
                }

                //remaining code makes a file that contains all the files left in the actual download folder
                Set<String> newFilesInDownloadFolder = readFiles.filesInFolder(downloadFolderPath);
                StringBuilder downloadFolder = new StringBuilder();
                for (String file: newFilesInDownloadFolder){
                    downloadFolder.append(file).append(": ");
                }
                String auditText;
                try {
                    new WriteFiles().createTextFile(previousDownloadFolderPath, downloadFolder.toString());
                    auditText = "downloadFolderStorage created";
                }
                catch(IOException ioException){
                     auditText = "IException trying to create download folder storage in: " + downloadPathStorageFolderPath + constants.slash + previousDownloadFolderName;
                    System.out.println(auditText);
                }
                new WriteFiles().appendAuditLog(auditText);
            }
        }

        return filesMoved;
    }


    public void fakeDeleteFile(String filePath) {
        Constants constants = new Constants();
        String fileName = Paths.get(filePath).getFileName().toString();
        String fileDeletionPath = constants.deletedFiles +constants.slash+ fileName;
        moveFile(filePath, fileDeletionPath);

    }

}
