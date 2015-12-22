package cn.edu.bit.cs.explorer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.views.ButtonFloatSmall;
import com.gc.materialdesign.views.ProgressBarIndeterminate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.service.MainService;
import cn.edu.bit.cs.explorer.ui.customview.PathIndicator;
import cn.edu.bit.cs.explorer.ui.customview.StorageVolumeLabel;
import cn.edu.bit.cs.explorer.ui.dialog.DeleteDialog;
import cn.edu.bit.cs.explorer.ui.dialog.NewFileOrFolderDialog;
import cn.edu.bit.cs.explorer.ui.dialog.RenameDialog;
import cn.edu.bit.cs.explorer.ui.dialog.SortOrderDialog;
import cn.edu.bit.cs.explorer.ui.fragment.BaseFileListFragment;
import cn.edu.bit.cs.explorer.util.FileUtil;
import cn.edu.bit.cs.explorer.util.StorageUtil;
import cn.edu.bit.cs.explorer.util.TextUtil;
import cn.edu.bit.cs.explorer.tasks.DeleteTask;
import cn.edu.bit.cs.explorer.tasks.FileAsyncTask;
import cn.edu.bit.cs.explorer.tasks.PasteTask;
import cn.edu.bit.cs.explorer.tasks.UnzipTask;
import cn.edu.bit.cs.explorer.tasks.ZipTask;

