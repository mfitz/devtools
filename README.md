Devtools
===========

Tools to help with various aspects of development, for example automatically inserting headers (such as licences) into source files.

Building
===========

Uses Apache Maven (http://maven.apache.org/).
To compile and build a distro in zip format:

    mvn clean assembly:assembly 

To run unit tests and generate a test coverage report:

    mvn clean cobertura:cobertura

To generate a local project website include all reports:

    mvn clean site:site
    
Header Tool
===========

An app that reports on text files in a given directory that do not contain a specified header. Lists files recursively, with optional filtering based on file extension. Can also be run in insert mode, whereby it will insert the header at the beginning of any file that lacks it. 

The tool also includes the option to match only against the first line of the header, which I included because the licence headers I use in personal open source projects typically include the year in the copyright statement. When a header is present in a file, but, for example, with a copyright statement using a different year from that of the specified header, I don't want to insert the header on top of the existing one. Essentially I just want to automatically insert the licence header into any file that does not currently have a licence header. The first line matching option does that for me; I am assuming that a source file that begins with the same line as that of the header will only do so because it starts with a header, even if that header differs slightly from the one I am matching against.

Running
-----------

Having built a distro and then unpacked the subsequent zip file, from the root of the unpacked distro, on *nix, type:

    java -classpath lib:lib/* com.michaelfitzmaurice.devtools.HeaderTool <root of source directory> <location of header file> <file extensions>
    
Note that on Windows, classpath elements are separated with a semi-colon, rather than a colon. The locations of both the source directory and the header file should be full paths, and the list of file extensions is space separated and should not include the dot character, i.e. `java cpp`, rather than `.java .cpp` (don't surround the list with quote marks). The list of file extensions must include at least one value, but you can use the * character here to remove file extension filtering completely and match against any file.

By default, HeaderTool attempts to match against the full header, rather than only the first line of the header, and only **reports** on files lacking that header (as opposed to inserting the header). You can override both these defaults using Java system properties:

    -Dinsert-mode=true
    -Dfirst-line-match=true
    
There is a `header-tool.sh` shell script provided for convenience; you will need to pass this script the same runtime arguments described above.
    

