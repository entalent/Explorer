package cn.edu.bit.cs.explorer.util;

import java.io.File;

/**
 * Created by entalent on 2015/11/20.
 */
public class FileUtil {
    public static boolean isChildDirectoty(File parentDirectory, File childDirectory) {
        return childDirectory.getAbsolutePath().startsWith(parentDirectory.getAbsolutePath());
    }
}
