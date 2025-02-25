package org.cbigames.pdm;

import java.util.ArrayList;

public class LibraryDef {
    private String libPackage = null;
    private String libJar = null;

    private String libVersion = null;

    private ArrayList<String> libNatives = new ArrayList<>();
    private ArrayList<String>  libDependsJars = new ArrayList<>();


    public LibraryDef(String[] file, int start,int length){
        for(int i=start;i<start+length;i++){
            if(file[i].startsWith("package ")){
                libPackage = file[i].split(" ")[1];
                continue;
            }
            if(file[i].startsWith("jar ")){
                libJar = file[i].split(" ")[1];
                continue;
            }
            if(file[i].startsWith("version ")){
                libVersion = file[i].split(" ")[1];
                continue;
            }
            if(file[i].startsWith("native ")){
                libNatives.add(file[i].split(" ")[1]);
                continue;
            }
            if(file[i].startsWith("dependant ")){
                libDependsJars.add(file[i].split(" ")[1]);
                continue;
            }
        }

        if(libJar == null){
            throw new RuntimeException("No jar specified for lib on line "+start);
        }
        if(libPackage == null){
            throw new RuntimeException("No package specified for lib "+libJar);
        }
        if(libVersion == null){
            throw new RuntimeException("No version specified for "+libPackage+":"+libJar);
        }
    }

    @Override
    public String toString() {
        return "LibraryDef{" +
                "libPackage='" + libPackage + '\'' +
                ", libJar='" + libJar + '\'' +
                ", libVersion='" + libVersion + '\'' +
                ", libNatives=" + libNatives +
                ", libDependsJars=" + libDependsJars +
                '}';
    }

    public ArrayList<String> getLibDependsJars() {
        return libDependsJars;
    }

    public ArrayList<String> getLibNatives() {
        return libNatives;
    }

    public String getLibJar() {
        return libJar;
    }

    public String getLibPackage() {
        return libPackage;
    }

    public String getLibVersion() {
        return libVersion;
    }
}
