package cn.edu.bit.cs.explorer;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.ui.customview.PathIndicator;
import cn.edu.bit.cs.explorer.ui.fragment.BaseFileListFragment;

public class MainActivity extends AppCompatActivity implements BaseFileListFragment.FileListListener, PathIndicator.OnPathChangeListener{

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    PathIndicator indicator;

    BaseFileListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        indicator = (PathIndicator)findViewById(R.id.path_indicator);
        fragment = (BaseFileListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState(); //show a spinning button on ActionBar

        fragment.setRootDir(Environment.getExternalStorageDirectory());
        fragment.setFileListListener(MainActivity.this);

        indicator.setRootDir(Environment.getExternalStorageDirectory());
        indicator.setOnPathChangeListener(MainActivity.this);

    }

    @Override
    public void onOpenFileOrDirectory(File file) {
        Toast.makeText(MainActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        if(file.isDirectory())
            indicator.setCurrentDir(file);
    }

    @Override
    public void onSelectedFilesChange(ArrayList<File> files) {
        Toast.makeText(MainActivity.this, files.size() + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPathChange(File file) {
        fragment.setCurrentDir(file);
    }
}
