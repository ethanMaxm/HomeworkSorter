# HomeworkSorter

PROJECT TITLE: HomeWorkSorter
PURPOSE OF PROJECT:  To Semi-Automate the process of organizing downloaded files 
VERSION or DATE:  11/16/23
HOW TO START THIS PROJECT: First you must add the library sqlite-jdbc-3.41.2.1 to the IDE which is located in the main folder of the program. Then You may run the program
AUTHORS: Ethanmaxm
USER INSTRUCTIONS:

The program has only been tested on Mac and windows.
The program should works with google chrome but should work on all chromium browsers with a bit of additional user input, although I've only tested it on Microsoft edge.


Run the Main method.
After the program is run for the first time, if you are not using google chrome, click the on the "move files via url menu" and then "select history” and try the following path to find the history file for your browser. (Google chrome is used as an example, but you should not actually need to do this if you are using google chrome).
Windows
C:\Users<username>\AppData\Local\Google\Chrome\User Data\Default\History
C:\Users<username>\AppData\Local\Google\Chrome\User Data\Default\Cache\History
macOS
/Users/<username>/Library/Application Support/Google/Chrome/Default/History
/Users/<username>/Library/Caches/Google/Chrome/Default/Cache/History

After that click the add string button and select “add path” and click on the folder you would like to send files to.

Click on the new buttons that are not “new path” to enter the substring that you would like to have sent to the folder. 

To actually move the files click move files, although this will be done automatically every minute. The move all files button moves all files whose source urls have contain the given substring. The not move all files will only look at the ones from about the last month or so.

To delete a substring click not the delete button, the folder you would like to delete an associated substring for and then the substring you want deleted.

To move files based off of their name return to the main menu and select "Move Files Via Name." Then select add download folder, the new path, then select the folder you would like to move files from. Then click on the button with that folders name and click new paths to select the folder you want files to be moved to. Then click on the new button to add a substring that the file names should contain. 
Note: if the source folder and destination folder have the same name something will break. This will also happen if two destination folders have the same name and the same source folder, or if two source folders have the same name.

To actually move the files either wait up to a minute or click the move files button. The move all files button moves all files from the source folder to the destination folder that have the substring in their name. The move files (not all) will only do this for all files added since the last time it was ran.
