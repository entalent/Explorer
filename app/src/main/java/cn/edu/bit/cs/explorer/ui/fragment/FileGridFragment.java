package cn.edu.bit.cs.explorer.ui.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.ui.customview.FileListItem;

/**
 * Created by entalent on 2015/11/11.
 */
public class FileGridFragment extends BaseFileListFragment {
    @Override
    protected FileListItem getNewFileListItem() {
        return new FileListItem(getContext(), FileListItem.MODE_GRID_ITEM);
    }

    @Override
    protected void initUI(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_file_grid, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        Resources resources = getResources();
        gridView.setColumnWidth((int) resources.getDimension(R.dimen.grid_item_width));
        adapterView = gridView;
        adapterView.setAdapter(new FileListAdapter());
    }
}
