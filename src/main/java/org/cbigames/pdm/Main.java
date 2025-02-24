package org.cbigames.pdm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
         if(args.length <1){
             System.out.print("Usage: ");
             System.out.print("java -jar pdm.jar /path/to/setup.depends");
             return;
         }
         OS currentOS = detectOS();

         //get the path of the processing preferences file
         String processingPrefrencePath="";

         if(currentOS == OS.WINDOWS){
             processingPrefrencePath = System.getenv("appdata")+"\\Processing\\preferences.txt";
         }
         //do other oses here

         String sketchBookLocation ="";
         //read the preferences file to find the sketchbook location
         try(Scanner fin = new Scanner(new File(processingPrefrencePath))){
             while(fin.hasNextLine()){
                 String line = fin.nextLine();
                 if(line.startsWith("sketchbook.path")){
                     sketchBookLocation = line.split("=")[1];
                     break;
                 }
             }
         } catch (FileNotFoundException e) {
             System.err.println("Unable to read processing preferences file!");
             return;
         }


        ArrayList<String> repos = new ArrayList<>();
        ArrayList<LibraryDef> libraries = new ArrayList<>();
        //default repo
        repos.add("https://repo1.maven.org/maven2");


        //parse the input file
        String setupFileContent;
        try(FileInputStream setupFile = new FileInputStream(args[0])){
            setupFileContent = new String(setupFile.readAllBytes());
        }catch (IOException e){
            throw new RuntimeException("Unable to read depends file",e);
        }

        String[] setupLines = setupFileContent.split("\n");
        for(int i=0;i<setupLines.length;i++){
            setupLines[i] = setupLines[i].trim();
        }

        for(int lineNumber =0;lineNumber<setupLines.length;lineNumber++){
            //if the current line is defining a repo
            if(setupLines[lineNumber].startsWith("repo")){
                repos.add(setupLines[lineNumber].split(" ")[1]);
                continue;
            }
            //if the line is defining a lib
            if(setupLines[lineNumber].startsWith("lib")){
                if(setupLines[lineNumber].charAt(3)=='{'){
                    //find how long the lib block is
                    int length = 0;
                    int numBlocks = 1;
                    while(true){
                        if(length+length>= setupLines.length){
                            throw new RuntimeException("Reached end of file before end of lib block");
                        }
                        if(setupLines[lineNumber+length].charAt(0)=='}'){
                            numBlocks --;
                        }
                        if(numBlocks == 0){
                            break;
                        }
                        if(setupLines[lineNumber+length].contains("{")){
                            numBlocks ++;
                        }
                        length++;
                    }
                    libraries.add(new LibraryDef(setupLines,lineNumber+1,length));
                    lineNumber+=length;
                }

            }
        }

    }

    public enum OS{
        WINDOWS,LINUX,MACOS;
    }

    static OS detectOS() {
        String name = System.getProperty("os.name");
        name=name.toLowerCase();
        if(name.contains("windows")) {
            return OS.WINDOWS;
        }
        if(name.contains("linux")) {
            return OS.LINUX;
        }
        if(name.contains("mac")) {
            return OS.MACOS;
        }
        return OS.LINUX;
    }
}