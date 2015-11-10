package cn.edu.bit.cs.explorer;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import cn.edu.bit.cs.explorer.customview.StorageVolumeLabel;
import cn.edu.bit.cs.explorer.util.StorageUtil;
import cn.edu.bit.cs.explorer.util.StorageVolumeInfo;
import cn.edu.bit.cs.explorer.util.TranslucentUtil;

public class MainActivity extends AppCompatActivity {

    NavigationView navigationView;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    LinearLayout storageVolumeList, optionsList;

    ArrayList<StorageVolumeInfo> volumes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TranslucentUtil.setTranslucent(MainActivity.this);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        navigationView = (NavigationView)findViewById(R.id.navigation);
        storageVolumeList = (LinearLayout) findViewById(R.id.linearLayout1);
        optionsList = (LinearLayout)findViewById(R.id.linearLayout2);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState(); //show a spinning button on ActionBar

        TranslucentUtil.setTranslucent(MainActivity.this);

        refreshStorageVolume();
    }

    public void refreshStorageVolume() {
        storageVolumeList.removeAllViews();
        volumes = StorageUtil.getVolumePaths(MainActivity.this);
        for(StorageVolumeInfo i : volumes){
            StorageVolumeLabel label = new StorageVolumeLabel(MainActivity.this, null);
            label.setVolumeInfo(i);
            storageVolumeList.addView(label);
            label.setOnClickListener(onVolumeSelectedistener);
        }
    }

    View.OnClickListener onVolumeSelectedistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            StorageVolumeLabel label = (StorageVolumeLabel)v;
            Toast.makeText(MainActivity.this, label.getVolumeInfo().path, Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    };
}
