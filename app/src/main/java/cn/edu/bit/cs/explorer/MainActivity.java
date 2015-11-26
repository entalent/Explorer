package cn.edu.bit.cs.explorer;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.Button;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.views.ButtonFloatSmall;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.ui.dialog.BlockingDialog;
import cn.edu.bit.cs.explorer.ui.customview.FileListItem;
import cn.edu.bit.cs.explorer.ui.customview.PathIndicator;
import cn.edu.bit.cs.explorer.ui.customview.StorageVolumeLabel;
import cn.edu.bit.cs.explorer.ui.fragment.BaseFileListFragment;
import cn.edu.bit.cs.explorer.util.FileUtil;
import cn.edu.bit.cs.explorer.util.StorageUtil;

public class MainActivity extends AppCompatActivity
        implements BaseFileListFragment.FileListListener, PathIndicator.OnPathChangeListener {

    static final int ACTION_COPY = 0x0;
    static final int ACTION_CUT = 0x1;

    static final int STATE_NONE = 0x0;
    static final int STATE_SELECT_FILE = 0x1;
    static final int STATE_PASTE = 0x2;

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    PathIndicator indicator;
    LinearLayout storageList;
    ButtonFloat addbutton;
    ButtonFloatSmall newFileBtn, newFolderBtn, searchBtn;
    FrameLayout cover;

    BaseFileListFragment fragment;

    ActionMode actionMode;
    int actionModeState;

    ArrayList<StorageUtil.StorageVolumeInfo> storageVolumes;

    int prevSelectedFileCnt = 0;
    ArrayList<File> selectedFiles;
    ArrayList<File> pasteBin = new ArrayList<>();
    int pasteBinAction;

    boolean smallButtonShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        indicator = (PathIndicator)findViewById(R.id.path_indicator);
        fragment = (BaseFileListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        storageList = (LinearLayout) findViewById(R.id.storageList);
        addbutton = (ButtonFloat)findViewById(R.id.buttonAdd);
        newFileBtn = (ButtonFloatSmall)findViewById(R.id.btnNewFile);
        newFolderBtn = (ButtonFloatSmall)findViewById(R.id.btnNewFolder);
        searchBtn = (ButtonFloatSmall)findViewById(R.id.btnSearch);
        cover = (FrameLayout)findViewById(R.id.cover);

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

        refreshStorageVolumes();

        addbutton.setOnClickListener(new View.OnClickListener() {


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

        newFolderBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                executeNewFolder();
            }
        });
    }

    @Override
    public void onOpenFileOrDirectory(File file) {
        if(file.isDirectory())
            indicator.setCurrentDir(file);
    }

    @Override
    public void onSelectedFilesChange(ArrayList<File> files) {
        this.selectedFiles = files;
        if(prevSelectedFileCnt == 0 &&
                files.size() > 0) {
            actionModeState = STATE_SELECT_FILE;
            actionMode = toolbar.startActionMode(actionModeCallback);
        } else if(prevSelectedFileCnt > 0 && files.size() > 0) {
            actionMode.invalidate();
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
                    menu.add("copy").setIcon(R.drawable.ic_content_copy_white_18dp).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    menu.add("cut").setIcon(R.drawable.ic_content_cut_white_18dp).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    menu.add("delete").setIcon(R.drawable.ic_delete_white_18dp).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    if (selectedFiles != null && selectedFiles.size() == 1) {
                        menu.add("rename").setIcon(R.drawable.ic_create_white_18dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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
                final DeleteDialog dialog = new DeleteDialog(MainActivity.this);
                dialog.show();
                dialog.getDeleteInfoText().setText("delete selected " + selectedFiles.size() + " files ?");
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

            } else if (item.getTitle().equals("rename")){
                //TODO: multiple file names?
                final RenameDialog dialog = new RenameDialog(MainActivity.this);
                dialog.show();
                final File originalFile = selectedFiles.get(0);
                dialog.getOriginalNameText().setText(originalFile.getName());
                dialog.getNewNameText().setText(originalFile.getName());
                dialog.getButtonAccept().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File newFile = new File(originalFile.getParent() + File.separator + dialog.getNewNameText().getText());
                        if (originalFile.renameTo(newFile)) {
                            Toast.makeText(MainActivity.this, "rename success", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "rename fail", Toast.LENGTH_SHORT).show();
                        }

                        clearSelectedAndRefrfesh();
                        dialog.dismiss();
                    }
                });
                dialog.getButtonCancel().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearSelectedAndRefrfesh();
                        dialog.dismiss();
                    }
                });
            } else if (item.getTitle().equals("paste")) {
                executePaste();
                clearSelectedAndRefrfesh();
                pasteBin.clear();

            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            fragment.deselectAll();
        }
    };

    public void clearSelectedAndRefrfesh() {
        fragment.deselectAll();
        fragment.refreshCurrentDir();
        switchActionModeState(STATE_NONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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

    private int executePaste() {
        //TODO: execute async task list

        if(pasteBinAction == ACTION_COPY) {
            int cnt1 = 0;
            File currentPath = fragment.getCurrentDir();
            for (File i : pasteBin) {
                try {
                    cnt1 += FileUtil.copyFile(i, currentPath, new FileHandler());
                } catch (IllegalArgumentException e) {
                    Toast.makeText(MainActivity.this, "source directory identical with target directory", Toast.LENGTH_SHORT).show();
                    return cnt1;
                }
            }
            return cnt1;
        } else if(pasteBinAction == ACTION_CUT) {
            int cnt1 = 0;
            File currentPath = fragment.getCurrentDir();
            for (File i : pasteBin) {
                try {
                    cnt1 += FileUtil.cutFile(i, currentPath, new FileHandler());
                } catch (IllegalArgumentException e) {
                    Toast.makeText(MainActivity.this, "source directory identical with target directory", Toast.LENGTH_SHORT).show();
                    return cnt1;
                }
            }
            return cnt1;
        }
        return 0;
    }

    private int executeDelete() {
        int cnt = 0;
        for(File i : selectedFiles) {
            cnt += FileUtil.deleteFile(i);
        }
        return cnt;
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
                if(!newFile.exists() && newFile.mkdir()) {
                    fragment.refreshCurrentDir();
                } else {

                }
                dialog.dismiss();
                hideSmallButton();
            }

        });
    }

    private void executeNewFile() {
        final NewFileOrFolderDialog dialog = new NewFileOrFolderDialog(MainActivity.this);
        dialog.show();
        dialog.setTitle("new file");
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
                if(!newFile.exists()) {
                    try {
                        newFile.createNewFile();
                        fragment.refreshCurrentDir();
                    } catch (IOException e) {

                    }

                } else {

                }
                dialog.dismiss();
                hideSmallButton();
            }
        });
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
        rotateAnim.setDuration(700);
        rotateAnim.setFillBefore(true);
        rotateAnim.setFillAfter(true);
        addbutton.startAnimation(rotateAnim);
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
        rotateAnim.setDuration(700);
        //FIXME: animation ?!
        rotateAnim.setFillBefore(true);
        rotateAnim.setFillAfter(true);
        addbutton.startAnimation(rotateAnim);
        cover.setVisibility(View.GONE);
        smallButtonShowing = false;
    }

    class FileExistsConfirmDialog extends BlockingDialog {
        public static final int DIALOG_OVERRIDE = 0x0,
                            DIALOG_CANCEL = 0x1,
                            DIALOG_OVERRIDE_FOR_ALL = 0x2;

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
            item1.getCheckBox().setVisibility(View.GONE);
            item2.getCheckBox().setVisibility(View.GONE);
        }

        @Override
        public void onCreate() {
            super.onCreate(null);
            setTheme(R.style.AppTheme);
            setTitle("file conflict");
            setContentView(R.layout.dialog_blocking);
            findViewById(R.id.cancelBtn).setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(View paramView) {
                    endDialog(DIALOG_CANCEL);
                }
            });
            findViewById(R.id.okBtn).setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(View paramView) {
                    endDialog(DIALOG_OVERRIDE);
                }
            });
            findViewById(R.id.forall).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endDialog(DIALOG_OVERRIDE_FOR_ALL);
                }
            });
        }
    }

    class DeleteDialog extends android.app.Dialog {
        TextView deleteInfoText;
        ButtonFlat deleteBtn, cancelBtn;

        public DeleteDialog(Context context) {
            super(context);
            setTheme(R.style.AppTheme);
            setTitle("delete");
            setContentView(R.layout.dialog_delete);

            deleteInfoText = (TextView)findViewById(R.id.deleteInfoText);
            deleteBtn = (ButtonFlat)findViewById(R.id.btnDelete);
            cancelBtn = (ButtonFlat)findViewById(R.id.btnCancel);
        }

        public TextView getDeleteInfoText() {
            return deleteInfoText;
        }

        public View getButtonDelete() {
            return deleteBtn;
        }

        public View getButtonCancel() {
            return cancelBtn;
        }
    }

    class RenameDialog extends android.app.Dialog {
        EditText originalNameText, newNameText;
        ButtonFlat renameBtn, cancelBtn;

        public RenameDialog(Context context) {
            super(context);
            setTheme(R.style.AppTheme);
            setTitle("rename");
            setContentView(R.layout.dialog_rename);
            originalNameText = (EditText)findViewById(R.id.editText1);
            newNameText = (EditText)findViewById(R.id.editText2);
            renameBtn = (ButtonFlat)findViewById(R.id.btnRename);
            cancelBtn = (ButtonFlat)findViewById(R.id.btnCancel);
        }

        public View getButtonAccept(){
            return renameBtn;
        }

        public View getButtonCancel(){
            return cancelBtn;
        }

        public EditText getOriginalNameText () {
            return originalNameText;
        }

        public EditText getNewNameText () {
            return newNameText;
        }
    }

    class NewFileOrFolderDialog extends android.app.Dialog {
        EditText newFileNameText;
        ButtonFlat cancelBtn, createBtn;

        public NewFileOrFolderDialog(Context context) {
            super(context);
            setTheme(R.style.AppTheme);
            setContentView(R.layout.dialog_newfileorfolder);

            newFileNameText = (EditText)findViewById(R.id.editText);
            cancelBtn = (ButtonFlat)findViewById(R.id.btnCancel);
            createBtn = (ButtonFlat)findViewById(R.id.btnCreate);
        }

        public ButtonFlat getCancelBtn() {
            return cancelBtn;
        }

        public ButtonFlat getCreateBtn() {
            return createBtn;
        }

        public EditText getNewFileNameText() {
            return newFileNameText;
        }
    }

    class FileHandler implements FileUtil.IFileUtil {
        boolean overrideForAll = false;

        @Override
        public boolean onDestiationFileExist(File srcFile, File dstFile) {
            if(overrideForAll){
                return true;
            }
            FileExistsConfirmDialog dialog = new FileExistsConfirmDialog(MainActivity.this, srcFile, dstFile);
            int operation = dialog.showDialog();
            if(operation == FileExistsConfirmDialog.DIALOG_OVERRIDE) {
                Toast.makeText(MainActivity.this, "override", Toast.LENGTH_SHORT).show();
                return true;
            } else if(operation == FileExistsConfirmDialog.DIALOG_CANCEL) {
                Toast.makeText(MainActivity.this, "not override", Toast.LENGTH_SHORT).show();
                return false;
            } else if(operation == FileExistsConfirmDialog.DIALOG_OVERRIDE_FOR_ALL) {
                overrideForAll = true;
                return true;
            }
            return false;
        }
    }
}
