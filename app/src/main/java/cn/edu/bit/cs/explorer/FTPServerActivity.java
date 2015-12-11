package cn.edu.bit.cs.explorer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.ftpserver.FtpServerFactory;

import cn.edu.bit.cs.explorer.service.FtpService;
import cn.edu.bit.cs.explorer.util.FtpHelper;

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
            ftpHelper.addListener(FTPServerActivity.this);
            Toast.makeText(FTPServerActivity.this, "service connected", Toast.LENGTH_SHORT).show();
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
        setTitle("FTP Server");

        Intent intent = new Intent(FTPServerActivity.this, FtpService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);

        buttonLaunch = (Button) findViewById(R.id.btn_launch);
        textView = (TextView)findViewById(R.id.textView);

        buttonLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ftpHelper.isServerRunning())
                    ftpHelper.stopServer();
                else
                    ftpHelper.startServer();
            }
        });
    }

    @Override
    public void onServerStartFinish(boolean running) {
        textView.setText("server running");
    }

    @Override
    public void onServerStop() {
        textView.setText("server stopped");
    }
}
