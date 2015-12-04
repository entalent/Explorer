package cn.edu.bit.cs.explorer.util;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by entalent on 2015/11/11.
 */
public class TextUtil {

    static String[] UNIT_STR = {
            "B", "KB", "MB", "GB", "TB"
    };

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    public static String formatSizeStr(long sizeLong){
        double size = sizeLong;
        int unit = 0;
        while(unit < UNIT_STR.length && size >= 1024){
            size /= 1024.0;
            unit++;
        }
        return String.format("%.2f %s", size, UNIT_STR[unit]);
    }

    public static String formatTimeStr(long time){
        Date date = new Date();
        date.setTime(time);
        return simpleDateFormat.format(date);
    }

    public static String getMimeTypeFromFile(File file){
        return getMimeType(Uri.fromFile(file).toString());
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getIPStringFromInt(int ipAddress) {
        return (ipAddress & 0xFF ) + "." +
                ((ipAddress >> 8 ) & 0xFF) + "." +
                ((ipAddress >> 16 ) & 0xFF) + "." +
                ( ipAddress >> 24 & 0xFF) ;
    }
}
