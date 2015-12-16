package cn.edu.bit.cs.explorer.util;


import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by entalent on 2015/12/2.
 */
public class ZipUtil {

    public static int zip(ArrayList<File> files, File targetFile) throws IOException {
        int zipped = 0;
        if(!targetFile.exists()){
            targetFile.createNewFile();
        }
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFile));

        for(File i : files) {
            zip("", i, out);
            zipped++;
        }

        out.close();

        return zipped;
    }

    private static void zip(String entry, File file, ZipOutputStream out) throws IOException {
        if(!(file.canRead() && file.exists())) {
            throw new IOException();
        }
        if(file.isDirectory()) {
            String nextEntry = entry + file.getName() + File.separator;

            ZipEntry e = new ZipEntry(nextEntry);
            out.putNextEntry(new ZipEntry(nextEntry));
            File[] files = file.listFiles();
            if(files == null){
                throw new IOException();
            }
            for(File i : files) {
                zip(nextEntry, i, out);
            }
        } else {
            if(!(file.exists() && file.canRead())){
                throw new IOException("input file does not exist or cannot read");
            }

            out.putNextEntry(new ZipEntry(entry + file.getName()));
            InputStream input = new FileInputStream(file);
            int tmp;
            while(-1 != (tmp = input.read())){
                out.write(tmp);
            }
            input.close();
        }
    }

    public static void unZip(File inputFile, File targetDirectory)
            throws IOException, IllegalArgumentException{
        if(targetDirectory.exists() && (!targetDirectory.isDirectory())) {
            throw new IllegalArgumentException("target directory should be a directory");
        }

        ZipFile zipFile = new ZipFile(inputFile);
        Enumeration<ZipEntry> entries = zipFile.getEntries();
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        while(entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            System.out.println(zipEntry.getName() + " " + zipEntry.isDirectory());
            if(zipEntry.isDirectory()) {
                File dir = new File(targetDirectory.getAbsolutePath() + File.separator + zipEntry.getName());
                dir.mkdirs();
            } else {
                File file = new File(targetDirectory.getAbsolutePath() + File.separator + zipEntry.getName());
                File parentDir = file.getParentFile();
                file.createNewFile();
                InputStream input = zipFile.getInputStream(zipEntry);
                OutputStream output = new FileOutputStream(file);
                int temp;
                while(-1 != (temp = input.read())){
                    output.write(temp);
                }
                input.close();
                output.close();
            }
        }
    }
}