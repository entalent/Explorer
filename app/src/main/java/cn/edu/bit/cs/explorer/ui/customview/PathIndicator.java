package cn.edu.bit.cs.explorer.ui.customview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.LinkAddress;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;

import cn.edu.bit.cs.explorer.R;

/**
 * Created by entalent on 2015/11/11.
 */
public class PathIndicator extends LinearLayout implements View.OnClickListener{
    LinearLayout indicator;

    File rootDir, currentDir;

    OnPathChangeListener listener;

    HorizontalScrollView scroll;

    public PathIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_path_indicator, this);
        indicator = (LinearLayout)findViewById(R.id.linearLayout);
        scroll = (HorizontalScrollView)findViewById(R.id.horizontalScrollView);
    }

    public void setRootDir(File rootDir) {
        this.rootDir = rootDir;
        currentDir = rootDir;
        setCurrentDir(currentDir);
    }

    public void setCurrentDir (File currentDir) {
        this.currentDir = currentDir;
        if(rootDir == null)
            throw new RuntimeException("root path not specified");
        if(!currentDir.getAbsolutePath().startsWith(rootDir.getAbsolutePath())){
            throw new IllegalArgumentException("current directory is not child directory of root directory");
        }
        String[] folders = (currentDir.getAbsolutePath().
                substring(rootDir.getAbsolutePath().length())).
                split("[" + File.separator + "]");

        indicator.removeAllViews();


        TextView textRoot = new TextView(getContext());
        textRoot.setText(rootDir.getName());
        textRoot.setTag(rootDir.getAbsolutePath());
        setTextViewParameter(textRoot);
        indicator.addView(textRoot);
        textRoot.setOnClickListener(this);

        String path = rootDir.getAbsolutePath();
        Resources resources = getContext().getResources();
        for(String folder : folders){
            if(folder.length() == 0)
                continue;
            ImageView imgSeparator = new ImageView(getContext());
            imgSeparator.setImageDrawable(resources.getDrawable(R.drawable.ic_keyboard_arrow_right_black_18dp));
            indicator.addView(imgSeparator);
            TextView textFolder = new TextView(getContext());
            textFolder.setText(folder);
            setTextViewParameter(textFolder);
            path += (File.separator + folder);
            textFolder.setTag(path);
            textFolder.setOnClickListener(this);
            indicator.addView(textFolder);
        }
        scroll.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll.scrollTo(10000, Integer.MAX_VALUE);
            }
        }, 100);

    }

    void setTextViewParameter(TextView textView) {
        textView.setTextColor(Color.BLACK);
        textView.setPadding(10, 10, 10, 10);
    }
    
    @Override
    public void onClick(View v) {
        String path = (String) v.getTag();
        setCurrentDir(new File(path));
        if(listener != null){
            listener.onPathChange(currentDir);
        }
    }

    public void setOnPathChangeListener(OnPathChangeListener listener){
        this.listener = listener;
    }

    public interface OnPathChangeListener {
        void onPathChange(File file);
    }
}
