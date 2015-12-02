package cn.edu.bit.cs.explorer.util.tasks;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.util.FileUtil;

/**
 * Created by entalent on 2015/12/2.
 */
public class DeleteTask extends FileAsyncTask {

    public DeleteTask(Context context, File operationDirectory, ArrayList<File> filesToOperate) {
        super(context, operationDirectory, filesToOperate);
    }

    @Override
    protected void onPostExecute(Integer aInteger) {
        super.onPostExecute(aInteger);
        Toast.makeText(context, "deleted " + completedFileCnt + " file(s)", Toast.LENGTH_SHORT).show();
        service.sendRefreshDirectory(operationDirectory);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
        //Looper.prepare();
        completedFileCnt = 0;
        for (File i : filesToOperate) {
            completedFileCnt += FileUtil.deleteFile(i);
        }

        return completedFileCnt;
    }
}
