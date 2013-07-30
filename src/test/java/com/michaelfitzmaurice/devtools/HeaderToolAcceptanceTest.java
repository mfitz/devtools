package com.michaelfitzmaurice.devtools;

import static com.michaelfitzmaurice.devtools.FileListBuilder.aFileList;
import static com.michaelfitzmaurice.devtools.HeaderTool.FIRST_LINE_MATCH_SYS_PROP;
import static com.michaelfitzmaurice.devtools.HeaderTool.INSERT_MODE_SYS_PROP;
import static com.michaelfitzmaurice.devtools.HeaderTool.WILDCARD_FILE_EXTENSION;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class HeaderToolAcceptanceTest extends HeaderToolTest {
    
    @Test
    public void doesNotInsertHeaderByDefault()
    throws IOException {
        
        File sourceDir = new File(TMP_ROOT_DIRECTORY, "root/subA");
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
        
        assertFalse( Boolean.getBoolean(INSERT_MODE_SYS_PROP) );
        HeaderTool.main( 
            runtimeArgs(sourceDir, new String[] {WILDCARD_FILE_EXTENSION}) );
        assertFilesLackHeader(headerlessFiles);
    }
    
    @Test
    public void insertsHeaderIntoAllHeaderlessFilesForWildcardFileExtensionWhenInsertModeSet() 
    throws IOException {
     
        File sourceDir = new File(TMP_ROOT_DIRECTORY, "root/subA");
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
        
        System.setProperty(INSERT_MODE_SYS_PROP, "true");
        try {
            HeaderTool.main(
                runtimeArgs(sourceDir, 
                            new String[] {WILDCARD_FILE_EXTENSION}) );
            assertFilesHaveHeader(files);
        } finally {
            System.clearProperty(INSERT_MODE_SYS_PROP);
        }
    }
    
    @Test
    public void doesNotModifyFilesThatContainHeaderWhenInsertModeSet()
    throws IOException {
        
        File sourceDir = new File(TMP_ROOT_DIRECTORY, "root/subA");
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
        
        System.setProperty(INSERT_MODE_SYS_PROP, "true");
        try {
            HeaderTool.main(
                runtimeArgs(sourceDir, 
                            new String[] {WILDCARD_FILE_EXTENSION}) );
            assertFilesHaveHeader(filesWithHeader);
        } finally {
            System.clearProperty(INSERT_MODE_SYS_PROP);
        }
        
        Map<String, FileSizeAndDate> newFileSizesAndDates = 
            fileSizesAndDates(filesWithHeader);
        assertFilesUnchanged(originalFileSizesAndDates, newFileSizesAndDates);
    }
    
    @Test
    public void insertsHeaderIntoOnlyRelevantHeaderlessFilesForSuppliedFileExtensionsWhenInsertModeSet() 
    throws IOException {
     
        File sourceDir = new File(TMP_ROOT_DIRECTORY, "root/subA");
        List<File> filesThatShouldChange  = 
            aFileList()
                .withFile(sourceDir, "subA1/NoHeader.java")
                .withFile(sourceDir, "subA1/DifferentHeader.java")
                .withFile(sourceDir, "subA2/NoHeader.java")
                .withFile(sourceDir, "subA2/DifferentHeader.java")
                .build();
        assertFilesLackHeader(filesThatShouldChange);
        
        System.setProperty(INSERT_MODE_SYS_PROP, "true");
        try {
            HeaderTool.main( runtimeArgs(sourceDir, new String[] {"java"}) );
            assertFilesHaveHeader(filesThatShouldChange);
        } finally {
            System.clearProperty(INSERT_MODE_SYS_PROP);
        }
        
        List<File> filesThatShouldNotChange  = 
                aFileList()
                    .withFile(sourceDir, "subA1/no-header.txt")
                    .withFile(sourceDir, "subA1/different-header.txt")
                    .withFile(sourceDir, "subA2/no-header.txt")
                    .withFile(sourceDir, "subA2/different-header.txt")
                    .build();
        assertFilesLackHeader(filesThatShouldNotChange);
    }
    
    @Test
    public void doesNotInsertHeaderIntoFilesWithDifferentHeaderIfFirstLineMatchesWhenInsertModeAndFirstLineModeBothSet()
    throws IOException {
        
        File sourceDir = new File(TMP_ROOT_DIRECTORY, "root/subA");
        List<File> filesThatShouldChange  = 
            aFileList()
                .withFile(sourceDir, "subA1/NoHeader.java")
                .withFile(sourceDir, "subA2/NoHeader.java")
                .build();
        assertFilesLackHeader(filesThatShouldChange);
        
        System.setProperty(INSERT_MODE_SYS_PROP, "true");
        System.setProperty(FIRST_LINE_MATCH_SYS_PROP, "true");
        try {
            HeaderTool.main( runtimeArgs(sourceDir, new String[] {"java"}) );
            assertFilesHaveHeader(filesThatShouldChange);
        } finally {
            System.clearProperty(INSERT_MODE_SYS_PROP);
            System.clearProperty(FIRST_LINE_MATCH_SYS_PROP);
        }
        
        List<File> filesThatShouldNotChange  = 
                aFileList()
                    .withFile(sourceDir, "subA1/DifferentHeader.java")
                    .withFile(sourceDir, "subA1/no-header.txt")
                    .withFile(sourceDir, "subA1/different-header.txt")
                    .withFile(sourceDir, "subA2/DifferentHeader.java")
                    .withFile(sourceDir, "subA2/no-header.txt")
                    .withFile(sourceDir, "subA2/different-header.txt")
                    .build();
        assertFilesLackHeader(filesThatShouldNotChange);
    }
    
    ///////////////////////////////////////////////////////
    // helper methods
    ///////////////////////////////////////////////////////
    
    private String[] runtimeArgs(File sourceDir, String[] fileExtensions) {
        
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
