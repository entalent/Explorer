package cn.edu.bit.cs.explorer.tasks;

import android.content.Context;
import android.view.View;

import com.gc.materialdesign.widgets.Dialog;

import java.io.File;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.util.ZipUtil;

/**
 * Created by entalent on 2015/12/2.
 */
public class ZipTask extends FileAsyncTask {
    File targetZipFile;

    public ZipTask(Context context, File operationDirectory, ArrayList<File> filesToOperate, File targetZipFile) {
        super(context, operationDirectory, filesToOperate);
        this.targetZipFile = targetZipFile;
    }

    @Override
    protected Integer doInBackground(String... params) {
        try {
            return ZipUtil.zip(filesToOperate, targetZipFile);
        } catch (Exception e) {
            e.printStackTrace();
            Dialog dialog = new Dialog(context, service.getString(R.string.error), service.getString(R.string.compress_error));
            dialog.show();
            dialog.show();
            return 0;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        final Dialog dialog = new Dialog(context, service.getString(R.string.compress_finish), service.getString(R.string.message_compress_finish));
        dialog.addCancelButton(service.getString(android.R.string.no));
        dialog.show();
        dialog.getButtonAccept().setText(service.getString(android.R.string.yes));
        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service.sendGotoDirectory(targetZipFile.getParentFile());
                dialog.dismiss();
            }
        });


    }

    public File getZipFile() {
        return targetZipFile;
    }
}
