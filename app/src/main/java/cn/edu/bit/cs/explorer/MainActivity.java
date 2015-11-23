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

    ActionMode actionMode;

    int prevSelectedFileCnt = 0;
    ArrayList<File> selectedFiles;

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
        if(file.isDirectory())
            indicator.setCurrentDir(file);
    }

    @Override
    public void onSelectedFilesChange(ArrayList<File> files) {
        this.selectedFiles = files;
        /*
        Exception e = new Exception();
        e.printStackTrace(System.out);
        System.out.println("onSelectedFilesChange");
        */
        for(File i : files){
            System.out.println(i.getAbsolutePath());
        }
        if(files.size() > 0 && prevSelectedFileCnt == 0) {
            actionMode = toolbar.startActionMode(actionModeCallback);
        } else if(files.size() == 0) {
            actionMode.finish();
        }
        prevSelectedFileCnt = files.size();
    }

    @Override
    public void onPathChange(File file) {
        fragment.setCurrentDir(file);
    }

    ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.clear();
            menu.add("copy").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add("cut").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add("delete").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            if(selectedFiles.size() == 1){
                menu.add("rename").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getTitle().equals("copy")){

            } else if (item.getTitle().equals("cut")){

            } else if (item.getTitle().equals("delete")){

            } else if (item.getTitle().equals("rename")){

            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
