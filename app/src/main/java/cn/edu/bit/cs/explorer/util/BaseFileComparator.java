package cn.edu.bit.cs.explorer.util;

import java.io.File;
import java.util.Comparator;

/**
 * Created by entalent on 2015/12/5.
 */
public class BaseFileComparator implements Comparator<File> {
    protected boolean reversed = false;

    @Override
    public int compare(File lhs, File rhs) {
        if(lhs.isDirectory() && !rhs.isDirectory()) {
            return -1;
        } else if(!lhs.isDirectory() && rhs.isDirectory()) {
            return 1;
        }
        return 0;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public boolean isReversed() {
        return this.reversed;
    }
}
