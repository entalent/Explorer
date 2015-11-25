package cn.edu.bit.cs.explorer;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;

import java.io.File;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.ui.BlockingDialog;
import cn.edu.bit.cs.explorer.ui.customview.FileListItem;
import cn.edu.bit.cs.explorer.ui.customview.PathIndicator;
import cn.edu.bit.cs.explorer.ui.fragment.BaseFileListFragment;
import cn.edu.bit.cs.explorer.util.FileUtil;

public class MainActivity extends AppCompatActivity
        implements BaseFileListFragment.FileListListener, PathIndicator.OnPathChangeListener, FileUtil.IFileUtil {

    static final int ACTION_COPY = 0x0;
    static final int ACTION_CUT = 0x1;

    static final int STATE_NONE = 0x0;
    static final int STATE_SELECT_FILE = 0x1;
    static final int STATE_PASTE = 0x2;

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    PathIndicator indicator;

    BaseFileListFragment fragment;

    ActionMode actionMode;
    int actionModeState;

    int prevSelectedFileCnt = 0;
    ArrayList<File> selectedFiles;
    ArrayList<File> pasteBin = new ArrayList<>();
    int pasteBinAction;


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
        if(files.size() > 0) {
            actionModeState = STATE_SELECT_FILE;
            actionMode = toolbar.startActionMode(actionModeCallback);
        } else if(files.size() == 0 && pasteBin.size() == 0) {
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
            switch(actionModeState) {
                case STATE_NONE:

                    break;

                case STATE_SELECT_FILE:
                    menu.clear();
                    menu.add("copy").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    menu.add("cut").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    menu.add("delete").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    if (selectedFiles != null && selectedFiles.size() == 1) {
                        menu.add("rename").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    }
                    break;

                case STATE_PASTE:
                    menu.clear();
                    menu.add("paste").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    break;
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
                Toast.makeText(MainActivity.this, "copy", Toast.LENGTH_SHORT).show();
                pasteBin.clear();
                pasteBin.addAll(selectedFiles);
                pasteBinAction = ACTION_COPY;

                switchActionModeState(STATE_PASTE);

            } else if (item.getTitle().equals("cut")){
                pasteBin.clear();
                pasteBin.addAll(selectedFiles);
                pasteBinAction = ACTION_CUT;

                switchActionModeState(STATE_PASTE);

            } else if (item.getTitle().equals("delete")){

            } else if (item.getTitle().equals("rename")){

            } else if (item.getTitle().equals("paste")) {
                executePaste();
                fragment.refreshCurrentDir();
                pasteBin.clear();
                switchActionModeState(STATE_NONE);

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

    private void switchActionModeState(int state) {
        actionModeState = state;
        actionMode.finish();
        if(state == STATE_NONE)
            return;
        actionMode = toolbar.startActionMode(actionModeCallback);
    }

    private void executePaste() {
        //TODO: execute async
        //TODO: task list

        if(pasteBinAction == ACTION_COPY) {
            int cnt1 = 0;
            File currentPath = fragment.getCurrentDir();
            for (File i : pasteBin) {
                cnt1 += FileUtil.copyFile(i, currentPath, MainActivity.this);
            }
            System.out.println("success " + cnt1);
        } else if(pasteBinAction == ACTION_CUT) {
            int cnt1 = 0;
            File currentPath = fragment.getCurrentDir();
            for (File i : pasteBin) {
                cnt1 += FileUtil.cutFile(i, currentPath, MainActivity.this);
            }
            System.out.println("success " + cnt1);
        }
    }

    @Override
    public boolean onDestiationFileExist(File srcFile, File dstFile) {
        FileExistsConfirmDialog dialog = new FileExistsConfirmDialog(MainActivity.this, srcFile, dstFile);
        if(dialog.showDialog() == BlockingDialog.DIALOG_OK) {
            Toast.makeText(MainActivity.this, "override", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(MainActivity.this, "not override", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    class FileExistsConfirmDialog extends BlockingDialog {
        File srcFile, dstFile;

        public FileExistsConfirmDialog(Context context, File _srcFile, File _dstFile) {
            super(context, "title", "message");
            this.srcFile = _srcFile;
            this.dstFile = _dstFile;
            setFiles();
        }


        public void setFiles() {
            FileListItem item1 = (FileListItem) findViewById(R.id.file1);
            FileListItem item2 = (FileListItem) findViewById(R.id.file2);
            item1.setFile(srcFile);
            item2.setFile(dstFile);
        }

        @Override
        public void onCreate() {
            super.onCreate(null);

            setContentView(R.layout.dialog_blocking);
            findViewById(R.id.cancelBtn).setOnClickListener(new android.view.View.OnClickListener() {

                @Override
                public void onClick(View paramView) {
                    endDialog(BlockingDialog.DIALOG_CANCEL);
                }
            });
            findViewById(R.id.okBtn).setOnClickListener(new android.view.View.OnClickListener() {

                @Override
                public void onClick(View paramView) {
                    endDialog(BlockingDialog.DIALOG_OK);
                }
            });


        }
    }
}
