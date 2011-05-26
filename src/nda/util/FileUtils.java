package nda.util;

import java.io.File;

/**
 * @author giulianoxt
 *
 */
public final class FileUtils {
    public static String parseFileName(String filepath) {
        int start = filepath.lastIndexOf('.');
        int end = filepath.lastIndexOf(File.separatorChar);
        
        if (start < 0) start = filepath.length();

        String newName = filepath.substring(end+1, start);
        return newName;
    }
}
