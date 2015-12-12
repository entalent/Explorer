package cn.edu.bit.cs.explorer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import org.apache.ftpserver.usermanager.impl.BaseUser;

import java.io.IOException;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.util.FtpHelper;

/**
 * just act as a container of FTP Server...
 */
public class FtpService extends Service {

    Binder binder = new FtpServiceBinder();
    FtpHelper ftpHelper = null;

    public FtpService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(ftpHelper != null) {
            ftpHelper.stopServer();
        }
    }

    public class FtpServiceBinder extends Binder {
        public FtpService getService() {
            return FtpService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            ftpHelper = new FtpHelper(FtpService.this);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public FtpHelper getFtpHelper() {
        return ftpHelper;
    }
}
