package cn.edu.bit.cs.explorer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import cn.edu.bit.cs.explorer.util.AdbUtil;
import cn.edu.bit.cs.explorer.util.NetworkUtil;

/**
 * Created by entalent on 2015/12/5.
 */
public class AdbWifiActivity extends BaseActivity {

    public static int REQUEST_CODE_NETWORK_PERMISSIONS = 1000000011;

    int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_adb_wifi);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(PackageManager.PERMISSION_GRANTED !=
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(AdbWifiActivity.this,
                        "You must grant the permission of accessing network state to use ADB Wifi feature.",
                        Toast.LENGTH_LONG).show();
                String[] permissions = {
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.INTERNET
                };
                requestPermissions(permissions, REQUEST_CODE_NETWORK_PERMISSIONS);
            }
        }

        refreshState();
        (findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (port < 0) {
                    try {
                        AdbUtil.startAdbd(5555);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        AdbUtil.startAdbd(-1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                refreshState();
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int result : grantResults) {
            if(result != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }



    void refreshState() {
        if(NetworkUtil.isWifiConnected(AdbWifiActivity.this)) {
            String IP = NetworkUtil.getIPAddress(AdbWifiActivity.this);
            ((TextView)findViewById(R.id.textView)).setText("IP is " + IP + "\n");

            try {
                System.out.println("adb port = " + AdbUtil.getAdbPort());
                port = Integer.parseInt(AdbUtil.getAdbPort());
            } catch (Exception e) {
                port = -1;
            }
            if(port != -1) {
                ((TextView)findViewById(R.id.textView)).append("adb listens on port " + port);
                ((Button)findViewById(R.id.button)).setText("stop adb wifi");
            } else {
                ((TextView)findViewById(R.id.textView)).append("adb not listening");
                ((Button)findViewById(R.id.button)).setText("start adb wifi");
            }
        }

    }
}
