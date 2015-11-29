package cn.edu.bit.cs.explorer.util;

import android.os.AsyncTask;

import java.io.File;

/**
 * Created by entalent on 2015/11/29.
 */
public abstract class FileAsyncTask extends AsyncTask<String, Integer, Integer> {
    protected String taskName;
    protected File currentDirectory;
}