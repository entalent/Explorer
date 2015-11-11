package cn.edu.bit.cs.explorer.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.ui.customview.FileListItem;

/**
 * Created by entalent on 2015/11/11.
 */
public class FileListFragment extends BaseFileListFragment {

    @Override
    protected FileListItem getNewFileListItem() {
        return new FileListItem(getContext(), null);
    }

    @Override
    protected void initUI(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_file_list, container, false);
        adapterView = (AdapterView) rootView.findViewById(R.id.listView);
        adapterView.setAdapter(new FileListAdapter());
    }
}
