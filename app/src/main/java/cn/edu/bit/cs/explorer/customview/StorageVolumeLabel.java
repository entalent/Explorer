package cn.edu.bit.cs.explorer.customview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.edu.bit.cs.explorer.R;
import cn.edu.bit.cs.explorer.util.StorageUtil;
import cn.edu.bit.cs.explorer.util.StorageVolumeInfo;

/**
 * TODO: document your custom view class.
 */
public class StorageVolumeLabel extends LinearLayout {

    TextView textTitle;
    TextView textPath;
    TextView textSpace;
    ImageView img;
    StorageVolumeInfo info;

    public StorageVolumeLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_storage_volume_label, this);
        textTitle = (TextView)findViewById(R.id.text_title);
        textPath = (TextView)findViewById(R.id.text_path);
        textSpace = (TextView)findViewById(R.id.text_space);
        img = (ImageView)findViewById(R.id.imageView);
    }

    public void setVolumeInfo(StorageVolumeInfo info){
        this.info = info;
        Resources res = getContext().getResources();
        if(info.path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
            textTitle.setText(getContext().getString(R.string.storage_label_main));
            img.setImageDrawable(res.getDrawable(R.drawable.ic_storage_black_36dp));
        } else if(info.path.toLowerCase().contains("usb")){
            textTitle.setText(getContext().getString(R.string.storage_label_usb));
            img.setImageDrawable(res.getDrawable(R.drawable.ic_usb_black_36dp));
        } else {
            textTitle.setText(getContext().getString(R.string.storage_label_sdcard));
            img.setImageDrawable(res.getDrawable(R.drawable.ic_sd_storage_black_36dp));
        }
        textPath.setText(info.path);
        textSpace.setText(StorageUtil.formatSizeStr(info.availableBytes) +  "/" +
                StorageUtil.formatSizeStr(info.totalBytes));
    }

    public StorageVolumeInfo getVolumeInfo(){
        return info;
    }
}
