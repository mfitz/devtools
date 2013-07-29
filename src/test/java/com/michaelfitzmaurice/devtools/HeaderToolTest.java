package com.michaelfitzmaurice.devtools;

import static com.michaelfitzmaurice.devtools.FileListBuilder.aFileList;
import static com.michaelfitzmaurice.devtools.HeaderTool.MatchMode.FIRST_LINE_ONLY;
import static com.michaelfitzmaurice.devtools.HeaderTool.MatchMode.FULL_MATCH;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.michaelfitzmaurice.devtools.HeaderTool.MatchMode;

public class HeaderToolTest {
    
    private static final String NEWLINE = 
            System.getProperty("line.separator");
    private static final String HEADER_FILENAME = 
            "apache2-licence-java-header.txt";
    
    private static File TMP_ROOT_DIRECTORY;
    private static File HEADER_FILE;
    private static String HEADER_CONTENT = "";

    @Before
    public void setUpDirectories() throws IOException {
        
        // check test data exists as expected
        File runtimeDirectory = new File( System.getProperty("user.dir") );
        File testDataRoot = new File(runtimeDirectory, "src/test/data/");
        assertTestDataDirValid(testDataRoot);
        
        HEADER_FILE = new File(testDataRoot, HEADER_FILENAME);
        HEADER_CONTENT = readFileContent(HEADER_FILE);
        assertTrue("Header file was empty", HEADER_CONTENT.length() > 0);
        
        // copy test data to a temp directory
        TMP_ROOT_DIRECTORY = 
            new File( FileUtils.getTempDirectory(), 
                        "header_tool_tests_" + System.currentTimeMillis() );
        FileUtils.forceMkdir(TMP_ROOT_DIRECTORY);
        TMP_ROOT_DIRECTORY.deleteOnExit();
        FileUtils.copyDirectory(testDataRoot, TMP_ROOT_DIRECTORY);
        Collection<File> tmpDirContents = 
            FileUtils.listFilesAndDirs(TMP_ROOT_DIRECTORY, 
                                        TrueFileFilter.INSTANCE, 
                                        TrueFileFilter.INSTANCE);
        assertFileListsEqual(expectedTestDirContents(TMP_ROOT_DIRECTORY), 
                            tmpDirContents,
                            "Temp dir does not contain expected contents");
    }
    
    @After
    public void tearDownDirectories() throws IOException {
        if ( TMP_ROOT_DIRECTORY.exists() ) {
            FileUtils.deleteDirectory(TMP_ROOT_DIRECTORY);
        }
    }
    
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
        List<File> filesWithoutHeader  = 
            aFileList()
                .withFile(targetDir, "NoHeader.java")
                .withFile(targetDir, "no-header.txt")
                .withFile(targetDir, "DifferentHeader.java")
                .withFile(targetDir, "different-header.txt")
                .build();
        String failMsg = "";
        for (File file : filesWithoutHeader) {
            failMsg = 
                format("%s should not contain header from %s, but does", 
                        file, 
                        HEADER_FILE);
            assertFalse( failMsg, fileContents(file).startsWith(HEADER_CONTENT) );
        }
        
        HeaderTool headerTool = new HeaderTool(HEADER_FILE, FULL_MATCH);
        headerTool.insertHeader(filesWithoutHeader);
        
        for (File file : filesWithoutHeader) {
            failMsg = 
                format("%s should contain header from %s, but does not", 
                        file, 
                        HEADER_FILE);
            assertTrue( failMsg, fileContents(file).startsWith(HEADER_CONTENT) );
        }
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
    
    ///////////////////////////////////////////////////////
    // helper methods
    ///////////////////////////////////////////////////////
    private static void assertTestDataDirAvailable(File testDataRoot) {
        
        String failMsg = 
            format("Test data directory at %s is not a readable directory", 
                    testDataRoot);
        assertTrue( failMsg, testDataRoot.exists() );
        assertTrue( failMsg, testDataRoot.isDirectory() );
        assertTrue( failMsg, testDataRoot.canRead() );
    }
    
