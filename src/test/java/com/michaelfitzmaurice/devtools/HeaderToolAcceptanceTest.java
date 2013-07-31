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

import static com.michaelfitzmaurice.devtools.FileListBuilder.aFileList;
import static com.michaelfitzmaurice.devtools.HeaderTool.FIRST_LINE_MATCH_SYS_PROP;
import static com.michaelfitzmaurice.devtools.HeaderTool.INSERT_MODE_SYS_PROP;
import static com.michaelfitzmaurice.devtools.HeaderTool.WILDCARD_FILE_EXTENSION;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Launches HeaderTool as an application using different combinations 
 * of runtime arguments and system properties, then makes assertions 
 * about its behaviour. Does not touch any of the method-level API of 
 * HeaderTool, other than to call the <code>main()</code> method, thus 
 * replicating running the app from the command line.
 * 
 * @author Michael Fitzmaurice
 */
public class HeaderToolAcceptanceTest extends HeaderToolTest {
    
    private File sourceDir;
    private String[] fileExtensions;
    
    @Before
    public void setup() {
        sourceDir = new File(TMP_ROOT_DIRECTORY, "root/subA");
        fileExtensions = new String[] {WILDCARD_FILE_EXTENSION};
    }
    
    @Test
    public void doesNotInsertHeaderByDefault()
    throws IOException {
        
        List<File> headerlessFiles  = 
                aFileList()
                    .withFile(sourceDir, "subA1/NoHeader.java")
                    .withFile(sourceDir, "subA1/DifferentHeader.java")
                    .withFile(sourceDir, "subA1/no-header.txt")
                    .withFile(sourceDir, "subA1/different-header.txt")
                    .withFile(sourceDir, "subA2/NoHeader.java")
                    .withFile(sourceDir, "subA2/DifferentHeader.java")
                    .withFile(sourceDir, "subA2/no-header.txt")
                    .withFile(sourceDir, "subA2/different-header.txt")
                    .build();
        assertFilesLackHeader(headerlessFiles);
        
        assertFalse( isInsertModeOn() );
        runHeaderTool();
        assertFilesLackHeader(headerlessFiles);
    }

    @Test
    public void insertModeInsertsHeaderIntoAllHeaderlessFilesForWildcardFileExtension() 
    throws IOException {
     
        List<File> files  = 
            aFileList()
                .withFile(sourceDir, "subA1/NoHeader.java")
                .withFile(sourceDir, "subA1/DifferentHeader.java")
                .withFile(sourceDir, "subA1/no-header.txt")
                .withFile(sourceDir, "subA1/different-header.txt")
                .withFile(sourceDir, "subA2/NoHeader.java")
                .withFile(sourceDir, "subA2/DifferentHeader.java")
                .withFile(sourceDir, "subA2/no-header.txt")
                .withFile(sourceDir, "subA2/different-header.txt")
                .build();
        assertFilesLackHeader(files);
        
        switchOnInsertMode();
        try {
            runHeaderTool();
        } finally {
            switchOffInsertMode();
        }
        
        assertFilesHaveHeader(files);
    }

    @Test
    public void insertModeDoesNotModifyFilesThatContainHeader()
    throws IOException {
        
        List<File> filesWithHeader  = 
            aFileList()
                .withFile(sourceDir, "subA1/Header.java")
                .withFile(sourceDir, "subA1/header.txt")
                .withFile(sourceDir, "subA2/Header.java")
                .withFile(sourceDir, "subA2/header.txt")
                .build();
        assertFilesHaveHeader(filesWithHeader);
        Map<String, FileSizeAndDate> originalFileSizesAndDates = 
            fileSizesAndDates(filesWithHeader);
        
        switchOnInsertMode();
        try {
            runHeaderTool();
        } finally {
            switchOffInsertMode();
        }
        
        assertFilesHaveHeader(filesWithHeader);
        Map<String, FileSizeAndDate> newFileSizesAndDates = 
            fileSizesAndDates(filesWithHeader);
        assertFilesUnchanged(originalFileSizesAndDates, newFileSizesAndDates);
    }
    
    @Test
    public void insertModeOnlyInsertsHeaderIntoHeaderlessFilesMatchingSuppliedFileExtensions() 
    throws IOException {
     
        fileExtensions = new String[] {"java"};
        List<File> filesThatShouldChange  = 
            aFileList()
                .withFile(sourceDir, "subA1/NoHeader.java")
                .withFile(sourceDir, "subA1/DifferentHeader.java")
                .withFile(sourceDir, "subA2/NoHeader.java")
                .withFile(sourceDir, "subA2/DifferentHeader.java")
                .build();
        assertFilesLackHeader(filesThatShouldChange);
        
        List<File> filesThatShouldNotChange  = 
                aFileList()
                    .withFile(sourceDir, "subA1/no-header.txt")
                    .withFile(sourceDir, "subA1/different-header.txt")
                    .withFile(sourceDir, "subA2/no-header.txt")
                    .withFile(sourceDir, "subA2/different-header.txt")
                    .build();
        assertFilesLackHeader(filesThatShouldNotChange);
        
        switchOnInsertMode();
        try {
            runHeaderTool();
        } finally {
            switchOffInsertMode();
        }
        
        assertFilesHaveHeader(filesThatShouldChange);
        assertFilesLackHeader(filesThatShouldNotChange);
    }
    
