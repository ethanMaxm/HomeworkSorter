import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.Stack;

/**
 * The GUI aspect to the project
 *
 * @author EthanMaxm
 * @version 11/15/23
 */
public class MainJFrame extends JFrame {
    /**
     * Field panels: an ArrayList that contains the JPanels the user used in order to get to the current panel they are on
     */
    private Stack<JPanel> panels;
    /**
     * Field buttonSize: the size of all the buttons in the current panel
     */
    private Dimension buttonSize;

    /**
     * constructor
     */
    public MainJFrame(){
        panels = new Stack<>();
        buttonSize = new Dimension(200,200);
        setVisible(true);
        JPanel panel = mainMenu();
        add(panel);
        panels.push(panel);
        pack();
        setTitle("Auto File Sorter");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * replaces the current JPanel with another and adds the new one to panels arrayList
     * @param replacement the panel the caller wishes to replace the current one with
     */
    public void replacePanel(JPanel replacement){

        getContentPane().removeAll();
        getContentPane().add(replacement);

        pack();
        panels.push(replacement);
    }

    /**
     * replaces the current panel with the one the previous panel in panels and removes the current one from panels
     */
    public void goToLastPanel(){
        getContentPane().removeAll();
        panels.pop();
        getContentPane().add(panels.peek());
        pack();
    }

    /**
     * the default panel for the frame
     * @return the panel
     */
    public JPanel mainMenu(){
        JPanel panel = new JPanel();
        //panel.setPreferredSize(new Dimension(500,250));
        panel.add(mainURLPanelPopUp());
        panel.add(mainNamePanelPopUp());
        return panel;
    }

    /**
     * the main panel for the part of the program that moves files based off of the url they are from
     * @return the panel
     */
    public JPanel mainURLPanel(){
        JPanel panel = new JPanel();
        buttonSize = new Dimension(200,200);
        //panel.setPreferredSize(new Dimension(500,500));
        panel.add(backButton());
        panel.add(fileChooserButton("Select History File",false, new Constants().thisPath, false));
        panel.add(fileListPopUp("Add String", new Constants().pathStorage,false));
        panel.add(fileListPopUp("delete", new Constants().pathStorage,true));
        panel.add(moveFilesButton(false));
        panel.add(moveFilesButton(true));
        return panel;

    }
    /**
     * the main panel for the part of the program that moves files based off of their file names
     * @return the panel
     */
    public JPanel mainNamePanel(){
        JPanel panel = new JPanel();
        //panel.setPreferredSize(new Dimension(500,500));
        buttonSize = new Dimension(200,200);
        panel.add(backButton());
        panel.add(addDownloadFolder());
        panel.add(moveNameFiles(false));
        panel.add(moveNameFiles(true));
        return panel;
    }


    private JPanel preparePanelList(Set<String> collection){
        JPanel panel = new JPanel();
        //adjusts the size of the panel so that it doesn't become to long
        int buttonHeight = 25;
        int panelHeight = buttonHeight+5;
        buttonSize = new Dimension(300, buttonHeight);
        int collectionSize = collection.size();
        panelHeight = ((collectionSize+4)/4)*panelHeight;
        if (collectionSize > 2){
            panel.setPreferredSize(new Dimension(1200, panelHeight));
        }
        panel.add(backButton());
        return panel;
    }
        /**
     * a panel containing buttons for all the files in a directory, a back button and a fileChooserButton
     * @param storagePath the directory that the panel should make a buttons for
     * @param makeDirectory if the buttons should make a directory or a file when used
     * @param delete if the button is being used to delete a file
     * @return the panel
     */
    public JPanel fileListPanel2(String storagePath, boolean makeDirectory, boolean delete) {
        ReadFiles readFiles = new ReadFiles();
        Constants constants = new Constants();
        Set<String> files = readFiles.filesInFolder(storagePath);
        //removes the path storage file (if any)
        files.remove(Paths.get(storagePath).getFileName().toString());
        files.remove("previousDownloadFolder");
        JPanel panel = preparePanelList(files);


        if (!delete){
            JButton newPathButton = fileChooserButton("New Path", true, storagePath, makeDirectory);
            newPathButton.setPreferredSize(buttonSize);
            panel.add(newPathButton);
        }
        for (String path : files) {
            String fileLocation = storagePath +constants.slash+path;
            JButton button = textFieldPopUp(fileLocation);
            if (new File(fileLocation).isDirectory()) {
                button = fileListPopUp(path, fileLocation,false);
            }
            else if(delete){
                button = stringListPopUp(fileLocation);

            }
            button.setPreferredSize(buttonSize);
            panel.add(button);
        }
        return panel;
    }
    private JPanel stringListPanel(String storagePath){
    Constants constants = new Constants();
    String storageName = Paths.get(storagePath).getFileName().toString();
    ReadFiles readFiles = new ReadFiles();
    Set<String> strings;
    try {
        strings = readFiles.readFileSet(storagePath);
    }
    catch (IOException ioException){
        String auditText =  "stringListPanel" + " caused an error trying to read " + storagePath;
        System.out.println(auditText);
        new WriteFiles().appendAuditLog(auditText);
        new GUIFrame("error");
        //stops function from going further
        return panels.peek();
    }
    JPanel panel = preparePanelList(strings);
    for (String string : strings) {
        if(!string.contains(constants.slash+"")) {
            JButton button = deleteStringButton(storagePath, string);
            button.setPreferredSize(buttonSize);
            panel.add(button);
        }
    }
    return panel;
}
    /**
     * method to have the user select a file using JFileChooser
     * @param isDirectory if the caller wishes the user to select a file or a directory
     * @return the path the user selected
     */
    public Path fileChooser(boolean isDirectory) {
        JFileChooser chooser = new JFileChooser();
        Path path = null;
        if (isDirectory) {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            //getCurrentDirectory and getSelectedFile return different values on windows and mac
            if(isDirectory && new Constants().slash != '\\' ) {
                path = chooser.getCurrentDirectory().toPath();
               // System.out.println(path+"hrbgkjf");
            }
        else{
            path = chooser.getSelectedFile().toPath();
        }
    }

        return path;
}
    /**
     * button that allows the user to select a directory that they would like to move files to/from
     * it also allows the user to select the local history for their browser
     * @param buttonText the text on the button
     * @param isDirectory is the item the user is looking for a file or a directory
     * @param destinationPath The directory the caller desires the location of the folder/file to be stored
     * @param makeDirectory should the folder/file be stored in a new folder
     * @return the button
     */
    public JButton fileChooserButton(String buttonText, boolean isDirectory, String destinationPath, boolean makeDirectory) {
        JButton selectDownloadFile = new JButton(new AbstractAction(buttonText) {
            @Override
            public void actionPerformed(ActionEvent j) {
                String destinationPath1 = destinationPath;
                Constants constants = new Constants();
                Path path = fileChooser(isDirectory);
                if (path != null){
                    Path filePathEnding  = path.getFileName();
                    //if makeDirectory this will make the directory as update destinationPath1 so that it will make the storage file inside of this new directory
                    if(makeDirectory){
                        destinationPath1 = destinationPath +constants.slash+ filePathEnding;
                        System.out.println(destinationPath1);
                        new File(destinationPath1).mkdir();
                    }
                    WriteFiles writeFiles = new WriteFiles();
                    String auditText = null;
                    try {
                        writeFiles.createTextFile(destinationPath1+constants.slash+filePathEnding, path + "; ");
                        auditText = "Created " + filePathEnding + " string storage for " + path;
                    } catch (IOException ioException) {
                        String text = "Error trying to create string storage for " + path + " would be storage location " + destinationPath1;
                        System.out.println(text);
                        auditText = text;
                    } finally {
                        writeFiles.appendAuditLog(auditText);
                    }
                    if (new File(destinationPath1 + constants.slash + filePathEnding).exists()) {
                        new GUIFrame("You have selected a path!");
                        //if they were looking for a directory then they were doing so through fileListPanel
                        // so this makes it update the panel with that the new folder button will show up
                        if(isDirectory) {
                            goToLastPanel();
                            replacePanel(fileListPanel2(destinationPath, makeDirectory,false));
                        }
                    } else {
                        new GUIFrame("Path not saved.");
                    }
                }}
        });
        selectDownloadFile.setPreferredSize(buttonSize);
        return selectDownloadFile;
    }
    /**
     * button that makes a frame containing a text-field pop up
     * when the user enters a string the program will add that string to the desired file
     * @param fileLocation the location of the file that the user desires to add text to
     * @return the button
     */
    public JButton textFieldPopUp(String fileLocation) {
        Path fileLocationPath = Paths.get(fileLocation);
//        setTitle("add String to: " +fileLocationPath.getFileName());
        ReadFiles readFiles = new ReadFiles();
        String fileName = fileLocationPath.getFileName().toString();
        JButton addTextField = new JButton(new AbstractAction(fileName) {
            JTextField textField;
            GUIFrame frame;

            @Override
            public void actionPerformed(ActionEvent m) {
                JPanel panel = new JPanel();
                textField = new JTextField("Enter Your String", 30);
                textField.addActionListener(new storeString());
                panel.add(textField);
                frame = new GUIFrame("Add to", false, panel);
            }

            class storeString implements ActionListener {
                public void actionPerformed(ActionEvent event) {

                    //Constants constants = new Constants();
                    WriteFiles writeFiles = new WriteFiles();
                    String auditText;
                    Set<String> fileBefore = null;
                    try {
                        fileBefore = readFiles.readFileSet(fileLocation);

                    } catch (IOException ioException) {
                        auditText = "error trying to read " + fileLocation;
                        System.out.println(auditText);
                        writeFiles.appendAuditLog(auditText);
                    }

                    String textFieldText = textField.getText();
                    try {
                        writeFiles.appendTextFile(fileLocation, textFieldText + "; ");
                    } catch (IOException ioException) {
                        String text = "There was an error writing " + textFieldText + " from text field to " + fileName;
                        System.out.println(text);
                        writeFiles.appendAuditLog(text);
                    }
                    Set<String> fileAfter;
                    String frameText = "";
                    try {
                        fileAfter = readFiles.readFileSet(fileLocation);
                        assert fileBefore != null;
                        if (fileBefore.equals(fileAfter)) {
                            frameText = "Nothing was added.";
                        } else {
                            frameText = "You have added a string!";

                        }
                    } catch (IOException ioException) {
                        auditText = "error trying to read " + fileLocation;
                        System.out.println(auditText);
                        frameText = auditText;
                        writeFiles.appendAuditLog(auditText);

                    } finally {
                        frame.setVisible(false);
                        new GUIFrame(frameText);
                    }
                }
            }

        });
        addTextField.setPreferredSize(buttonSize);
        return addTextField;
    }
    /**
     * when button is pushed calls FileMover().moveWantedFiles
     * @param searchAll should the program try to move every file that meets the criteria
     * @return the button
     */
    public JButton moveFilesButton(boolean searchAll) {
        String buttonText;
        if (searchAll) {
            buttonText = "Move All Files Via Url";
        }
        else{
            buttonText = "Move Files Via Url";
        }
        JButton moveFiles = new JButton(new AbstractAction(buttonText) {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean fileMoved;
//                Constants constants = new Constants();
//                ReadFiles readFiles = new ReadFiles();
//                Set <String>historyDataBaseLocations = new HashSet<>();
//                try {
//                   historyDataBaseLocations = readFiles.readFileSet(readFiles.combinePaths(constants.thisPath, Paths.get("History")));
//                } catch (IOException ex) {
//                    String text = "error reading history location storage in moveAllFilesButton";
//                }
//                for (String historyDataBaseLocation: new ReadFiles().readHistoryFile()){
                    fileMoved = new FileMover().moveWantedFiles(searchAll/*,historyDataBaseLocation*/);
//                }
                if (fileMoved) {
                    new GUIFrame("You have moved (a) file(s)");
                } else {
                    new GUIFrame("No files were moved.");
                }
            }
        });
        moveFiles.setPreferredSize(buttonSize);
        return moveFiles;
    }
    /**
     * when button is pushed changes the panel to the fileListPanel
     * @return the button
     */
    public JButton addDownloadFolder() {
        JButton addDownloadFolder = new JButton(new AbstractAction("Add Download Folder") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Constants constants = new Constants();
                replacePanel(fileListPanel2(constants.downloadPathStorage, true,false));
            }
        });
        addDownloadFolder.setPreferredSize(buttonSize);
        return addDownloadFolder;
    }
    /**
     * when button is pushed changes the panel to the fileListPanel
     * @return the button
     */
    public JButton fileListPopUp(String folderName, String directory, boolean delete) {
        JButton fileListPopUp = new JButton(new AbstractAction(folderName) {
            @Override
            public void actionPerformed(ActionEvent e) {
                replacePanel(fileListPanel2(directory,false,delete));
            }
        });
        fileListPopUp.setPreferredSize(buttonSize);
        return fileListPopUp;
    }
    public JButton stringListPopUp(String directory) {
        JButton stringListPopUp = new JButton(new AbstractAction(Paths.get(directory).getFileName().toString()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                replacePanel(stringListPanel(directory));
            }
        });
        stringListPopUp.setPreferredSize(buttonSize);
        return stringListPopUp;
    }
    /**
     * when button is pushed calls FileMover().moveNameFiles()
     * @param searchAll should the program try to move every file that meets the criteria
     * @return the button
     */
    public JButton moveNameFiles(boolean searchAll){
        String buttonText = "Move Files Via Name";
        if (searchAll){
            buttonText = "Move All Files Via Name";
        }
        JButton moveNameFiles = new JButton(new AbstractAction(buttonText) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (new FileMover().moveNameFiles(searchAll)) {
                    new GUIFrame("You have moved (a) file(s)");
                } else {
                    new GUIFrame("No files were moved.");
                }
            }
        });
        moveNameFiles.setPreferredSize(buttonSize);
        return moveNameFiles;
    }
    /**
     * when button is pushed calls goToLastPanel
     * @return the button
     */
    private JButton backButton() {
        JButton mainNamePanelPopUp = new JButton(new AbstractAction("Back") {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToLastPanel();
            }
        });
        mainNamePanelPopUp.setPreferredSize(buttonSize);
        return mainNamePanelPopUp;
    }


    // in order to fix it make the file list panel have a button parameter for when the file selected is a file and not a directory
    private JButton deleteStringButton(String filePath, String string){
        JButton deleteStringButton = new JButton(new AbstractAction(string) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ReadFiles readFiles = new ReadFiles();
                    System.out.println(filePath);
                    Set<String> strings = readFiles.readFileSet(filePath);
                    strings.remove(string);
                    //safe guards before deleting
                    if (filePath.contains(new Constants().pathStorage)){
                        new FileMover().fakeDeleteFile(filePath);
                        new File(filePath).delete();
                    }

                    WriteFiles writeFiles = new WriteFiles();
                    for(String string: strings){
                        string += "; ";
                        writeFiles.appendTextFile(filePath,string);
                    }
                }
                catch (IOException ioException){
                    String auditText =  "deleteString" + " caused an error trying to read " + filePath;
                    System.out.println(auditText);
                    new WriteFiles().appendAuditLog(auditText);
                    new GUIFrame("error trying to read file to delete from");
                }
                goToLastPanel();
                replacePanel(stringListPanel(filePath));


            }
        });
        deleteStringButton.setPreferredSize(buttonSize);


        return deleteStringButton;
    }
    /**
     * when button is pushed changes the panel to the mainNamePanel
     * @return the button
     */
    private JButton mainNamePanelPopUp() {
        JButton mainNamePanelPopUp = new JButton(new AbstractAction("Move Files Via Name Menu") {
            @Override
            public void actionPerformed(ActionEvent e) {
                replacePanel(mainNamePanel());
            }
        });
        mainNamePanelPopUp.setPreferredSize(buttonSize);
        return mainNamePanelPopUp;
    }
    /**
     * when button is pushed changes the panel to the mainURLPanel
     * @return the button
     */
    private JButton mainURLPanelPopUp() {
        JButton mainURLPanelPopUp = new JButton(new AbstractAction("Move Files Via URL Menu") {
            @Override
            public void actionPerformed(ActionEvent e) {
                replacePanel(mainURLPanel());
            }
        });
        mainURLPanelPopUp.setPreferredSize(buttonSize);
        return mainURLPanelPopUp;
    }
}