    private static void assertTestDataDirValid(File testDataDir) {
        
        assertTestDataDirAvailable(testDataDir);
        Collection<File> testDirContents = 
            FileUtils.listFilesAndDirs(testDataDir, 
                                    TrueFileFilter.INSTANCE, 
                                    TrueFileFilter.INSTANCE);
        
        assertFileListsEqual(expectedTestDirContents(testDataDir), 
                            testDirContents,
                            "Did not report expected list of files");
    }
    
    private static List<File> expectedTestDirContents(File testDataDir) {
        
        return aFileList()
                .withFile(testDataDir, "")
                .withFile(testDataDir, HEADER_FILENAME)
                .withFile(testDataDir, "root")
                .withFile(testDataDir, "root/subA")
                .withFile(testDataDir, "root/subA/subA1")
                .withFile(testDataDir, "root/subA/subA1/different-header.txt")
                .withFile(testDataDir, "root/subA/subA1/header.txt")
                .withFile(testDataDir, "root/subA/subA1/no-header.txt")
                .withFile(testDataDir, "root/subA/subA1/NoHeader.java")
                .withFile(testDataDir, "root/subA/subA1/Header.java")
                .withFile(testDataDir, "root/subA/subA1/DifferentHeader.java")
                .withFile(testDataDir, "root/subA/subA2")
                .withFile(testDataDir, "root/subA/subA2/different-header.txt")
                .withFile(testDataDir, "root/subA/subA2/header.txt")
                .withFile(testDataDir, "root/subA/subA2/no-header.txt")
                .withFile(testDataDir, "root/subA/subA2/NoHeader.java")
                .withFile(testDataDir, "root/subA/subA2/Header.java")
                .withFile(testDataDir, "root/subA/subA2/DifferentHeader.java")
                .withFile(testDataDir, "root/subB")
                .withFile(testDataDir, "root/subB/header.txt")
                .withFile(testDataDir, "root/subB/no-header.txt")
                .withFile(testDataDir, "root/subB/NoHeader.java")
                .withFile(testDataDir, "root/subB/Header.java")
                .withFile(testDataDir, "root/subC")
                .withFile(testDataDir, "root/subC/subC1")
                .withFile(testDataDir, "root/subC/subC1/header.txt")
                .withFile(testDataDir, "root/subC/subC1/no-header.txt")
                .withFile(testDataDir, "root/subC/subC1/NoHeader.java")
                .withFile(testDataDir, "root/subC/subC1/Header.java")
                .withFile(testDataDir, "root/emptySub")
                .build();
    }
    
    private static void assertFileListsEqual(Collection<File> first, 
                                        Collection<File> second,
                                        String assertFailMsg) {
        
        String failMsg = "File list sizes differ";
        if (assertFailMsg != null) {
            failMsg = assertFailMsg;
        }
        assertEquals( failMsg, first.size(), second.size() );
        
        for (File file : second) {
            if (assertFailMsg == null) {
                failMsg = "First list does not contain expected file " + file;
            }
            assertTrue( failMsg, first.contains(file) );
        }
    }

    private static String readFileContent(File headerFile)
    throws FileNotFoundException, IOException {
        
        String failMsg = 
            format("No readble header file at %s", headerFile);
        assertTrue( failMsg, headerFile.exists() );
        assertTrue( failMsg, headerFile.canRead() );
        
        StringBuffer fileContent = new StringBuffer();
        BufferedReader br = new BufferedReader( new FileReader(headerFile) );
        String nextLine = br.readLine();
        while (nextLine != null) {
            fileContent.append(nextLine);
            fileContent.append(NEWLINE);
            nextLine = br.readLine();
        }
        br.close();
        
        return fileContent.toString();
    }
    
    private static void assertDirectoryEmpty(File dir) {
        
        String failMsg = dir + " is not empty";
        assertEquals(failMsg, dir.list().length, 0);
    }

    private static String fileContents(File file) 
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
}
