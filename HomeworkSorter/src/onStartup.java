import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * This is instructions for the computer to do on startup
 * @author EthanMaxm
 * @version 09/12/23
 */
public class onStartup {

    /**
     * constructor containing set of instructions that must be completed whenever the program is ran
     */
    public onStartup(){
        Constants constants = new Constants();
        new File(constants.pathStorage).mkdir();
        new File(constants.downloadPathStorage).mkdir();
        new File(constants.historyPathStorage).mkdir();
        new File(constants.deletedFiles).mkdir();
        findCreateHistoryProfileStorage();
        new MainJFrame();
        while(true) {
            try {
                if (new Time().isDayAfter()) {
                    findCreateHistoryProfileStorage();
                    new Time().storeCurrentDate();
                }
                TimeUnit.MINUTES.sleep(1);
                FileMover fileMover = new FileMover();
                fileMover.moveWantedFiles(true);
                fileMover.moveNameFiles(true);
             //   new GUIFrame("files moved");
            }
            catch(InterruptedException interruptedException){
                String text = "there was an error running AutoFileSorter";
                System.out.println(text);
                new GUIFrame(text);
                new WriteFiles().appendAuditLog(text);
            }
        }


    }

    /**
     * locates the local history file (assumes user is using chrome)
     * @return the location of the file
     */
    private String findHistory(){
        Constants constants = new Constants();
        String trueHistoryDataBase;
        //if the program is on windows slash should be \ and there are two possibilities of where it can be
        if (constants.slash == '\\') {
            String historyDataBaseLocation1 = "C:\\Users\\" +System.getProperty("user.name") +"\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";
            String historyDataBaseLocation2 = "C:\\Users\\" +System.getProperty("user.name") +"\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Cache\\History";
            if(new File(historyDataBaseLocation1).exists()){
                trueHistoryDataBase = historyDataBaseLocation1;

            }
            else /*if (new File(historyDataBaseLocation2).exists())*/{
                trueHistoryDataBase = historyDataBaseLocation2;
            }
        }
        //assumes the user is on mac if they are not on windows
        else{
            String historyDataBaseLocation3 = "/Users/" +System.getProperty("user.name") +"/Library/Application Support/Google/Chrome/Default/History";
            String historyDataBaseLocation4 =    "/Users/"+System.getProperty("user.name")+"/Library/Caches/Google/Chrome/Default/Cache/History";
            if(new File(historyDataBaseLocation3).exists()){
                trueHistoryDataBase = historyDataBaseLocation3;

            }
            else /*if (new File(historyDataBaseLocation4).exists())*/{
                trueHistoryDataBase = historyDataBaseLocation4;
            }
        }
        return trueHistoryDataBase;
    }

    /**
     * creates the files where the history database directory will be stored for any profile
     * @param trueHistoryDataBasePath the directory that the history database is located
     * @param profileName the name of the profile that the history is storing
     */
    private void createHistoryProfileStorage(Path trueHistoryDataBasePath, String profileName){
        Constants constants = new Constants();
        String profilePath = constants.historyPathStorage +constants.slash+profileName;
        new File(profilePath).mkdir();
        new WriteFiles().createTextFile(
                profilePath +constants.slash+ "History",
                trueHistoryDataBasePath.toString() + constants.slash + profileName + constants.slash+"History; ",
                "onStartup");
    }

    /**
     * finds the location of the history database and then creates the storage for them.
     */
    private void findCreateHistoryProfileStorage(){
        Constants constants = new Constants();

        String trueHistoryDataBase;
        ReadFiles readFiles = new ReadFiles();
        String fileName = "History";
        //if the user manually selected the history file this looks at it rather than finding it itself
        if(new File(fileName).exists()){
            Path historyPath = Paths.get(readFiles.readFileString(constants.thisPath + constants.slash+fileName, "findCreateHistoryProfileStorage"));
            trueHistoryDataBase = historyPath.toString();
            System.out.println(trueHistoryDataBase);
        }
        else{
           trueHistoryDataBase = findHistory();
        }
        //System.out.println(Paths.get(trueHistoryDataBase));
        // System.out.println(Paths.get(trueHistoryDataBase).getParent());


        //the final two items in the directory should be the profile title followed by history
        //so this looks at the directory before the profile title to search for all profiles
        Path trueHistoryDataBasePath = Paths.get(trueHistoryDataBase).getParent().getParent();


        for (String file:readFiles.filesInFolder(trueHistoryDataBasePath.toString())){
            if(file.contains("Profile")){
                createHistoryProfileStorage(trueHistoryDataBasePath, file);
            }
        }
        createHistoryProfileStorage(trueHistoryDataBasePath, "Default");
    }

}
