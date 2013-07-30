package com.michaelfitzmaurice.devtools;

import static com.michaelfitzmaurice.devtools.FileListBuilder.aFileList;
import static com.michaelfitzmaurice.devtools.HeaderTool.MatchMode.FIRST_LINE_ONLY;
import static com.michaelfitzmaurice.devtools.HeaderTool.MatchMode.FULL_MATCH;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.michaelfitzmaurice.devtools.HeaderTool.MatchMode;

public class HeaderToolUnitTest extends HeaderToolTest {
    
    @Test
    public void reportsEmptyFileListWhenScanningAnEmptyDirectoryInFullMatchMode() 
    throws Exception {
        
        assertReportsEmptyFileListWhenScanningAnEmptyDirectory(
                                                        MatchMode.FULL_MATCH);
    }
    
    @Test
    public void reportsEmptyFileListWhenScanningAnEmptyDirectoryInFirstLineMatchMode() 
    throws Exception {
        
        assertReportsEmptyFileListWhenScanningAnEmptyDirectory(
                                        MatchMode.FIRST_LINE_ONLY);
    }
    
    private void assertReportsEmptyFileListWhenScanningAnEmptyDirectory(
                                                            MatchMode mode) 
    throws Exception {
        
        File emptyDir = new File(TMP_ROOT_DIRECTORY, "root/emptySub");
        assertDirectoryEmpty(emptyDir);
        
        HeaderTool headerTool = new HeaderTool(HEADER_FILE, mode);
        Collection<File> missingHeaders = 
            headerTool.listFilesWithoutHeader(emptyDir, null);
        String failMsg =
            "Empty directory should return empty list of files missing headers";
        assertTrue(failMsg, missingHeaders.isEmpty() ); 
    }
    
    @Test
    public void reportsAllFilesLackingHeadersForWildcardExtensionsInFullMatchMode() 
    throws Exception {
        assertReportsAllFilesLackingHeadersForWildcardExtensions(FULL_MATCH);
    }
    
    @Test
    public void reportsAllFilesLackingHeadersForWildcardExtensionsInFirstLineMatchMode() 
    throws Exception {
        assertReportsAllFilesLackingHeadersForWildcardExtensions(
                                                            FIRST_LINE_ONLY);
    }
    
    private void assertReportsAllFilesLackingHeadersForWildcardExtensions(
                                                            MatchMode mode) 
    throws Exception {
        
        File targetDir = new File(TMP_ROOT_DIRECTORY, "root/subA/subA1");
        FileListBuilder filelistBuilder = 
                aFileList()
                .withFile(targetDir, "NoHeader.java")
                .withFile(targetDir, "no-header.txt");
        if (mode == MatchMode.FULL_MATCH) {
            filelistBuilder
                .withFile(targetDir, "DifferentHeader.java")
                .withFile(targetDir, "different-header.txt");
        }
        List<File> filesWithoutHeader = filelistBuilder.build();
        
        HeaderTool headerTool = new HeaderTool(HEADER_FILE, mode);
        assertFileListsEqual(filesWithoutHeader, 
                            headerTool.listFilesWithoutHeader(targetDir, 
                                                    null),
                            "Did not report expected list of files");
    }
    
    @Test
    public void recursivelyReportsAllFilesLackingHeadersForWildcardExtensionsInFullMatchMode() 
    throws Exception {
        assertRecursivelyReportsAllFilesLackingHeadersForWildcardExtensions(FULL_MATCH);
    }
    
    @Test
    public void recursivelyReportsAllFilesLackingHeadersForWildcardExtensionsInFirstLineMatchMode() 
    throws Exception {
        assertRecursivelyReportsAllFilesLackingHeadersForWildcardExtensions(
                                                            FIRST_LINE_ONLY);
    }
    
