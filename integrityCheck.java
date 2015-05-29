/*
//#### Pre Face ####
 Authored by Harish VK for Professor Michael Witt
The following code takes command line input and logs errors
The following Assumptions are made with regard sto the Environment that runs this program:
1. The running system has the latest version of java
2. There is no adversary that will delete the $METADATA.txt File in any of the directries. Though there is a checksum maintained for the $METADATA.txt, it is not checked in the program

INPUT : Path to to Directory / File 
Working :
1. For folder Input, Traverses all files in the Input folder and its sub folders and calculates their MD5 hash
     If there is an existing entry in the $METADATA.txt for this file, then the calculated hash is checked against that value
        else, a new entry is created and the File Name, hash value of the file and the Last Modified Time are created 
2. For a File Input, the just file's hash is checked agaisnt the hash value stored in the $METADATA.txt present in the folder containing the particular file
OUPUT: If the Final line of the output says "Integrity Check Successful for all the file and sub-folders inside .." only then it means the test ran successfully, else check the log file for errors

LOG FILE:(errorLog.txt)
The general Structure is as follows:
<ERROR MESSAGE><FILENAME><TIME AT WHICH THE LOG WAS CREATED><Last Legitimate Access Time>
By default, the file is stored in "C:\\errorLog.txt"
This can be changed though at the Following line Number: 

-compare option:
The compare option takes the inputs of two directories and says if files are missing in one of the directories when compared with the other. 
The input and output directories should be seperated by a "#"
It is important to note that even in this option, bad checksum is checked for in both of the source and destination directories and Errors are logged

Legitimate Modification:
Also whenever the there is a change made to a file, legitimately, the checksum should be updated. Else during the running, it will throw a bad check sum error. 
The update should be made with the help of "-update" option and the input should be that of the directory that contains the file. This option should be run carefully, because every time this is run, the check sum of all 
files in the directory will be overwritten and hence if there was some other file in the directory that had an illegal modification to it, this will go unnoticed.


*/
package checkpurr;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Calendar;
public class CheckPurr {
   static  boolean integrityChecker =true;
public static void main(String[] args)   {
try {
StringBuffer hexString = new StringBuffer();
StringBuffer digestValue = new StringBuffer();
String currentDirectory = new String();
String []temp;
String fileName = "TO DO.txt";
int h,size;
String sourceDirectory, destinationDirectory;
//#### Check if no command line arguments  ####
if(args.length == 0) {
    System.out.println("Invalid Input \n Valid Input : \n" + 
            "java -jar <jar file name>.jar  <option>  <Directory 1># <Directory 2> \n or"+
            "java -jar <jar file name>.jar  <option>  <Directory 1>\n"+
            "Valid Options : -compare , -update "
            );
    System.exit(0);
}
//#### SOURCE DESTINATION CHECK ####
//Logic: Source and Destination seperated by #, Run through all files in the Sousce and its subfolders and check if it is present in the Destiantion and its sub-folders
//If the source or destination does not contain METADATA, first create that
//If present, proceed with checking entries in METADATA in the corresponding folders
// 
File folder = null;
if(args[0].equalsIgnoreCase("-compare")) { //Store all of user input including spaces onto the variable currentDirectory 
    File[] sourcePathList = null; 
    File[] destinationPathList = null;
    currentDirectory = args[1];
    for(h = 2;h < args.length; h ++) {
        currentDirectory += " ";
        currentDirectory += args[h];
    }
if(!currentDirectory.contains("#")) { //Check for #
    System.out.println("Usage Error. use # to seperate the source and destination directories");
    System.exit(0);
}
temp = currentDirectory.split("#"); // Spit the sourceDirectory(directory 1) and destinationDirectory(directory 2) 
sourceDirectory = temp[0];
destinationDirectory = temp[1];
File sourcePath = new File(sourceDirectory);
File destinationPath = new File(destinationDirectory);
if(!sourcePath.isDirectory() || !destinationPath.isDirectory() ) {
    System.out.println("Invalid Input: Please Enter a Directory (Path) of the list of files ");
    System.exit(0); 
}
folderDigest(sourcePath); //First check for Check sum error in each of the directories
folderDigest(destinationPath);
folderCompare(sourcePath, destinationPath ); //Now Compare for missing Files
System.exit(0);
}

if(args[0].equals("-update")) {
 currentDirectory = args[1];
for(h = 2;h < args.length; h ++) {
currentDirectory += " ";
currentDirectory += args[h];
}  
Path toDelete = Paths.get(currentDirectory+"\\$METADATA.txt");
 Files.delete(toDelete);
}
//#### if the directory had spaces in between, the following section will append all these together  ####
//#### Ex: args[0]C:\Google args[1]Drive ==> currentDirectory:C:\Google Drive ####
else 
{
    currentDirectory = args[0];
    for(h = 1;h < args.length; h ++) {
currentDirectory += " ";
currentDirectory += args[h];
}
        }
//#### Appropriate Call
folder = new File(currentDirectory); 
//if(folder.isFile()) {
  //fileDigest(folder);
//}
//else {
    folderDigest(folder);
    if(integrityChecker) System.out.println("Integrity Check Successful for all the file and sub-folders inside" + currentDirectory);
    else System.out.println("Some file had Illegal Modifications, check Log File");
//}
}
       catch(NoSuchAlgorithmException e){
          System.out.println(e);
        }
       catch(FileNotFoundException e){
           System.out.println(e);
        }
        catch(IOException e){
           System.out.println(e);  
        } 
    }

//#### Just Checks the Number of files to check if we have to look Further####
//Input : Two Directories, Data type: File
//Action: Calls pairwiseCheck() if there are unequal number of files inside the directories that were Input. Else, Does nothing
public static void folderCompare(File sourceDirectory, File destinationDirectory) throws IOException, NoSuchAlgorithmException {
File f = null; int i,j;
File[] sourceSubFolder = sourceDirectory.listFiles();
File[] destinationSubFolder = destinationDirectory.listFiles();
//First compare the files in the Given Directoris
pairwiseCheck(sourceDirectory, destinationDirectory );
 //Then compare in sub directories in each of the parent directories 
for(i=0,j=0 ; i<sourceSubFolder.length && j<destinationSubFolder.length  ; i++, j++) {
    if(sourceSubFolder[i].isDirectory() && (destinationSubFolder[j].isDirectory()) ){
        folderCompare(sourceSubFolder[i], destinationSubFolder[j] ); // Recurse to dive into sub directories
    }
}
if(i<sourceSubFolder.length) { //End case when there are directories missing
    
}
}
//#### The Function would find the Hash of a given File ####
//#### Inputs Filename(With Full path) ####
//#### Output Digest in StringBuffer format ####
public static void fileDigest(File currentDirectory, File actual_file) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
int size;
StringBuffer hexString = new StringBuffer();
FileInputStream f = new FileInputStream(actual_file);
size = (int) actual_file.length();
byte[] barray = new byte[size];
long checkSum = 0L;
int nRead;
//#### Empty File ####
if(size==0) {
  System.out.println("Encountered an Empty File"); 
}
//#### Read the file as a byte array into barray if the file is not empty  ####//
else {
while ( (nRead=f.read( barray, 0, size)) != -1 )
{
    for (int i=0; i<nRead; i++ ) {
           checkSum += barray[i]; 
    }
}
}
MessageDigest md = MessageDigest.getInstance("MD5");
byte[] digestbytes = md.digest(barray);
//#### Convert the obtained hash into a Hex String####as
for (int i = 0; i < digestbytes.length; i++) {
            if ((0xff & digestbytes[i]) < 0x10) {
                hexString.append("0"
                        + Integer.toHexString((0xFF & digestbytes[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & digestbytes[i]));
            }
        }        
 updateMetadata(currentDirectory, actual_file, hexString);
}

public static void folderDigest(File folderInput) throws IOException, FileNotFoundException, NoSuchAlgorithmException {
   File f = null;
   boolean bool = false;
    f = new File(folderInput+"\\$METADATA.txt");
    bool = f.createNewFile();
    System.out.println("Metadata file Created "+bool);
    String content = "Blah";
    File[] insideFolder = folderInput.listFiles();
    for(int i=0 ; i<insideFolder.length ; i++) {
    bool =false;
    if(insideFolder[i].isDirectory()){
        System.out.println("Searching all the Files, please wait..");
         folderDigest(insideFolder[i]);
    }
    else{
        fileDigest(folderInput,insideFolder[i]);    
    }    
}
}
//#### UPDATES META DATA####
//Given the folder, checks for the metadata file, if it does not exist, then create it.
//If exists, checks for the particular file's record. If not, creates a new entry and appends the hash  
//
public static void updateMetadata(File folderInput, File actual_file ,StringBuffer digest ){
    try {
        String digestString = new String(digest);
     BasicFileAttributes attr = null;    
     int lineNumber =-1;
    boolean flagCheck= false; //True when the entry for the file is in metadata file
//Reading Metadta File
    //System.out.println("Checking Hash for  "+actual_file.getName()+"....");
    Path file = Paths.get(folderInput+"\\$METADATA.txt");
    Path FFF = Paths.get(actual_file.getAbsolutePath());
    attr = Files.readAttributes(FFF, BasicFileAttributes.class);
    String actual_file_creationTime ;
    //    actual_file_creationTime = new String((String)attr.creationTime());
    String content = new String(Files.readAllBytes(file));
        String[] arrayOfStrings = content.split("\n");
    for(int i=0;i<arrayOfStrings.length; i++) {
        if(arrayOfStrings[i].contains(actual_file.getName())) {
            flagCheck = true;
        lineNumber = i;
        break;
        }        
    }
        //Logic: Get the contents of the file as array of strings search each index for the file name and also check with the creation time
        //If present, then modify the string in that index and write back to the file 
    if(flagCheck ) {
        //System.out.println("File entry already present..Checking Hash Values..");
            String[] requiredEntry = arrayOfStrings[lineNumber].split("\\*");
            //System.out.println(requiredEntry[1]+"and "+digestString);
          if(digestString.equals(requiredEntry[1])) {
              System.out.print("Success..\n\n");    
          }
          else {
              if("$METADATA.txt".equals(actual_file.getName())) ;
              else{
                  System.out.println("Illegal Modification to"+folderInput+"\\"+actual_file.getName()+"\n Last Known Modification"+attr.lastModifiedTime());
                  integrityChecker=false;
                  errorLog("Bad Checksum for file#"+actual_file.getPath());
              }
              
          }
    }
    else { 
    //Writes to Metadata
    File FF = new File(folderInput+"\\$METADATA.txt");
    FF.createNewFile();
    FileWriter fw = new FileWriter(FF,true); //the true will append the new data
    //Store file creation time and Corresponding Digest:
    System.out.println("Writing Hash for"+actual_file.getName());
    fw.write(actual_file.getName()+"*"+digest+"*"+attr.lastModifiedTime()+"\n");//appends the string to the file
    fw.close();
    }
    }
    catch(FileAlreadyExistsException ex) {
      // System.out.println(ex);
    } catch (IOException ex) {
        System.out.println(ex);
    }
}
//#### Logs the Files that had unmatching hashes ####
//errorLog.txt will have the details like the absolute path name and time at which the error was logged
public static void errorLog( String errorMessage) throws IOException {
 File logFile = new File("F:\\errorLog.txt");
    logFile.createNewFile();
    FileWriter fw = new FileWriter(logFile,true); //the true will append the new data
    //Store file creation time and Corresponding Digest:
    System.out.println("Logging Error");
    //Getting Time:
    Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	//System.out.println( sdf.format(cal.getTime()) );
    
    fw.write(errorMessage +"#Time of Logging:#" +  sdf.format(cal.getTime())+"\n");//appends the string to the file
    fw.close();
}

//#### Checks Source and Destiantion ####
//Checks the corresponding Directories, Metadata Fiels and Logs File Missing and the like errors. 
//Logic for METADATA check: First read as a ful lstring and directly compare. If there is mismatch, probe deep in.
public static void pairwiseCheck(File sourcePath,File destinationPath) throws IOException {
String[] compare1 =null ;
String[] compare2 = null;
//Boolean ifSubDirectoryExists = false;
File[] listSource = null; File[] listDestination = null;
listSource = sourcePath.listFiles();
listDestination = destinationPath.listFiles();
//Logic for the following code:
//See if there are any sub directories inside the given directory: If yes dive into that sub directories else return to what it is actually meant to do
//for(int i=0, j=0 ;  i<listSource.length&& j<listDestination.length ; i++, j++) {
   // if(listSource[i].isDirectory()&&listDestination[j].isDirectory())  {
        //ifSubDirectoryExists = true;
      //  pairwiseCheck(listSource[i] ,listDestination[j] );
    //}
//}
Path rootMetaFileSource = Paths.get(sourcePath+"\\$METADATA.txt");
Path rootMetaFileDestination = Paths. get(destinationPath+"\\$METADATA.txt");
String rootMetaFileDestinationString = new String(Files.readAllBytes(rootMetaFileDestination));
String rootMetaFileSourceString = new String(Files.readAllBytes(rootMetaFileSource));
if(rootMetaFileDestinationString.length()==rootMetaFileSourceString.length()) {
    System.out.println("Root looks Good");
}
else // This case the metadata files do not match // check to see if the entire record is missing in which case, the file's entry should go missing //Entry => Look in the entry for file name and whether missing in source or destiantion.
{ 
    compare1 = rootMetaFileSourceString.split("\n");
    compare2 = rootMetaFileDestinationString.split("\n");
    String compare11[] = null; String compare21[];
    //Following case for files missing in Destination
    if(rootMetaFileDestinationString.length() < rootMetaFileSourceString.length()) { // Check which entry goes missing
       for(int i=0, j=0; i<compare1.length ;) {
           compare11 = compare1[i].split("\\*");
            compare21 = compare2[j].split("\\*");
           if(compare11[0].equals(compare21[0]) && compare11[1].equals(compare21[1]) ) { //Check if there corresponding entries for filename and hash are present. 
               i++; 
               if(j<compare2.length-1)j++; //Bounds check for j
           }  
           else {  
               
              
              String  forLogging = new String(destinationPath.getAbsolutePath()+"\\"+compare11[0]);
              //Path pathForLogging = Paths.get(destinationPath.getAbsolutePath()+"\\"+compare11[0]);
               //BasicFileAttributes attr1= Files.readAttributes(pathForLogging, BasicFileAttributes.class);
              errorLog("Missing File#"+ forLogging+"# when compared with#"+sourcePath.getAbsolutePath());
               i++;
           } 
       }  
    }
    //Following case for Files missing in source (What is in the destiantion is not here
    else if(rootMetaFileDestinationString.length() > rootMetaFileSourceString.length()) {
         for(int i=0, j=0; j<compare2.length ;) {
           compare11 = compare1[i].split("\\*");
           compare21 = compare2[j].split("\\*");
           if(compare11[0].equals(compare21[0]) && compare11[1].equals(compare21[1])) {
              if(i<compare1.length-1) i++; //Bounds check for i; 
               j++;
           }  
           else {
               
              String forLogging = new String(sourcePath.getAbsolutePath()+"\\"+compare21[0]);
              //Path pathForLogging = Paths.get(sourcePath.getAbsolutePath()+"\\"+compare21[0]);
              //BasicFileAttributes attr2= Files.readAttributes(pathForLogging, BasicFileAttributes.class);
              errorLog("Missing File#"+forLogging+"#when compared with#"+destinationPath.getAbsolutePath());
              j++;
           }
       }  
    }
    //System.out.println("");
    //errorLog(,)
}
//for(i=0, j=0; i<listSource.length && j<listDestination.length ;  ) {

//i++; j++;    
//}
}
}


//#### Assumptions ####
//1.  Does not handle same file names in the Directory
//2. 
//3. 
//4 .


   /* Works for file overwriting
         FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
         */ 


//#### Left OFF ####//
// Checking metadata file for the missing files, see if the listFiles uses the same order for listing 
// 


/*      folder digest function
         //f = new File(insideFolder[i]+"\\$METADATA.txt");
         //bool = f.createNewFile();
         /*
         if(bool) {
         System.out.println("Metadata file Created "); 
         }*/
         //System.out.println("Blah"); 
         /*FileWriter fw = new FileWriter(f,true); //the true will append the new data
         fw.write("add a line\n");//appends the string to the file
         fw.close();
         FileInputStream fi = new FileInputStream(insideFolder[i]+"\\$METADATA.txt");
         //String targetFileStr = new String(Files.readAllBytes();
###################
main last part
//String pathOfFile = new String(currentDirectory+"\\"+fileName);
//FileInputStream f = new FileInputStream(pathOfFile);
//File[] folderList = folder.listFiles();
//
//hexString = fileDigest(actual_input);
//System.out.println(hexString);  

*/


