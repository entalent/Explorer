package cn.edu.bit.cs.explorer.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.gc.materialdesign.views.ButtonFlat;

import cn.edu.bit.cs.explorer.R;

/**
 * Created by entalent on 2015/12/2.
 */
public class RenameDialog extends android.app.Dialog {
    EditText originalNameText, newNameText;
    ButtonFlat renameBtn, cancelBtn;

    public RenameDialog(Context context) {
        super(context);
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