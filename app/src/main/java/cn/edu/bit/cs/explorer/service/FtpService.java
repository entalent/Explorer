package cn.edu.bit.cs.explorer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.usermanager.impl.BaseUser;

import java.util.ArrayList;

import cn.edu.bit.cs.explorer.util.FtpHelper;

/**
 * just act as a container of FTP Server...
 */
public class FtpService extends Service {

    Binder binder = new FtpServiceBinder();
    FtpHelper ftpHelper = new FtpHelper();

    public FtpService() {
        ArrayList<BaseUser> users = new ArrayList<>();
        BaseUser user = new BaseUser();
        user.setName("anonymous");
        user.setHomeDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
        users.add(user);
        ftpHelper.addUsers(users);
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

    public FtpHelper getFtpHelper() {
        return ftpHelper;
    }
}
