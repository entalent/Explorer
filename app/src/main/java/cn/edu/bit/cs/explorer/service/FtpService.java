package cn.edu.bit.cs.explorer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import org.apache.ftpserver.usermanager.impl.BaseUser;

import java.io.IOException;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.util.AdbUtil;
import cn.edu.bit.cs.explorer.util.FtpHelper;
import cn.edu.bit.cs.explorer.util.NetworkUtil;

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
        unregisterReceiver(broadcastReceiver);
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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public FtpHelper getFtpHelper() {
        return ftpHelper;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!NetworkUtil.isWifiConnected(FtpService.this)){
                try {
                    AdbUtil.startAdbd(-1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ftpHelper.stopServer();
            }
        }
    };

}
