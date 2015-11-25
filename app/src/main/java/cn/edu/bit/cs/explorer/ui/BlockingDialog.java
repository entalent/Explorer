package cn.edu.bit.cs.explorer.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.gc.materialdesign.views.ButtonFlat;


import java.io.File;

/**
 * Created by entalent on 2015/11/25.
 */
public abstract class BlockingDialog extends Dialog {
    public static final int DIALOG_OK = 0x0;
    public static final int DIALOG_CANCEL = 0x1;

    int dialogResult;
    Handler mHandler;

    public BlockingDialog(Context context, String title, String message)
    {
        super(context);
        onCreate();
    }

    public int getDialogResult()
    {
        return dialogResult;
    }

    public void setDialogResult(int dialogResult)
    {
        this.dialogResult = dialogResult;
    }
    /** Called when the activity is first created. */

    public abstract void onCreate();


    public void endDialog(int result)
    {
        dismiss();
        setDialogResult(result);
        Message m = mHandler.obtainMessage();
        mHandler.sendMessage(m);
    }

    public int showDialog()
    {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                // process incoming messages here
                //super.handleMessage(msg);
                throw new RuntimeException();
            }
        };
        super.show();
        try {
            Looper.getMainLooper().loop();
        }
        catch(RuntimeException e2)
        {
        }
        return dialogResult;
    }


}
