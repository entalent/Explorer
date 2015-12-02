package cn.edu.bit.cs.explorer.util.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by entalent on 2015/11/29.
 */
public abstract class FileAsyncTask extends AsyncTask<String, Integer, Integer> {
    protected Context context;

    protected File operationDirectory;

    protected ArrayList<File> filesToOperate;

    protected int completedFileCnt = 0;

    OnPostExecuteListener onPostExecuteListener;

    public FileAsyncTask(Context context, File operationDirectory, ArrayList<File> filesToOperate){
        this.context = context;
        this.operationDirectory = operationDirectory;
        this.filesToOperate = filesToOperate;
    }

    public File getOperationDirectory() {
        return operationDirectory;
    }

    public void setOnPostExecuteListener(OnPostExecuteListener listener){
        this.onPostExecuteListener = listener;
    }

    public interface OnPostExecuteListener {
        void onPostExecute();
    }
}