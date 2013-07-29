package com.michaelfitzmaurice.devtools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListBuilder {

    private final List<File> fileList = new ArrayList<File>();

    public static FileListBuilder aFileList() {
        return new FileListBuilder();
    }

    public FileListBuilder withFile(File parent, String path) {
        fileList.add( new File(parent, path) );
        return this;
    }

    public List<File> build() {
        return fileList;
    }
}
