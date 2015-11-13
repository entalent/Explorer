package cn.edu.bit.cs.explorer.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import cn.edu.bit.cs.explorer.ui.customview.FileListItem;

/**
 * Created by entalent on 2015/11/11.
 */
public abstract class BaseFileListFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    View rootView;

    File rootPath, currentPath;
    boolean isInRootDirectory = true;

    ArrayList<File> filesInCurrentDir = new ArrayList<>();

    ArrayList<File> selectedFiles = new ArrayList<>();

    protected AdapterView adapterView;

    FileListListener fileListListener;

    public class FileListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return filesInCurrentDir.size();
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
            FileListItem item = /*(FileListItem)convertView;
            if(item == null)
                item =*/ getNewFileListItem();
            File currentFile = filesInCurrentDir.get(position);
            item.setFile(currentFile);
            Log.e("TAG", position + " " + currentFile.getAbsolutePath());
            item.setIsParentDirectory(position == 0 && (!isInRootDirectory));
            item.setOnClickListener(BaseFileListFragment.this);
            item.getCheckBox().setOnCheckedChangeListener(BaseFileListFragment.this);
            item.getCheckBox().setTag(new Integer(position));
            item.getCheckBox().setChecked(selectedFiles.contains(currentFile));
            return item;
        }
    }

    public interface FileListListener {
        public void OnOpenFileOrDirectory(File file);
        public void OnSelectedFileChange(ArrayList<File> selectedFiles);
    }


    protected abstract FileListItem getNewFileListItem();

    protected abstract void initUI(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //rootView = super.onCreateView(inflater, container, savedInstanceState);
        initUI(inflater, container, savedInstanceState);
        return rootView;
    }

    public void setRootDirectory (File rootDir){
        if((!rootDir.exists()) || (!rootDir.isDirectory())){
            throw new RuntimeException("root path should be an existing directory");
        }
        this.rootPath = rootDir;
        this.setCurrentDirectory(rootPath);
    }

    public File getRootDirectory(){
        return this.rootPath;
    }

    public void setCurrentDirectory (String absolutePath) {
        if(rootPath == null)
            throw new RuntimeException("root path not specified");
        if(!absolutePath.startsWith(rootPath.getAbsolutePath()))
            throw new IllegalArgumentException("current directory is not child directory of root directory");
        setCurrentDirectory(new File(absolutePath));
    }

    protected void setCurrentDirectory (final File currentDirectory){
        adapterView.setSelection(0);
        if((!currentDirectory.isDirectory()) || (!currentDirectory.exists())){
            this.currentPath = rootPath;
        } else {
            this.currentPath = currentDirectory;
        }

        selectedFiles.clear();
        refreshFileList();

        filesInCurrentDir.clear();
        File[] files = currentPath.listFiles();
        Arrays.sort(files);
        Log.e("TAG", currentPath + " " + rootPath + " " + currentPath.equals(rootPath));
        if(currentPath.equals(rootPath)){
            isInRootDirectory = true;
        } else {
            isInRootDirectory = false;
            filesInCurrentDir.add(currentPath.getParentFile());
        }
        for(File i : files){
            filesInCurrentDir.add(i);
        }

        refreshFileList();
    }

    public void setFilesToShow(ArrayList<File> files){
        filesInCurrentDir = files;
        refreshFileList();
    }

    public void refreshFileList(){
        final FileListAdapter adapter = (FileListAdapter) adapterView.getAdapter();
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
        adapterView.setSelection(0);
        adapterView.scrollTo(0, 0);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int index = (Integer) buttonView.getTag();
        File file = filesInCurrentDir.get(index);
        if(isChecked && (!selectedFiles.contains(file))) {
            selectedFiles.add(file);
        } else if(!isChecked) {
            selectedFiles.remove(file);
        } else {
            return ;
        }
        if(fileListListener != null)
            fileListListener.OnSelectedFileChange(selectedFiles);
    }

    @Override
    public void onClick(View v) {
        FileListItem item = (FileListItem)v;
        int index = (int) item.getCheckBox().getTag();
        Log.e("TAG", "onclick " + index);
        File currentFile = filesInCurrentDir.get(index);
        openFileOrDirectory(currentFile);
    }

    public void openParentDirectory() {
        if(!isInRootDirectory){
            openFileOrDirectory(currentPath.getParentFile());
        }
    }

    public void selectAll() {
        selectedFiles.addAll(filesInCurrentDir);
        if(!isInRootDirectory){
            selectedFiles.remove(0);
        }
        notifySelectionChanged();
    }

    public void deselectAll() {
        selectedFiles.clear();
        notifySelectionChanged();
    }

    void notifySelectionChanged(){
        if(fileListListener != null)
            fileListListener.OnSelectedFileChange(selectedFiles);
    }

    protected void openFileOrDirectory(File currentFile) {
        currentFile = currentFile.getAbsoluteFile();
        if(currentFile.isDirectory()) {
            this.setCurrentDirectory(currentFile);
        }
        if(fileListListener != null)
            fileListListener.OnOpenFileOrDirectory(currentFile);
    }

    public void setFileListListener(FileListListener listener){
        this.fileListListener = listener;
    }
}
