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
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.After;
import org.junit.Before;

/**
 * Base class defining functionality that is common to both
 * unit and acceptance tests for HeaderTool.
 * 
 * @author Michael Fitzmaurice
 */
public class HeaderToolTest {
    
    protected static final String NEWLINE = 
            System.getProperty("line.separator");
    protected static final String HEADER_FILENAME = 
            "apache2-licence-java-header.txt";
    
    protected static File TMP_ROOT_DIRECTORY;
    protected static File HEADER_FILE;
    protected static String HEADER_CONTENT = "";

    @Before
    public void setUpDirectories() throws IOException {
        
        // ensure test data exists as expected
        File runtimeDirectory = new File( System.getProperty("user.dir") );
        File testDataRoot = new File(runtimeDirectory, "src/test/data/");
        assertTestDataDirValid(testDataRoot);
        
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
    
        HEADER_FILE = new File(testDataRoot, HEADER_FILENAME);
        HEADER_CONTENT = fileContents(HEADER_FILE);
        assertTrue("Header file was empty", HEADER_CONTENT.length() > 0);
    }
    
    @After
    public void tearDownDirectories() throws IOException {
        if ( TMP_ROOT_DIRECTORY.exists() ) {
            FileUtils.deleteDirectory(TMP_ROOT_DIRECTORY);
        }
    }
    
    ///////////////////////////////////////////////////////
    // helper methods
    ///////////////////////////////////////////////////////
    protected void assertTestDataDirAvailable(File testDataRoot) {
        
        String failMsg = 
            format("Test data directory at %s is not a readable directory", 
                    testDataRoot);
        assertTrue( failMsg, testDataRoot.exists() );
        assertTrue( failMsg, testDataRoot.isDirectory() );
        assertTrue( failMsg, testDataRoot.canRead() );
    }
    
    protected void assertTestDataDirValid(File testDataDir) {
        
        assertTestDataDirAvailable(testDataDir);
        Collection<File> testDirContents = 
            FileUtils.listFilesAndDirs(testDataDir, 
                                    TrueFileFilter.INSTANCE, 
                                    TrueFileFilter.INSTANCE);
        
        assertFileListsEqual(expectedTestDirContents(testDataDir), 
                            testDirContents,
                            "Did not report expected list of files");
    }
    
    protected List<File> expectedTestDirContents(File testDataDir) {
        
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
    
    protected void assertFileListsEqual(Collection<File> first, 
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
    
    protected void assertDirectoryEmpty(File dir) {
        
        String failMsg = dir + " is not empty";
        assertEquals(failMsg, dir.list().length, 0);
    }

    protected String fileContents(File file) 
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
    
    protected void assertFilesLackHeader(Collection<File> files) 
    throws IOException {
        
        for (File file : files) {
            String failMsg = 
                format("%s should not contain header from %s, but does", 
                        file, 
                        HEADER_FILE);
            assertFalse( failMsg, fileContents(file).startsWith(HEADER_CONTENT) );
        }
    }
    
    protected void assertFilesHaveHeader(Collection<File> files) 
    throws IOException {
        
        for (File file : files) {
            String failMsg = 
                format("%s should contain header from %s, but does not", 
                        file, 
                        HEADER_FILE);
            assertTrue( failMsg, fileContents(file).startsWith(HEADER_CONTENT) );
        }
    }

}