    @Test
    public void insertModeDoesNotInsertHeaderIntoFilesWithDifferentHeaderIfFirstLineMatchesInFirstLineMode()
    throws IOException {
        
        List<File> filesWithDifferentHeader  = 
                aFileList()
                    .withFile(sourceDir, "subA1/DifferentHeader.java")
                    .withFile(sourceDir, "subA1/different-header.txt")
                    .withFile(sourceDir, "subA2/DifferentHeader.java")
                    .withFile(sourceDir, "subA2/different-header.txt")
                    .build();
        assertFilesLackHeader(filesWithDifferentHeader);
        
        switchOnInsertMode();
        switchOnFirstLineMatching();
        try {
            runHeaderTool();
        } finally {
            switchOffInsertMode();
            switchOffFirstLineMatching();
        }
        
        assertFilesLackHeader(filesWithDifferentHeader);
    }

    ///////////////////////////////////////////////////////
    // helper methods
    ///////////////////////////////////////////////////////
    
    private static void switchOffInsertMode() {
        System.clearProperty(INSERT_MODE_SYS_PROP);
    }

    private static void switchOnInsertMode() {
        System.setProperty(INSERT_MODE_SYS_PROP, "true");
    }
    
    private static boolean isInsertModeOn() {
        return Boolean.getBoolean(INSERT_MODE_SYS_PROP);
    }
    
    private static void switchOffFirstLineMatching() {
        System.clearProperty(FIRST_LINE_MATCH_SYS_PROP);
    }

    private static void switchOnFirstLineMatching() {
        System.setProperty(FIRST_LINE_MATCH_SYS_PROP, "true");
    }
    
    private void runHeaderTool() 
    throws IOException {
        HeaderTool.main( runtimeArgs() );
    }
    
    private String[] runtimeArgs() {
        
        String[] args = new String[2 + fileExtensions.length];
        args[0] = sourceDir.getAbsolutePath();
        args[1] = HEADER_FILE.getAbsolutePath();
        System.arraycopy(fileExtensions, 0, args, 2, fileExtensions.length);
        
        return args;
    }
    
    private Map<String, FileSizeAndDate> fileSizesAndDates(
                                            Collection<File> files) {
        
        HashMap<String, FileSizeAndDate> sizesAndDates = 
            new HashMap<String, FileSizeAndDate>();
        for (File file : files) {
            sizesAndDates.put(
                file.getAbsolutePath(),
                new FileSizeAndDate( file.lastModified(), 
                                    file.length() ) );
        }
        
        return sizesAndDates;
    }
    
    private void assertFilesUnchanged(Map<String, FileSizeAndDate> first, 
                                    Map<String, FileSizeAndDate> second) {
        
        String failMsg = "Map sizes differ - cannot be compared";
        assertEquals( failMsg, first.size(), second.size() );
        
        for ( String key : first.keySet() ) {
            FileSizeAndDate firstSizeAndDate = first.get(key);
            FileSizeAndDate secondSizeAndDate = second.get(key);
            
            long firstSize = firstSizeAndDate.getSizeInBytes();
            long secondSize = secondSizeAndDate.getSizeInBytes();
            failMsg = 
                format("%s has been modified; file size differs (%s Vs %s)",  
                        key, 
                        firstSize, 
                        secondSize);
            assertEquals(failMsg, firstSize, secondSize);
            
            long firstModDate = firstSizeAndDate.getLastModified();
            long secondModDate = secondSizeAndDate.getLastModified();
            failMsg = 
                format("%s has been modified; mod dates differ (%s Vs %s)", 
                        key, 
                        firstModDate, 
                        secondModDate);
            assertEquals(failMsg, firstModDate, secondModDate);
        }
    }
    
    /**
     *  Data object to record information about a file that will change 
     *  if the file is written to.
     */
    private class FileSizeAndDate {
        
        private final long lastModified;
        private final long lengthInBytes;
        
        public FileSizeAndDate(long lastModified, 
                                long lengthInBytes) {
            super();
            this.lastModified = lastModified;
            this.lengthInBytes = lengthInBytes;
        }

        private HeaderToolAcceptanceTest getOuterType() {
            return HeaderToolAcceptanceTest.this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result
                    + (int) (lastModified ^ (lastModified >>> 32));
            result = prime * result
                    + (int) (lengthInBytes ^ (lengthInBytes >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            
            FileSizeAndDate other = (FileSizeAndDate) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (lastModified != other.lastModified)
                return false;
            if (lengthInBytes != other.lengthInBytes)
                return false;
            
            return true;
        }

        @Override
        public String toString() {
            return "FileSummary [lastModified=" + lastModified
                    + ", lengthInBytes=" + lengthInBytes + "]";
        }

        long getLastModified() {
            return lastModified;
        }

        long getSizeInBytes() {
            return lengthInBytes;
        }
    }
}
