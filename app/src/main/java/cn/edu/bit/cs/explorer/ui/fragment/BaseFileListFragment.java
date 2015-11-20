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

import java.io.File;
import java.util.ArrayList;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.ui.customview.FileListItem;
import cn.edu.bit.cs.explorer.util.FileUtil;

/**
 * Created by entalent on 2015/11/11.
 */
public class BaseFileListFragment extends Fragment
    implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    View rootView;
    AdapterView adapterView;

    File rootDir, currentDir;
    ArrayList<File> filesInCurrentDir = new ArrayList<>();
    ArrayList<File> selectedFiles = new ArrayList<>();
    boolean isInRootDir;

    FileListListener listener;

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
            if(!isInRootDir){
                if(position == 0){
                    item.setFile(currentDir.getParentFile());
                    item.setIsParentDirectory(true);
                } else {
                    item.setFile(filesInCurrentDir.get(position - 1));
                }
            } else {
                item.setFile(filesInCurrentDir.get(position));
            }
            item.getCheckBox().setOnCheckedChangeListener(BaseFileListFragment.this);
            item.setTag(new Integer(position));
            item.getCheckBox().setTag(new Integer(position));
            item.setOnClickListener(BaseFileListFragment.this);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_file_list, container, false);
        adapterView = (ListView)rootView.findViewById(R.id.listView);
        adapterView.setAdapter(new FileListAdapter());
        return rootView;
    }

    private void refreshView() {
        selectedFiles.clear();
        invokeOnSelectedFilesChange();
        ((BaseAdapter)adapterView.getAdapter()).notifyDataSetChanged();
    }

    public void setRootDir(File rootDir){
        this.rootDir = rootDir;
        setCurrentDir(rootDir);
    }

    public void setCurrentDir(File currentDir){
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
        File[] files = currentDir.listFiles();
        for(File i : files){
            filesInCurrentDir.add(i);
        }
        refreshView();
    }

    public void setFileListListener(FileListListener listener){
        this.listener = listener;
    }

    public void openFileOrDirectory(File file) {
        invokeOnOpenFileOrDirectory(file);
        if(file.isDirectory()){
            setCurrentDir(file);
        } else {

        }
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

    public interface FileListListener {
        void onOpenFileOrDirectory(File file);
        void onSelectedFilesChange(ArrayList<File> files);
    }
}