    private void assertRecursivelyReportsAllFilesLackingHeadersForWildcardExtensions(
                                                            MatchMode mode) 
    throws Exception {
        
        File targetDir = new File(TMP_ROOT_DIRECTORY, "root/subA");
        FileListBuilder filelistBuilder = 
                aFileList()
                    .withFile(targetDir, "subA1/NoHeader.java")
                    .withFile(targetDir, "subA1/no-header.txt")
                    .withFile(targetDir, "subA2/NoHeader.java")
                    .withFile(targetDir, "subA2/no-header.txt");
        if (mode == MatchMode.FULL_MATCH) {
            filelistBuilder
                .withFile(targetDir, "subA1/DifferentHeader.java")
                .withFile(targetDir, "subA1/different-header.txt")
                .withFile(targetDir, "subA2/DifferentHeader.java")
                .withFile(targetDir, "subA2/different-header.txt");
        }
        List<File> filesWithoutHeader = filelistBuilder.build();
        
        HeaderTool headerTool = new HeaderTool(HEADER_FILE, mode);
        assertFileListsEqual(filesWithoutHeader, 
                            headerTool.listFilesWithoutHeader(targetDir, 
                                                                null),
                            "Did not report expected list of files");
    }
    
    
    @Test
    public void recursivelyReportsFilesLackingHeadersMatchingFileExtensionInFullMatchMode() 
    throws Exception {
        assertRecursivelyReportsFilesLackingHeadersMatchingFileExtension(FULL_MATCH);
    }
    
    @Test
    public void recursivelyReportsFilesLackingHeadersMatchingFileExtensionInFirstLineMatchMode() 
    throws Exception {
        assertRecursivelyReportsFilesLackingHeadersMatchingFileExtension(
                                                            FIRST_LINE_ONLY);
    }
    
    private void assertRecursivelyReportsFilesLackingHeadersMatchingFileExtension(
                                                            MatchMode mode) 
    throws Exception {
        
        File targetDir = new File(TMP_ROOT_DIRECTORY, "root/subA");
        FileListBuilder filelistBuilder = 
            aFileList()
                .withFile(targetDir, "subA1/NoHeader.java")
                .withFile(targetDir, "subA2/NoHeader.java");
        if (mode == MatchMode.FULL_MATCH) {
            filelistBuilder
            .withFile(targetDir, "subA1/DifferentHeader.java")
            .withFile(targetDir, "subA2/DifferentHeader.java");
        }
        List<File> filesWithoutHeader = filelistBuilder.build();
        
        HeaderTool headerTool = new HeaderTool(HEADER_FILE, mode);
        assertFileListsEqual(filesWithoutHeader, 
                            headerTool.listFilesWithoutHeader(targetDir, 
                                                    new String[] {"java"}),
                            "Did not report expected list of files");
    }
    
    @Test
    public void insertsHeaderIntoSuppliedFiles()
    throws Exception {
        
        File targetDir = new File(TMP_ROOT_DIRECTORY, "root/subA/subA1");
        List<File> files  = 
            aFileList()
                .withFile(targetDir, "NoHeader.java")
                .withFile(targetDir, "no-header.txt")
                .withFile(targetDir, "DifferentHeader.java")
                .withFile(targetDir, "different-header.txt")
                .build();
        assertFilesLackHeader(files);
        
        HeaderTool headerTool = new HeaderTool(HEADER_FILE, FULL_MATCH);
        headerTool.insertHeader(files);
        
        assertFilesHaveHeader(files);
    }

    @Test (expected = IOException.class)
    public void propagatesExceptionInsertingHeader() 
    throws Exception {
        
        File targetDir = new File(TMP_ROOT_DIRECTORY, "root/subA/subA1");
        String filename = "NoHeader.java";
        File brokenFile = new File(targetDir, filename);
        assertTrue("Failed to make file unreadable", 
                    brokenFile.setReadable(false) );
        List<File> files = aFileList().withFile(targetDir, filename).build();
        
        HeaderTool headerTool = new HeaderTool(HEADER_FILE, FULL_MATCH);
        try {
            headerTool.insertHeader(files);
        } catch (IOException e) {
            String expectedMsg = 
                format( "%s (Permission denied)", 
                        brokenFile.getAbsolutePath() );
            String failMsg =
                format("Exception thrown did not have expected msg '%s'", 
                        expectedMsg);
            assertEquals( failMsg, expectedMsg, e.getMessage() );
            throw e;
        } finally {
            // set file back to readable to allow it to be deleted
            brokenFile.setReadable(true);
        }
    }
}
