package cn.edu.bit.cs.explorer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cn.edu.bit.cs.explorer.service.FtpService;
import cn.edu.bit.cs.explorer.network.FtpHelper;
import cn.edu.bit.cs.explorer.network.NetworkUtil;

public class FTPServerActivity extends BaseActivity implements FtpHelper.FtpServerListener {

    Button buttonLaunch;
    TextView textView;

    FtpService ftpService;
    FtpHelper ftpHelper;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ftpService = ((FtpService.FtpServiceBinder)service).getService();
            ftpHelper = ftpService.getFtpHelper();
            if(ftpHelper == null) {
                Toast.makeText(FTPServerActivity.this, getString(R.string.message_ftp_fail), Toast.LENGTH_SHORT).show();
                finish();
            }
            ftpHelper.addListener(FTPServerActivity.this);
            refreshState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ftpService = null;
            ftpHelper = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_ftpserver);
        buttonLaunch = (Button) findViewById(R.id.btn_launch);
        textView = (TextView)findViewById(R.id.textView);

        setTitle(getString(R.string.title_activity_ftp_server));

        Intent intent = new Intent(FTPServerActivity.this, FtpService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);



        buttonLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ftpHelper.isServerRunning())
                    ftpHelper.stopServer();
                else
                    ftpHelper.startServer();
            }
        });
    }

    @Override
    public void onServerStartFinish(boolean running) {
        if(running)
            textView.setText(String.format(getString(R.string.message_ftp_started), NetworkUtil.getIPAddress(FTPServerActivity.this)));
        else
            textView.setText(getString(R.string.message_ftp_fail));
        refreshState();
    }

    @Override
    public void onServerStop() {
        textView.setText(getString(R.string.message_ftp_stopped));
        refreshState();
    }

    public void refreshState() {
        if(NetworkUtil.isWifiConnected(FTPServerActivity.this)) {
            buttonLaunch.setClickable(true);
            if (ftpHelper.isServerRunning()) {
                buttonLaunch.setText(getString(R.string.message_stop_ftp));
            } else {
                buttonLaunch.setText(getString(R.string.message_start_ftp));
            }
        } else {
            buttonLaunch.setClickable(false);
            buttonLaunch.setText(getString(R.string.message_wlan_not_connected));
        }
    }
}
