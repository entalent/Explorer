package cn.edu.bit.cs.explorer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
/**
 * Created by entalent on 2015/12/2.
 */
public class ZipUtil {

    public static int zip(ArrayList<File> files, File targetFile) throws IOException {
        int entryCnt = 0;
        File parentFile = targetFile.getParentFile();
        if(!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if(!targetFile.exists()){
            targetFile.createNewFile();
        }
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFile));

        for(File i : files) {
            entryCnt += zip("", i, out);
        }

        out.close();
        return entryCnt;
    }

    private static int zip(String entry, File file, ZipOutputStream out) throws IOException {
        int entryCnt = 0;
        if(!(file.canRead() && file.exists())) {
            throw new IOException();
        }
        if(file.isDirectory()) {
            String nextEntry = entry + file.getName() + File.separator;

            entryCnt++;
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
            entryCnt++;
            out.putNextEntry(new ZipEntry(entry + file.getName()));
            InputStream input = new FileInputStream(file);
            int tmp;
            while(-1 != (tmp = input.read())){
                out.write(tmp);
            }
            input.close();
        }
        return entryCnt;
    }

    public static int unZip(File inputFile, File targetDirectory)
            throws IOException, IllegalArgumentException{

        int entryCnt = 0;
        if(targetDirectory.exists() && (!targetDirectory.isDirectory())) {
            throw new IllegalArgumentException("target directory should be a directory");
        }

        ZipFile zipFile = new ZipFile(inputFile);
        ZipInputStream zipInput = new ZipInputStream(new FileInputStream(inputFile));
        ZipEntry entry = null;
        while(null != (entry = zipInput.getNextEntry())){
            entryCnt++;
            File outFile = new File(targetDirectory.getAbsolutePath() + File.separator + entry.getName());
            InputStream input = null;
            OutputStream output = null;
            try {
                if (entry.isDirectory()) {
                    outFile.mkdir();
                } else {
                    File parentFile = outFile.getParentFile();

                    if (parentFile.exists() && !parentFile.isDirectory()) {
                        parentFile.delete();
                    }
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    outFile.createNewFile();
                    input = zipFile.getInputStream(entry);
                    output = new FileOutputStream(outFile);
                    int temp;
                    while (-1 != (temp = input.read())) {
                        output.write(temp);
                    }
                    input.close();
                    output.close();
                }
            } catch (Exception e) {
                return entryCnt;
            } finally {
                if(input != null) input.close();
                if(output != null) output.close();
                zipFile.close();
                zipInput.close();

            }
        }
        zipFile.close();
        zipInput.close();
        return entryCnt;
    }
}