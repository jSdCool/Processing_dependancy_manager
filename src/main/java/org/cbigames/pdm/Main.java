package org.cbigames.pdm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
                if(setupLines[lineNumber].charAt(3)=='{' || setupLines[lineNumber].charAt(4)=='{'){
                    lineNumber++;
                    //find how long the lib block is
                    int length = 0;
                    int numBlocks = 1;
                    while(true){
                        if(lineNumber+length >= setupLines.length){
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
                    libraries.add(new LibraryDef(setupLines,lineNumber,length));
                    lineNumber+=length;
                }

            }
        }

        String[] processingLibs = new File(sketchBookLocation+"/libraries").list();

        //prepare for installation
        for(LibraryDef lib: libraries){
            //check to see if the specified lib already exists
            boolean exsists = false;
            if(processingLibs != null) {
                for (String fn : processingLibs) {
                    if (fn.equals(lib.getLibJar())) {
                        exsists = true;
                        break;
                    }
                }
            }
            //if the lib is already installed then check the version
            boolean delete = false;
            if(exsists){
                String fileVersion;
                try(FileInputStream version = new FileInputStream(sketchBookLocation+"/libraries/"+lib.getLibJar()+"/library/version.txt")){
                    fileVersion = new String(version.readAllBytes());
                    if(fileVersion.equals(lib.getLibVersion())){
                        System.out.println("Found the correct version of "+lib.getLibJar()+", skipping");
                        continue;
                    }else{
                        System.out.println("Found an existing version of "+lib.getLibJar()+", but the version is incorrect. installing correct version");
                        delete = true;
                    }
                }catch (IOException i){
                    System.out.println("Found a lib in processing with the same name as one specified in the config, but no version file was found. considering it to be wrong version and re installing");
                    delete = true;
                }

            }
            if(delete){
                //delete the exsisting file in the location
                System.out.println("Deleting previous version of: "+lib.getLibJar());
                deleteFilesInFolder(sketchBookLocation+"/libraries/"+lib.getLibJar()+"/library");
            }else{
                boolean ignored = new File(sketchBookLocation+"/libraries/"+lib.getLibJar()+"/library/").mkdirs();
            }

            //we now have an empty folder, lets download the things
            //find what repo the thing is in
            String repo = null;

            for(String posiobleRep: repos){
                if(repoHasLib(posiobleRep,lib.getLibPackage().replaceAll("\\.","/")+"/"+lib.getLibJar()+"/"+lib.getLibVersion())){
                    repo = posiobleRep;
                    break;
                }
            }

            if(repo == null){
                System.err.println("Unable to find "+lib.getLibJar()+" in any of the supplied repos");
                System.err.println("Program will Exit");
                return;
            }

            String libRepoPath = repo+"/"+lib.getLibPackage().replaceAll("\\.","/")+"/"+lib.getLibJar()+"/"+lib.getLibVersion();
            //download the main library jar
            try {
                System.out.println("Downloading "+lib.getLibJar());
                DownloadFile.download(libRepoPath+"/"+lib.getLibJar()+"-"+lib.getLibVersion()+".jar",sketchBookLocation+"/libraries/"+lib.getLibJar()+"/library/"+lib.getLibJar()+".jar",null);
            } catch (IOException e) {
                throw new RuntimeException("Exception while downloading jar: "+lib.getLibJar(),e);
            }
            //download any native jars
            for(int i=0;i<lib.getLibNatives().size();i++){
                System.out.println("Downloading native lib for "+lib.getLibJar());
                try {
                    DownloadFile.download(libRepoPath+"/"+lib.getLibJar()+"-"+lib.getLibVersion()+"-"+lib.getLibNatives().get(i)+".jar",sketchBookLocation+"/libraries/"+lib.getLibJar()+"/library/native_lib"+i+".jar",null);
                } catch (IOException e) {
                    throw new RuntimeException("Exception while downloading Natives for "+lib.getLibJar(),e);
                }
            }
            //unpack the downloaded natives
            if(!lib.getLibNatives().isEmpty()) {
                System.out.println("Unpacking native libs");
            }
            File destDir = new File(sketchBookLocation+"/libraries/"+lib.getLibJar()+"/library/");
            for(int i=0;i<lib.getLibNatives().size();i++){
                try {
                    ZipInputStream zis = new ZipInputStream(new FileInputStream(sketchBookLocation+"/libraries/"+lib.getLibJar()+"/library/native_lib"+i+".jar"));
                    ZipEntry ze = zis.getNextEntry();
                    while (ze != null){
                        File newFile = newFile(destDir,ze);
                        if(newFile.isDirectory()){
                            if(newFile.getName().equals("META-INF")){
                                ze = zis.getNextEntry();
                                continue;
                            }
                            if(!newFile.mkdirs()){
                                throw new RuntimeException("Failed to create Folder "+newFile);
                            }
                        }else{
                            File parent = newFile.getParentFile();
                            if(parent.getPath().contains("META-INF")){
                                ze = zis.getNextEntry();
                                continue;
                            }
                            if(newFile.getName().equals("META-INF")){
                                ze = zis.getNextEntry();
                                continue;
                            }
                            if (!parent.isDirectory() && !parent.mkdirs()) {
                                throw new IOException("Failed to create directory " + parent);
                            }
                            try(FileOutputStream fos = new FileOutputStream(newFile)){
                                zis.transferTo(fos);//lets see if this works
                            }
                        }
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                    zis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            //finally wright the version file
            try (PrintWriter pr = new PrintWriter(sketchBookLocation + "/libraries/" + lib.getLibJar() + "/library/version.txt")) {
                pr.write(lib.getLibVersion());
                pr.flush();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }


        }



    }

    static boolean repoHasLib(String repo, String libPackageAndVerion){
        try {
            URL url = new URL(repo+"/"+libPackageAndVerion);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            int code = con.getResponseCode();
            return code >= 200 && code <= 299;
        } catch (IOException e) {
            return false;
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    static void deleteFilesInFolder(String folder){
        String[] files = new File(folder).list();
        if(files == null){
            return;
        }
        for(String thing: files){
            File file = new File(folder+"/"+thing);
            if(file.isDirectory()){
                deleteFilesInFolder(folder+"/"+thing);
            }
            boolean ignored = file.delete();
        }
    }

    public enum OS{
        WINDOWS,LINUX,MACOS
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