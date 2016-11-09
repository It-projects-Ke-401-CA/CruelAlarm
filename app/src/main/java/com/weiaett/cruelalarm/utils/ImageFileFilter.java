package com.weiaett.cruelalarm.utils;

import java.io.File;
import java.io.FileFilter;

class ImageFileFilter implements FileFilter {

    private static final String[] imgFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};

    public boolean accept(File file)
    {
        for (String extension : imgFileExtensions)
        {
            if (file.getName().toLowerCase().endsWith(extension))
            {
                return true;
            }
        }
        return false;
    }
}

