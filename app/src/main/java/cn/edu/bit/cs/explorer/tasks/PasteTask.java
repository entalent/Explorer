package cn.edu.bit.cs.explorer.tasks;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.MainActivity;
import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.util.FileUtil;
import cn.edu.bit.cs.explorer.ui.dialog.FileExistsConfirmDialog;

/**
 * Created by entalent on 2015/12/2.
 */
public class PasteTask extends FileAsyncTask {
    int pasteOperation;

    public PasteTask(Context context, File operationDir, ArrayList<File> filesToOperate, int pasteOperation){
        super(context, operationDir, filesToOperate);
        this.pasteOperation = pasteOperation;
    }

    @Override
    protected void onPostExecute(Integer aInteger) {
        super.onPostExecute(aInteger);
        Toast.makeText(context, String.format(service.getString(R.string.message_pasted_files), completedFileCnt), Toast.LENGTH_SHORT).show();
        service.sendRefreshDirectory(operationDirectory);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
        FileHandler handler = new FileHandler();
        completedFileCnt = 0;
        for (File i : filesToOperate) {
            try {
                if(pasteOperation == MainActivity.ACTION_COPY)
                    completedFileCnt += FileUtil.copyFile(i, operationDirectory, handler);
                else if(pasteOperation == MainActivity.ACTION_CUT)
                    completedFileCnt += FileUtil.cutFile(i, operationDirectory, handler);
            } catch (IllegalArgumentException e) {
                return completedFileCnt;
            }
        }
        return completedFileCnt;
    }

    public class FileHandler implements FileUtil.IFileUtil {
        boolean overrideForAll = false;

        @Override
        public boolean onDestiationFileExist(File srcFile, File dstFile) {
            if(overrideForAll){
                return true;
            }
            FileExistsConfirmDialog dialog = new FileExistsConfirmDialog(context, srcFile, dstFile);
            int operation = dialog.showDialog();
            if(operation == FileExistsConfirmDialog.DIALOG_OVERRIDE) {
                return true;
            } else if(operation == FileExistsConfirmDialog.DIALOG_CANCEL) {
                return false;
            } else if(operation == FileExistsConfirmDialog.DIALOG_OVERRIDE_FOR_ALL) {
                overrideForAll = true;
                return true;
            }
            return false;
        }
    }
}
