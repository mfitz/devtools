/**
 *    Copyright 2013 Michael Fitzmaurice
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.michaelfitzmaurice.devtools;

import static org.apache.commons.io.FileUtils.listFiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reports on files in a given directory that do not contain
 * a specified header. Lists files recursively, with optional
 * filtering based on file extension. Can also be run in
 * insert mode, whereby it will insert the header at the 
 * beginning of any file that lacks it. 
 * 
 * Also includes the option to match only against the first 
 * line of the header, which I included because the licence 
 * headers I use in personal open source projects typically 
 * include the year in the copyright statement. When a header 
 * is present in a file, but with copyright statement using
 * a different year from that of the specified header, I don't 
 * want to insert the header on top of the old one. Essentially 
 * I just want to automatically insert the licence header into 
 * any file that does not currently have a licence header. The
 * first line matching option does that for me.
 * 
 * @author Michael Fitzmaurice, July 2013
 */
public class HeaderTool {
    
    /**
     * Determines how the match against the header will be carried out
     */
    public enum MatchMode { FULL_MATCH, FIRST_LINE_ONLY };
    
    private static final String NEWLINE = System.getProperty("line.separator"); 
    
    private final String header;
    private final MatchMode matchMode;
    
//private static void setOptions() {
//        
//        verbose = Boolean.getBoolean("verbose-mode");
//        if (verbose) {
//            System.out.println("Using verbose mode");
//        }
//        
//        matchOnFirstLineOnly = 
//                Boolean.getBoolean("match-header-first-line-only");
//        if (matchOnFirstLineOnly) {
//            System.out.println("Matching against first line only");
//        }
//        
//        insertHeader = Boolean.getBoolean("insert-header");
//        if (insertHeader) {
//            System.out.println("In insert header mode");
//        }
//    }
    
    /**
     * Constructs a new instance of <code> HeaderTool</code>.
     * 
     * @param headerFile The file containing the header to check against
     * @param mode Whether to look for the entire header text, or just
     *        the first line
     * 
     * @throws IOException If something goes wrong reading the file 
     *         containing the header
     */
    public HeaderTool(File headerFile, MatchMode mode) 
    throws IOException {
        
        this.header = fileContents(headerFile);
        this.matchMode = mode;
    }
    
    /**
     * Recursively searches a directory for files matching a
     * given list of file extensions that do not begin with
     * the string found in the header file.
     * 
     * @param rootDir The directory to begin the search from
     * @param fileExtensions an array of extensions, e.g. {"java","xml"}. 
     *          If this parameter is null, all headerless files are 
     *          returned, regardless of file extension.
     * 
     * @return The files that do not begin with the header string (or
     *         first line of the header string, if MatchMode.FIRST_LINE_ONLY
     *         was specified at construction time).
     * 
     * @throws IOException If something goes wrong reading the content
     *         of any of the files being scanned
     */
    public Collection<File> listFilesWithoutHeader(File rootDir, 
                                            String[] fileExtensions)
    throws IOException {
        
        List<File> filesWithNoHeader = new ArrayList<File>();
        String toMatch = header;
        if (matchMode == MatchMode.FIRST_LINE_ONLY) {
            String firstLineOfHeader = header.split(NEWLINE)[0];
            toMatch = firstLineOfHeader;
        }
        
        Collection<File> filesInDir = listFiles(rootDir, fileExtensions, true);
        for (File file : filesInDir) {
            if (fileContents(file).startsWith(toMatch) == false) {
                filesWithNoHeader.add(file);
            }
        }
        
        return filesWithNoHeader;
    }
    
    /**
     * Inserts the header at the beginning of each file.
     * Does not check whether or not the header is already
     * present.
     * 
     * @param files The files to be amended
     * @throws IOException If something goes wrong reading 
     *          from or writing to any of the files
     */
    public void insertHeader(List<File> files) 
    throws IOException {
        
        for (File file : files) {
            String originalFileContent = fileContents(file);
            FileWriter writer = new FileWriter(file);
            writer.write(header);
            writer.write(originalFileContent);
            writer.close();
        }
    }
    
    private String fileContents(File file) 
    throws IOException {
             
        StringBuffer contentBuffer = new StringBuffer();
        FileReader fileReader = new FileReader(file);
        BufferedReader bufReader = new BufferedReader(fileReader);
        String line = bufReader.readLine();
        while (line != null) {
            contentBuffer.append(line);
            contentBuffer.append(NEWLINE);
            line = bufReader.readLine();
        }
        bufReader.close();

        return contentBuffer.toString();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