public class MainActivity extends AppCompatActivity
        implements BaseFileListFragment.FileListListener, PathIndicator.OnPathChangeListener,
            NavigationView.OnNavigationItemSelectedListener{

    public static final int ACTION_COPY = 0x0;
    public static final int ACTION_CUT = 0x1;

    static final int STATE_NONE = 0x0;
    static final int STATE_SELECT_FILE = 0x1;
    static final int STATE_PASTE = 0x2;

    static final int MENUITEM_ID_COPY = 0x1,
                    MENUITEM_ID_CUT = 0x2,
                    MENUITEM_ID_PASTE = 0x3,
                    MENUITEM_ID_RENAME = 0x4,
                    MENUITEM_ID_DELETE = 0x5,
            MENUITEM_ID_SHARE = 0x6,
            MENUITEM_ID_COMPRESS = 0x7,
            MENUITEM_ID_SELECT_ALL = 0x8,
            MENUITEM_ID_DESELECT_ALL = 0x10;

    public static int REQUEST_CODE_STORAGE_PERMISSIONS = 1000000007;

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    PathIndicator indicator;
    LinearLayout storageList;
    ButtonFloat addButton;
    ButtonFloatSmall newFileBtn, newFolderBtn, searchBtn;
    FrameLayout cover;
    ProgressBarIndeterminate progressBar;
    CoordinatorLayout coor;
    AppBarLayout bar;

    BaseFileListFragment fragment;

    ActionMode actionMode;
    int actionModeState;

    ArrayList<StorageUtil.StorageVolumeInfo> storageVolumes;

    int prevSelectedFileCnt = 0;
    ArrayList<File> selectedFiles;
    ArrayList<File> pasteBin = new ArrayList<>();
    int pasteBinAction;

    boolean smallButtonShowing = false;

    MainService mainService;
    ArrayList<FileAsyncTask> tasksToExecute = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(PackageManager.PERMISSION_GRANTED !=
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.permission_request_message_main),
                        Toast.LENGTH_LONG).show();
                String[] permissions = {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
                requestPermissions(permissions, REQUEST_CODE_STORAGE_PERMISSIONS);
            }
        }

        startService(new Intent(MainActivity.this, MainService.class));
        bindService(new Intent(MainActivity.this, MainService.class), conn, BIND_AUTO_CREATE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.action_main_service));
        registerReceiver(receiver, intentFilter);


        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.id_nv_menu);
        indicator = (PathIndicator)findViewById(R.id.path_indicator);
        fragment = (BaseFileListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        storageList = (LinearLayout) findViewById(R.id.storageList);
        addButton = (ButtonFloat)findViewById(R.id.buttonAdd);
        newFileBtn = (ButtonFloatSmall)findViewById(R.id.btnNewFile);
        newFolderBtn = (ButtonFloatSmall)findViewById(R.id.btnNewFolder);
        searchBtn = (ButtonFloatSmall)findViewById(R.id.btnSearch);
        cover = (FrameLayout)findViewById(R.id.cover);
        progressBar = (ProgressBarIndeterminate)findViewById(R.id.progressBar);
        coor = (CoordinatorLayout)findViewById(R.id.coor);
        bar =(AppBarLayout)findViewById(R.id.id_app_bar);

        bar.setBackground(getResources().getDrawable(R.drawable.bg));


        setSupportActionBar(toolbar);

        //toolbar.setTitleTextColor(Color.WHITE);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState(); //show a spinning button on ActionBar

        navigationView.setNavigationItemSelectedListener(this);

        fragment.setRootDir(Environment.getExternalStorageDirectory());
        fragment.setFileListListener(MainActivity.this);
        registerForContextMenu((ListView) fragment.getAdapterView());

        indicator.setRootDir(Environment.getExternalStorageDirectory());
        indicator.setOnPathChangeListener(MainActivity.this);

        refreshStorageVolumes();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!smallButtonShowing)
                    showSmallButtons();
                else
                    hideSmallButton();
            }
        });


        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSmallButton();
                smallButtonShowing = false;
            }
        });

        newFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeNewFile();
            }
        });

        newFolderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeNewFolder();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SearchActivity.class);
                i.putExtra("path", fragment.getCurrentDir());
                startActivity(i);
            }
        });

        Intent intent = getIntent();
        Toast.makeText(MainActivity.this, intent.getAction(), Toast.LENGTH_SHORT).show();
        //android.intent.action.VIEW
        if(intent.getAction().equals(Intent.ACTION_VIEW)) {
            String scheme = intent.getScheme();
            if(scheme.equals(ContentResolver.SCHEME_FILE)) {
                Uri uri = intent.getData();
                File file = new File(uri.getPath());
                executeUnzip(file);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        unregisterReceiver(receiver);
    }

    @Override
    public void onOpenFileOrDirectory(File file) {
        if(file.isDirectory())
            indicator.setCurrentDir(file);
        else {
            FileUtil.openFile(MainActivity.this, file);
        }
    }

    @Override
    public void onSelectedFilesChange(ArrayList<File> files) {
        this.selectedFiles = files;
        if((prevSelectedFileCnt == 0 && files.size() > 0) ||
                (prevSelectedFileCnt == 1 && files.size() > 1) ||
                (prevSelectedFileCnt > 1 && files.size() == 1) ||
                (prevSelectedFileCnt > 0 && files.size() > 0)) {
            actionModeState = STATE_SELECT_FILE;
            actionMode = toolbar.startActionMode(actionModeCallback);
        } else if(files.size() == 0 && pasteBin.size() == 0) {
            actionMode.finish();
        }
        prevSelectedFileCnt = files.size();
    }

    @Override
    public void onItemLongClick(File file) {
        Toast.makeText(MainActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        
    }

    long prevBackPressedTime = 0, currentBackPressedTime;

    @Override
    public void onBackPressed() {
        if(smallButtonShowing) {
            hideSmallButton();
        } else if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(!fragment.goToParentDir()){
            currentBackPressedTime = System.currentTimeMillis();
            if(currentBackPressedTime - prevBackPressedTime <= 1500) {
                finish();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.message_press_again_to_exit), Toast.LENGTH_SHORT).show();
            }
            prevBackPressedTime = currentBackPressedTime;
        }
    }

    @Override
    public void onPathChange(File file) {
        fragment.setCurrentDir(file);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, 0, 0, getString(R.string.menu_item_sort_order));
        menu.add(0, 1, 1, getString(R.string.menu_item_select_all));
        menu.add(0, 2, 2, getString(R.string.menu_item_deselect_all));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case 0:
                SortOrderDialog dialog = new SortOrderDialog(MainActivity.this);
                fragment.setSortMethod(dialog.showDialog());
                break;
            case 1:
                fragment.selectAll();
                break;
            case 2:
                fragment.deselectAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch(id) {
            /*
            case R.id.item_settings:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
                */
            case R.id.item_adbw:
                Intent i2 = new Intent(MainActivity.this, AdbWifiActivity.class);
                startActivity(i2);
                break;
            case R.id.item_ftp_server:
                Intent i3 = new Intent(MainActivity.this, FTPServerActivity.class);
                startActivity(i3);
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSIONS) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fragment.refreshCurrentContent();
            } else {
                finish();
            }
        }
    }

    ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            switch(actionModeState) {
                case STATE_NONE:

                    break;

                case STATE_SELECT_FILE:
                    menu.clear();
                    menu.add(0, MENUITEM_ID_COPY, 0, getString(R.string.menu_item_copy)).setIcon(R.drawable.ic_content_copy_white_18dp).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    menu.add(0, MENUITEM_ID_CUT, 1, getString(R.string.menu_item_cut)).setIcon(R.drawable.ic_content_cut_white_18dp).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    menu.add(0, MENUITEM_ID_DELETE, 2, getString(R.string.menu_item_delete)).setIcon(R.drawable.ic_delete_white_18dp).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    if(selectedFiles.size() == 1) {
                        menu.add(0, MENUITEM_ID_RENAME, 3, getString(R.string.menu_item_rename)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                    }
                    menu.add(0, MENUITEM_ID_SELECT_ALL, 4, getString(R.string.menu_item_select_all)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
                    menu.add(0, MENUITEM_ID_DESELECT_ALL, 5, getString(R.string.menu_item_deselect_all)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                    menu.add(0, MENUITEM_ID_COMPRESS, 5, getString(R.string.menu_item_compress)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
                    menu.add(0, MENUITEM_ID_SHARE, 6, getString(R.string.menu_item_share)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    break;

                case STATE_PASTE:
                    menu.clear();
                    menu.add(0, MENUITEM_ID_PASTE, 0, getString(R.string.menu_item_paste)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
            int id = item.getItemId();
            if(id == MENUITEM_ID_COPY){
                pasteBin.clear();
                pasteBin.addAll(selectedFiles);
                pasteBinAction = ACTION_COPY;
                switchActionModeState(STATE_PASTE);
            }
            else if(id == MENUITEM_ID_CUT){
                pasteBin.clear();
                pasteBin.addAll(selectedFiles);
                pasteBinAction = ACTION_CUT;
                switchActionModeState(STATE_PASTE);
            }
            else if(id == MENUITEM_ID_DELETE){
                final DeleteDialog dialog = new DeleteDialog(MainActivity.this);
                dialog.show();
                dialog.getDeleteInfoText().setText(String.format(getString(R.string.message_delete_confirm), selectedFiles.size()));
                dialog.getButtonCancel().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedFiles.clear();
                        clearSelectedAndRefrfesh();
                        dialog.dismiss();
                    }
                });
                dialog.getButtonDelete().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        executeDelete();
                        clearSelectedAndRefrfesh();
                        dialog.dismiss();
                    }
                });
            }
            else if(id == MENUITEM_ID_SELECT_ALL){
                fragment.selectAll();
            }
            else if(id == MENUITEM_ID_DESELECT_ALL){
                fragment.deselectAll();
            }
            else if(id == MENUITEM_ID_RENAME) {
                executeRename(selectedFiles.get(0));
            }
            else if(id == MENUITEM_ID_COMPRESS){
                executeCompress();
            }
            else if(id == MENUITEM_ID_PASTE){
                executePaste();
                clearSelectedAndRefrfesh();
                pasteBin.clear();
            }
            else if(id == MENUITEM_ID_SHARE){
                Intent intent;
                if(selectedFiles.size() > 1) {
                    intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    intent.setType("*/*");
                    ArrayList<Uri> uris = new ArrayList<>();
                    for(File i : selectedFiles) {
                        uris.add(Uri.fromFile(i));
                    }
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    } else {
                        intent = new Intent(Intent.ACTION_SEND);
                        intent.setType(TextUtil.getMimeTypeFromFile(selectedFiles.get(0)));
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(selectedFiles.get(0)));
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.message_share)));
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //fragment.deselectAll();
        }
    };

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainService = ((MainService.MainServiceBinder)service).getService();
            if(tasksToExecute != null) {
                for(FileAsyncTask i : tasksToExecute) {
                    mainService.addTask(i);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainService = null;
            if(tasksToExecute != null && tasksToExecute.size() > 0) {
                Intent intent = new Intent(MainActivity.this, MainService.class);
                startService(intent);
                bindService(intent, conn, BIND_AUTO_CREATE);
            }

        }
    };

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra("action", 0);
            //Toast.makeText(MainActivity.this, "received " + action, Toast.LENGTH_SHORT).show();
            if(action == MainService.ACTION_SHOW_PROGRESS){
                progressBar.setVisibility(View.VISIBLE);
            } else if(action == MainService.ACTION_HIDE_PROGRESS) {
                progressBar.setVisibility(View.GONE);
            } else if(action == MainService.ACTION_REFRESH_DIRECTORY){
                File file = (File) intent.getSerializableExtra("file");
                if(file.equals(fragment.getCurrentDir())){
                    fragment.refreshCurrentContent();
                }
            } else if(action == MainService.ACTION_GOTO_DIRECTORY) {
                File file = (File) intent.getSerializableExtra("file");
                for(StorageUtil.StorageVolumeInfo i : storageVolumes){
                    File newRoot = new File(i.path);
                    if(FileUtil.isChildDirectoty(newRoot, file)){
                        fragment.setRootDir(newRoot);
                        indicator.setRootDir(newRoot);
                        break;
                    }
                }
                fragment.setCurrentDir(file);
                indicator.setCurrentDir(file);
            }
        }
    };

    public void clearSelectedAndRefrfesh() {
        fragment.deselectAll();
        switchActionModeState(STATE_NONE);
    }

    private void refreshStorageVolumes() {
        storageList.removeAllViews();
        storageVolumes = StorageUtil.getVolumePaths(MainActivity.this);
        for(StorageUtil.StorageVolumeInfo i : storageVolumes) {
            final StorageVolumeLabel label = new StorageVolumeLabel(MainActivity.this, null);
            label.setVolumeInfo(i);
            label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File storageRoot = new File(label.getVolumeInfo().path);
                    fragment.setRootDir(storageRoot);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    indicator.setRootDir(storageRoot);
                }
            });
            storageList.addView(label);
        }
    }

    private void switchActionModeState(int state) {
        actionModeState = state;
        actionMode.finish();
        if(state == STATE_NONE)
            return;
        actionMode = toolbar.startActionMode(actionModeCallback);
    }

    private void executeTask(FileAsyncTask task) {
        if(mainService != null){
            mainService.addTask(task);
        } else {
            tasksToExecute.add(task);
        }
    }

    private void executePaste() {
        FileAsyncTask task = new PasteTask(MainActivity.this, fragment.getCurrentDir(), pasteBin, pasteBinAction);
        executeTask(task);
    }

    private void executeDelete() {
        FileAsyncTask task = new DeleteTask(MainActivity.this, fragment.getCurrentDir(), selectedFiles);
        executeTask(task);
    }

    private void executeNewFolder() {
        final NewFileOrFolderDialog dialog = new NewFileOrFolderDialog(MainActivity.this);
        dialog.show();
        dialog.setTitle("new folder");
        dialog.getCancelBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                hideSmallButton();
            }
        });

        dialog.getCreateBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File newFile = new File(fragment.getCurrentDir() + File.separator + dialog.getNewFileNameText().getText().toString());
                if (!newFile.exists() && newFile.mkdir()) {
                    fragment.refreshCurrentContent();
                } else {
                    Toast.makeText(MainActivity.this, "failed to create new folder", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                hideSmallButton();
                fragment.refreshCurrentContent();
            }

        });
    }

    private void executeRename(final File originalFile) {
        final RenameDialog dialog = new RenameDialog(MainActivity.this);
        dialog.show();
        dialog.getOriginalNameText().setText(originalFile.getName());
        dialog.getNewNameText().setText(originalFile.getName());
        dialog.getButtonAccept().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File newFile = new File(originalFile.getParent() + File.separator + dialog.getNewNameText().getText());
                if (originalFile.renameTo(newFile)) {
                    Toast.makeText(MainActivity.this, getString(R.string.message_rename_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.message_rename_fail), Toast.LENGTH_SHORT).show();
                }

                clearSelectedAndRefrfesh();
                dialog.dismiss();
                fragment.refreshCurrentContent();
            }
        });
        dialog.getButtonCancel().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelectedAndRefrfesh();
                dialog.dismiss();
            }
        });
    }

    private void executeNewFile() {
        final NewFileOrFolderDialog dialog = new NewFileOrFolderDialog(MainActivity.this);
        dialog.show();
        dialog.setTitle(getString(R.string.message_new_file));
        dialog.getCancelBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                hideSmallButton();
            }
        });

        dialog.getCreateBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File newFile = new File(fragment.getCurrentDir() + File.separator + dialog.getNewFileNameText().getText().toString());
                if (!newFile.exists()) {
                    try {
                        newFile.createNewFile();
                        fragment.refreshCurrentContent();
                        Toast.makeText(MainActivity.this, getString(R.string.message_rename_success), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, getString(R.string.message_rename_fail), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.message_file_already_exists), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                hideSmallButton();
                fragment.refreshCurrentContent();
            }
        });
    }

    private void executeCompress() {
        //TODO: get zip directory from sharedPreference
        String zipDir = (getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + File.separator + "zipFile");
        String zipFileName;
        if(selectedFiles.size() == 1) {
            zipFileName = selectedFiles.get(0).getName();
        } else {
            zipFileName = fragment.getCurrentDir().getName();
        }
        zipFileName += " - " + TextUtil.formatTimeStr(System.currentTimeMillis()) + ".zip";
        File zipFile = new File(zipDir + File.separator + zipFileName);
        System.out.println("compress " + zipFile.getAbsolutePath());
        ZipTask zipTask = new ZipTask(MainActivity.this, fragment.getCurrentDir(), selectedFiles, zipFile);
        executeTask(zipTask);
    }

    private void executeUnzip(File file) {
        Toast.makeText(MainActivity.this, "unzip", Toast.LENGTH_SHORT).show();
        ArrayList<File> files = new ArrayList<>();
        files.add(file);
        File unzipDir = new File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + File.separator +
                        "unzippedFile" + File.separator
        );

        unzipDir.mkdirs();

        UnzipTask task = new UnzipTask(MainActivity.this,
                unzipDir,
                files);
        Toast.makeText(MainActivity.this, (mainService == null) + "", Toast.LENGTH_SHORT).show();
        executeTask(task);
    }

    private void showSmallButtons() {
        Animation showAnim = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 1000, Animation.ABSOLUTE, 0);
        showAnim.setDuration(700);
        newFileBtn.startAnimation(showAnim);
        newFolderBtn.startAnimation(showAnim);
        searchBtn.startAnimation(showAnim);
        newFileBtn.setVisibility(View.VISIBLE);
        newFolderBtn.setVisibility(View.VISIBLE);
        searchBtn.setVisibility(View.VISIBLE);


        Animation rotateAnim = new RotateAnimation(0f, 45f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnim.setDuration(100);
        rotateAnim.setFillBefore(true);
        rotateAnim.setFillAfter(true);
        addButton.startAnimation(rotateAnim);
        cover.setVisibility(View.VISIBLE);
        smallButtonShowing = true;
    }

    private void hideSmallButton() {
        Animation hideAnim = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 1000);
        hideAnim.setDuration(700);
        newFileBtn.startAnimation(hideAnim);
        newFolderBtn.startAnimation(hideAnim);
        searchBtn.startAnimation(hideAnim);
        newFileBtn.setVisibility(View.INVISIBLE);
        newFolderBtn.setVisibility(View.INVISIBLE);
        searchBtn.setVisibility(View.INVISIBLE);

        Animation rotateAnim = new RotateAnimation(45f, 0f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnim.setDuration(100);
        rotateAnim.setFillBefore(true);
        rotateAnim.setFillAfter(true);
        addButton.startAnimation(rotateAnim);
        cover.setVisibility(View.GONE);
        smallButtonShowing = false;
    }



}
