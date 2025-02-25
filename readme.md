# Processing Dependency Manager
A simple program to manage and download standard java libraries as processing libraries 

## Usage
Simply run the program jar with the path to your projects depends file as a parameter
```shell
java -jar pdm.jar /path/to/setup.depends
```

## Depends file format
This file format is vaguely inspired by how gradle handles dependencies.  
Each specified library requires a few parameters that are referred to by different names in the maven syntax.

| PDM     | Maven      |
|---------|------------|
| package | groupId    |
| version | version    |
| jar     | artifactId |

This data represented in the Gradle implementation syntax would be
```grale
implementation 'package:jar:version'
```
Repos can be defined by putting the word `repo` on a line followed by the link to the repo. The link should be the same as what would be used to define a repo in gradle.

Libraries are defined inside a `lib` block. the block is bounded by `{}` the opening `{` must be on the same line as the word lib. The closing `}` must be on its own line.
Each field specified inside a lib block must be on its own line.  
The following fields can exist inside a lib block:  
`package` the . separated package of the lib. This field is required.  
`jar` the name of the library/primary name of the jar file. This is what the library will be named in processing. This field is required.  
`version` the version of the lib you want to install as specified by the lib author. This field is required.  
`native` additional information about a jar in the same package on the repo that contains native code to be extracted. You may have 0 - Integer.MAX_VALE of this field in each lib block

### example
```depends

//comment
//define our own repo
repo https://maven.mydomain.com/repository

//note: the default maven repo is automatiacly included (https://repo1.maven.org/maven2)
//you can define as many repos as is required

lib{
    package net.example.lib
    jar example1
    version 1.0.0+snapshot
    //the main jar that will be downloaded from is: example1-1.0.0+snapshot.jar this file will be saved as: example1.jar
    native natives-all
    //the native line will download the folowing file: example1-1.0.0+snapshot-natives-all.jar
    //its contense will then be extracted to the library folder minus the META-INF folder
}

//you can list as many libs as you need for your project 

//supermath-2.8.9.jar
lib{
    package com.utils.useful
    jar supermath
    version 2.8.9
}

```