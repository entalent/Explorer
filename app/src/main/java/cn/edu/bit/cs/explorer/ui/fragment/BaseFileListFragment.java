package cn.edu.bit.cs.explorer.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.ui.customview.FileListItem;
import cn.edu.bit.cs.explorer.util.BaseFileComparator;
import cn.edu.bit.cs.explorer.util.FileComparators;
import cn.edu.bit.cs.explorer.util.FileUtil;

/**
 * Created by entalent on 2015/11/11.
 */
public class BaseFileListFragment extends Fragment
    implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static final int SORT_BY_NAME = 0x0,
                    SORT_BY_SIZE = 0x1,
                    SORT_BY_LAST_MODIFIED = 0x2;

    View rootView;
    AdapterView adapterView;

    File rootDir, currentDir;
    ArrayList<File> filesInCurrentDir = new ArrayList<>();
    ArrayList<File> selectedFiles = new ArrayList<>();
    boolean isInRootDir;

    FileListListener listener;

    int currentSortMethod = SORT_BY_NAME;
    BaseFileComparator comparator = new FileComparators.NameComparator();

    public class FileListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(isInRootDir)
                return filesInCurrentDir.size();
            else
                return filesInCurrentDir.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            FileListItem item = (FileListItem)convertView;
            if(item == null){
                item = new FileListItem(getContext(), null);
            }
            item.getCheckBox().setOnCheckedChangeListener(null);
            if(!isInRootDir){
                if(position == 0){
                    item.setFile(currentDir.getParentFile());
                    item.setIsParentDirectory(true);
                } else {
                    File currentFile = filesInCurrentDir.get(position - 1);
                    item.setFile(currentFile);
                    item.getCheckBox().setChecked(isSelectedFile(currentFile));
                }
            } else {
                File currentFile = filesInCurrentDir.get(position);
                item.setFile(currentFile);
                item.getCheckBox().setChecked(isSelectedFile(currentFile));
            }
            item.setTag(new Integer(position));
            item.getCheckBox().setTag(new Integer(position));
            item.setOnClickListener(BaseFileListFragment.this);
            item.getCheckBox().setOnCheckedChangeListener(BaseFileListFragment.this);
            return item;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Integer position = (Integer)(buttonView.getTag());

        if(!isInRootDir)
            position--;
        if(isChecked)
            selectedFiles.add(filesInCurrentDir.get(position));
        else
            selectedFiles.remove(filesInCurrentDir.get(position));
        invokeOnSelectedFilesChange();
    }

    @Override
    public void onClick(View view) {
        int position = (Integer)view.getTag();
        if(!isInRootDir){
            if(position == 0){
                openFileOrDirectory(currentDir.getParentFile());
            } else {
                position--;
                openFileOrDirectory(filesInCurrentDir.get(position));
            }
        } else {
            openFileOrDirectory(filesInCurrentDir.get(position));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_file_list, container, false);
        adapterView = (ListView)rootView.findViewById(R.id.listView);
        adapterView.setAdapter(new FileListAdapter());
        TextView emptyView = new TextView(getContext());
        emptyView.setText("no file or folder in this volume");
        adapterView.setEmptyView(emptyView);
        return rootView;
    }

    private void refreshView() {
        ((BaseAdapter)adapterView.getAdapter()).notifyDataSetChanged();
    }

    public void refreshCurrentSelected() {
        refreshView();
    }

    public void refreshCurrentContent() {
        setCurrentDir(currentDir);
    }

    public void setRootDir(File rootDir){
        this.rootDir = rootDir;
        setCurrentDir(rootDir);
    }

    public void setCurrentDir(File currentDir) {
        if(this.rootDir == null){
            throw new IllegalStateException("root directory not set");
        }
        if(currentDir == null || !currentDir.exists() || !currentDir.isDirectory()) {
            throw new IllegalArgumentException("current directory should be an existing directory");
        }
        if(!FileUtil.isChildDirectoty(rootDir, currentDir)){
            throw new IllegalArgumentException("current directory should be the child directory of root");
        }
        this.currentDir = currentDir;
        this.isInRootDir = currentDir.getAbsolutePath().equals(rootDir.getAbsolutePath());

        filesInCurrentDir.clear();
        selectedFiles.clear();
        invokeOnSelectedFilesChange();
        File[] files = currentDir.listFiles();
        if(files != null) {
            for (File i : files) {
                filesInCurrentDir.add(i);
            }
            //TODO: changeable comparator
            Collections.sort(filesInCurrentDir, comparator);
        }
        ((BaseAdapter)adapterView.getAdapter()).notifyDataSetChanged();
        adapterView.setSelection(0);
    }

    public void setFileListListener(FileListListener listener){
        this.listener = listener;
    }

    public void setSortMethod(int sortMethod) {
        if(sortMethod == this.currentSortMethod) {
            comparator.setReversed(!comparator.isReversed());
        } else {
            this.currentSortMethod = sortMethod;
            switch (sortMethod) {
                case SORT_BY_NAME:
                    comparator = new FileComparators.NameComparator();
                    break;
                case SORT_BY_SIZE:
                    comparator = new FileComparators.SizeComparator();
                    break;
                case SORT_BY_LAST_MODIFIED:
                    comparator = new FileComparators.ModifyDateComparator();
                    break;
            }
        }
        refreshCurrentContent();
    }

    public void openFileOrDirectory(File file) {
        invokeOnOpenFileOrDirectory(file);
        if(file.isDirectory()){
            setCurrentDir(file);
        } else {

        }
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public boolean isSelectedFile(File file) {
        for(File i : selectedFiles){
            if(i.getAbsolutePath().equals(file.getAbsolutePath()))
                return true;
        }
        return false;
    }

    public void invokeOnOpenFileOrDirectory(File file) {
        if(listener != null){
            try{
                listener.onOpenFileOrDirectory(file);
            } catch (Throwable t) {

            }
        }
    }

    public void invokeOnSelectedFilesChange() {
        if(listener != null){
            try{
                listener.onSelectedFilesChange(selectedFiles);
            } catch (Throwable t) {

            }
        }
    }

    public void selectAll() {
        selectedFiles.clear();
        selectedFiles.addAll(filesInCurrentDir);
        refreshCurrentSelected();
        invokeOnSelectedFilesChange();
    }

    public void deselectAll() {
        selectedFiles.clear();
        refreshCurrentSelected();
        invokeOnSelectedFilesChange();
    }

    public interface FileListListener {
        void onOpenFileOrDirectory(File file);
        void onSelectedFilesChange(ArrayList<File> files);
    }
}
