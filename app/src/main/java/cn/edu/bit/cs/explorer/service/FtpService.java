package cn.edu.bit.cs.explorer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

import cn.edu.bit.cs.explorer.network.AdbUtil;
import cn.edu.bit.cs.explorer.network.FtpHelper;
import cn.edu.bit.cs.explorer.network.NetworkUtil;

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
        Toast.makeText(FtpService.this, "ftp service started", Toast.LENGTH_SHORT).show();
    }

    public FtpHelper getFtpHelper() {
        return ftpHelper;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!NetworkUtil.isWifiConnected(FtpService.this)){
                Toast.makeText(FtpService.this, "wifi not connected", Toast.LENGTH_SHORT).show();
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
