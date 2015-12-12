package cn.edu.bit.cs.explorer.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFlat;

import cn.edu.bit.cs.explorer.R;

/**
 * Created by entalent on 2015/12/2.
 */
public class DeleteDialog extends android.app.Dialog {
    TextView deleteInfoText;
    ButtonFlat deleteBtn, cancelBtn;

    public DeleteDialog(Context context) {
        super(context);
        setTitle(context.getString(R.string.message_delete));
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