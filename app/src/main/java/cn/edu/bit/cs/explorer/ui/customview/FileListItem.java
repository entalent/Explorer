package cn.edu.bit.cs.explorer.ui.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.util.TextUtil;

/**
 * Created by entalent on 2015/11/11.
 */
public class FileListItem extends RelativeLayout {

    boolean isParentDirectory;
    File file;

    ImageView icon;
    TextView textName, textSize, textDetail;
    CheckBox checkBox;

    public FileListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_file_list_item, this);
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
                    textDetail.setText("");
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
                    //textDetail.setText(f.getAbsolutePath());
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
    }

    public void setIsParentDirectory(boolean isParentDirectory){
        this.isParentDirectory = isParentDirectory;
        if(isParentDirectory)
            System.out.println(this.file.getAbsolutePath());
        if(isParentDirectory && (!this.file.isDirectory())){
            throw new IllegalStateException("parent file should be a directory");
        }
        setFile(this.file);
    }

    public CheckBox getCheckBox(){
        return this.checkBox;
    }
}
