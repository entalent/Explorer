package cn.edu.bit.cs.explorer.ui.customview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.util.FileIconUtil;
import cn.edu.bit.cs.explorer.util.TextUtil;

/**
 * Created by entalent on 2015/11/11.
 */
public class FileListItem extends RelativeLayout {

    public static final int MODE_LIST_ITEM = 0x0,
                        MODE_GRID_ITEM = 0x1;

    int layoutMode = MODE_LIST_ITEM;

    boolean isParentDirectory;
    File file;

    ImageView icon;
    TextView textName, textSize, textDetail;
    CheckBox checkBox;

    public FileListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI();
    }

    public FileListItem(Context context, int mode) {
        super(context);
        layoutMode = mode;

        initUI();
    }

    void initUI() {
        if(layoutMode != MODE_LIST_ITEM && layoutMode != MODE_GRID_ITEM)
            throw new IllegalArgumentException("mode should be MODE_LIST_ITEM or MODE_GRID_ITEM");
        if(layoutMode == MODE_LIST_ITEM)
            LayoutInflater.from(getContext()).inflate(R.layout.view_file_list_item, this);
        else if(layoutMode == MODE_GRID_ITEM)
            LayoutInflater.from(getContext()).inflate(R.layout.view_file_grid_item, this);

        icon = (ImageView) findViewById(R.id.imageView);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        textName = (TextView)findViewById(R.id.text_name);
        textSize = (TextView)findViewById(R.id.text_size);
        textDetail = (TextView)findViewById(R.id.text_detail);
    }

    public void setFile(File f) {
        this.file = f.getAbsoluteFile();
        if(this.file == null)
            return ;
        if(f.isDirectory()){
            if(isParentDirectory){
                if(textName != null){
                    textName.setText("..");
                }
                if(textSize != null){
                    textSize.setText("");
                }
                if(textDetail != null){
                    textDetail.setText(getResources().getString(R.string.file_list_item_parent_folder));
                }
                checkBox.setVisibility(View.INVISIBLE);
            } else {
                if (textName != null) {
                    textName.setText(f.getName());
                }
                if(textSize != null){
                    textSize.setText("");
                }
                if (textDetail != null) {
                    textDetail.setText(TextUtil.formatTimeStr(f.lastModified()));
                }
                checkBox.setVisibility(View.VISIBLE);
            }

        } else {
            if(textName != null){
                textName.setText(f.getName());
            }
            if(textSize != null){
                textSize.setText(TextUtil.formatSizeStr(f.length()));
            }
            if(textDetail != null){
                //textDetail.setText(f.getAbsolutePath());
                textDetail.setText(TextUtil.formatTimeStr(f.lastModified()));
            }
            checkBox.setVisibility(View.VISIBLE);
        }
        setIcon();
    }

    public void setIsParentDirectory(boolean isParentDirectory){
        this.isParentDirectory = isParentDirectory;
        if(isParentDirectory && (!this.file.isDirectory())){
            throw new IllegalStateException("parent file should be a directory");
        }
        setFile(this.file);
    }

    public CheckBox getCheckBox(){
        return this.checkBox;
    }

    private void setIcon() {
        Resources resources = getContext().getResources();
        Drawable iconDrawable = resources.getDrawable(R.drawable.ic_insert_drive_file_black_24dp);
        if(file.isDirectory()){
            iconDrawable = resources.getDrawable(R.drawable.ic_folder_black_36dp);
        } else {
            String mimeType = TextUtil.getMimeTypeFromFile(file);
            if(mimeType == null){
                iconDrawable = resources.getDrawable(R.drawable.ic_insert_drive_file_black_24dp);
            } else if(mimeType.contains("audio")){ //audio
                //TODO: Album Artwork?
                iconDrawable = resources.getDrawable(R.drawable.ic_library_music_black_36dp);
            } else if(mimeType.contains("video")){ //video
                //TODO: Video thumbnail?
                iconDrawable = resources.getDrawable(R.drawable.ic_movie_black_36dp);
            } else if(mimeType.contains("vnd.android.package-archive")){ //apk
                iconDrawable = FileIconUtil.getApkIcon(getContext(), file.getAbsolutePath());
            } else if(mimeType.contains("image")){
                //TODO: Image thumbnail?
            } else {
                iconDrawable = resources.getDrawable(R.drawable.ic_insert_drive_file_black_24dp);
            }
        }
        icon.setImageDrawable(iconDrawable);
    }

}
