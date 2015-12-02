package cn.edu.bit.cs.explorer.util.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.service.MainService;

/**
 * Created by entalent on 2015/11/29.
 */
public abstract class FileAsyncTask extends AsyncTask<String, Integer, Integer> {
    protected Context context;

    protected File operationDirectory;

    protected ArrayList<File> filesToOperate = new ArrayList<>();

    protected int completedFileCnt = 0;

    protected MainService service;

    public FileAsyncTask(Context context, File operationDirectory, ArrayList<File> filesToOperate){
        this.context = context;
        this.operationDirectory = operationDirectory;
        this.filesToOperate.addAll(filesToOperate);
    }

    public File getOperationDirectory() {
        return operationDirectory;
    }

    public void setServiceRef(MainService service) {
        this.service = service;
    }
}