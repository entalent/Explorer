package cn.edu.bit.cs.explorer.util;

import java.io.File;
import java.util.Comparator;

/**
 * Created by entalent on 2015/12/5.
 */
public class FileComparators {

    public static final class NameComparator extends BaseFileComparator {
        @Override
        public int compare(File lhs, File rhs) {
            int flag = super.compare(lhs, rhs);
            if(flag == 0) {
                flag = lhs.getName().compareTo(rhs.getName());
                flag = reversed ? flag : -flag;
            }
            return flag;
        }
    }

    public static final class SizeComparator extends BaseFileComparator {
        @Override
        public int compare(File lhs, File rhs) {
            int flag = super.compare(lhs, rhs);
            if(flag == 0) {
                long len1 = lhs.length(), len2 = rhs.length();
                if(len1 == len2) {
                    flag = lhs.getName().compareTo(rhs.getName());
                } else {
                    flag = len1 < len2 ? -1 : 1;
                }
                flag = reversed ? flag : -flag;
            }
            return flag;
        }
    }

    public static final class ModifyDateComparator extends BaseFileComparator {
        @Override
        public int compare(File lhs, File rhs) {
            int flag = super.compare(lhs, rhs);
            if(flag == 0) {
                long t1 = lhs.lastModified(), t2 = rhs.lastModified();
                if(t1 == t2) {
                    flag = lhs.getName().compareTo(rhs.getName());
                } else {
                    flag = t1 < t2 ? -1 : 1;
                }
                flag = reversed ? flag : -flag;
            }
            return flag;
        }
    }
}



