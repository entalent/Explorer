package cn.edu.bit.cs.explorer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.util.tasks.FileAsyncTask;
import cn.edu.bit.cs.explorer.util.tasks.ZipTask;

public class MainService extends Service {

    public static final int ACTION_SHOW_PROGRESS = 0x0,
                        ACTION_HIDE_PROGRESS = 0x1,
                        ACTION_REFRESH_DIRECTORY = 0x2,
                        ACTION_GOTO_DIRECTORY = 0x3;

    private static ExecutorService SINGLE_TASK_EXECUTOR;

    static {
        SINGLE_TASK_EXECUTOR = Executors.newSingleThreadExecutor();

        SINGLE_TASK_EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                //call Looper.prepare() once and only once in this thread
                Looper.prepare();
            }
        });
    };

    MainServiceBinder binder = new MainServiceBinder();
    private Queue<FileAsyncTask> tasks = new LinkedList<>();
    boolean isExecuting = false;

    int tasksToExecute = 0;

    public MainService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    void beginExecute() {
        isExecuting = true;



        while(!tasks.isEmpty()){
            final FileAsyncTask taskToExecute;

            synchronized (tasks) {
                taskToExecute = tasks.poll();
            }

            taskToExecute.setServiceRef(this);
            taskToExecute.executeOnExecutor(SINGLE_TASK_EXECUTOR);
        }



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

    public void sendRefreshDirectory(File directoryToRefresh) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.action_main_service));
        intent.putExtra("action", ACTION_REFRESH_DIRECTORY);
        intent.putExtra("file", directoryToRefresh);
        sendBroadcast(intent);
    }

    public void sendGotoDirectory(File directoryToGo) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.action_main_service));
        intent.putExtra("action", ACTION_GOTO_DIRECTORY);
        intent.putExtra("file", directoryToGo);
        sendBroadcast(intent);
    }

    public void addTasksToExecute() {
        tasksToExecute++;
        if(tasksToExecute == 1) {
            Intent i1 = new Intent();
            i1.setAction(getString(R.string.action_main_service));
            i1.putExtra("action", ACTION_SHOW_PROGRESS);
            sendBroadcast(i1);
        }
    }

    public void subTasksToExecute() {
        tasksToExecute--;
        if(tasksToExecute == 0) {
            Intent i2 = new Intent();
            i2.setAction(getString(R.string.action_main_service));
            i2.putExtra("action", ACTION_HIDE_PROGRESS);
            sendBroadcast(i2);
        }
    }
}
