package cn.edu.bit.cs.explorer.ui.dialog;

import android.content.Context;
import android.widget.RadioGroup;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.ui.fragment.BaseFileListFragment;

/**
 * Created by entalent on 2015/12/5.
 */
public class SortOrderDialog extends BlockingDialog {

    RadioGroup radioGroup;

    public SortOrderDialog(Context context) {
        super(context, "", "");

    }

    @Override
    public void onCreate() {
        setContentView(R.layout.dialog_sort_order);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int res = 0;
                switch (checkedId) {
                    case R.id.sortByName:
                        endDialog(BaseFileListFragment.SORT_BY_NAME);
                        break;
                    case R.id.sortBySize:
                        endDialog(BaseFileListFragment.SORT_BY_SIZE);
                        break;
                    case R.id.sortByLastModified:
                        endDialog(BaseFileListFragment.SORT_BY_LAST_MODIFIED);
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
