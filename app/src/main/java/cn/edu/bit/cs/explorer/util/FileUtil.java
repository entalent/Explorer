package cn.edu.bit.cs.explorer.util;

import java.io.File;

/**
 * Created by entalent on 2015/11/20.
 */
public class FileUtil {
    static {
        System.loadLibrary("explorerNative");
    }
    public static boolean isChildDirectoty(File parentDirectory, File childDirectory) {
        return childDirectory.getAbsolutePath().startsWith(parentDirectory.getAbsolutePath());
    }

    public static boolean copyFile(File srcFile, File dstPath){
        if((srcFile.isDirectory()) || (!dstPath.isDirectory())) {
            throw new IllegalArgumentException("");
        }
        if(srcFile.getParentFile().equals(dstPath)){
            throw new IllegalArgumentException("source and destination directory should not be the same");
        }
        String srcFileName = srcFile.getName();
        String dstFileName = dstPath + File.separator + srcFileName;
        if(copyFile(srcFile.getAbsolutePath(), dstFileName) != 1){
            return false;
        }
        return true;
    }

    public static native int copyFile(String srcFile, String dstFile);
}
