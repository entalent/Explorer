package cn.edu.bit.cs.explorer.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.bit.cs.explorer.MainActivity;
import cn.edu.bit.cs.explorer.util.FileAsyncTask;

public class MainService extends Service {

    private static ExecutorService SINGLE_TASK_EXECUTOR;

    static {
        SINGLE_TASK_EXECUTOR = (ExecutorService) Executors.newSingleThreadExecutor();
    };

    MainServiceBinder binder = new MainServiceBinder();
    private Queue<FileAsyncTask> tasks = new LinkedList<>();
    boolean isExecuting = false;

    public MainService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    void beginExecute() {
        isExecuting = true;
        Toast.makeText(MainService.this, "start execute", Toast.LENGTH_SHORT).show();
        while(!tasks.isEmpty()){
            FileAsyncTask taskToExecute;
            synchronized (tasks) {
                taskToExecute = tasks.poll();
            }
            taskToExecute.executeOnExecutor(SINGLE_TASK_EXECUTOR);
        }
        Toast.makeText(MainService.this, "execute finish", Toast.LENGTH_SHORT).show();
        isExecuting = false;
    }

    public boolean addTask(FileAsyncTask task) {
        boolean state = false;
        synchronized (tasks) {
            state = tasks.add(task);
        }
        if(!state) {
            return false;
        }
        if(tasks.size() == 1) {
            beginExecute();
        }
        return state;
    }

    public class MainServiceBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    public boolean getIsExecuting() {
        return isExecuting;
    }


}
