package cn.edu.bit.cs.explorer.ui.dialog;

import android.content.Context;
import android.widget.EditText;

import com.gc.materialdesign.views.ButtonFlat;

import cn.edu.bit.cs.explorer.R;

/**
 * Created by entalent on 2015/12/2.
 */
public class NewFileOrFolderDialog extends android.app.Dialog {
    EditText newFileNameText;
    ButtonFlat cancelBtn, createBtn;

    public NewFileOrFolderDialog(Context context) {
        super(context);
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