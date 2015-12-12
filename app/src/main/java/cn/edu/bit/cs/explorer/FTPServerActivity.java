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
import cn.edu.bit.cs.explorer.util.FtpHelper;
import cn.edu.bit.cs.explorer.util.NetworkUtil;

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
                Toast.makeText(FTPServerActivity.this, "failed to create FTP server", Toast.LENGTH_SHORT).show();
                finish();
            }
            ftpHelper.addListener(FTPServerActivity.this);
            Toast.makeText(FTPServerActivity.this, "service connected", Toast.LENGTH_SHORT).show();
            textView.setText(ftpHelper.isServerRunning() ?
                    "server running\n input ftp://" + NetworkUtil.getIPAddress(FTPServerActivity.this) + ":2221 to manage files" :
                    "server stopped");
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

        setTitle("FTP Server");

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
            textView.setText("server running\n input ftp://" + NetworkUtil.getIPAddress(FTPServerActivity.this) + ":2221 to manage files");
        else
            textView.setText("failed to start server");
    }

    @Override
    public void onServerStop() {
        textView.setText("server stopped");
    }
}
