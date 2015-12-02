package cn.edu.bit.cs.explorer.ui.dialog;

import android.content.Context;
import android.view.View;

import java.io.File;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.ui.customview.FileListItem;

/**
 * Created by entalent on 2015/12/2.
 */
public class FileExistsConfirmDialog extends BlockingDialog {
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