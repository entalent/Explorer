package cn.edu.bit.cs.explorer;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.gc.materialdesign.views.ProgressBarIndeterminate;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.bit.cs.explorer.ui.customview.FileListItem;
import cn.edu.bit.cs.explorer.util.FileUtil;

public class SearchActivity extends BaseActivity {

    private static ExecutorService SINGLE_TASK_EXECUTOR;

    static {
        SINGLE_TASK_EXECUTOR = (ExecutorService) Executors.newSingleThreadExecutor();
    };

    EditText editText;
    View searchView;
    ListView listView;
    ProgressBarIndeterminate progressBar;
    Toolbar toolbar;

    File currentDir;
    String keyWord;

    ArrayList<File> searchResult = new ArrayList<>();

    SearchTask searchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_search_content);


        currentDir = (File) getIntent().getSerializableExtra("path");

        editText = (EditText)findViewById(R.id.editText);
        searchView = findViewById(R.id.searchView);
        listView = (ListView)findViewById(R.id.listView);
        progressBar = (ProgressBarIndeterminate)findViewById(R.id.progressBar);
        toolbar = (Toolbar)findViewById(R.id.toolbar);

        setTitle("search in " + currentDir.getName());


        progressBar.setVisibility(View.INVISIBLE);

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyWord = editText.getText().toString();
                if (keyWord.isEmpty()) {
                    editText.setError("keyword should not be empty");
                } else {
                    searchTask = new SearchTask();
                    searchTask.executeOnExecutor(SINGLE_TASK_EXECUTOR);
                }
                //TODO: cancel task in onDestroy() ?
            }
        });

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return searchResult.size();
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
                FileListItem item = (FileListItem) convertView;
                if (item == null)
                    item = new FileListItem(SearchActivity.this, null);
                //item.setDetailType(FileListItem.TYPE_PATH);
                item.setFile(searchResult.get(position));
                item.getCheckBox().setVisibility(View.GONE);

                return item;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileUtil.openFile(SearchActivity.this, searchResult.get(position));
            }
        });

        TextView emptyView = new TextView(SearchActivity.this);
        emptyView.setText("no file to show");
        listView.setEmptyView(emptyView);

        /**/
    }

    class SearchTask extends AsyncTask<File, ArrayList<File>, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(ArrayList<File>... values) {
            super.onProgressUpdate(values);
            //modifying searchResult directly may cause exception
            searchResult.clear();
            searchResult.addAll(values[0]);
            ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
        }

        @Override
        protected Integer doInBackground(File... params) {
            Queue<File> searchQueue = new LinkedList<File>();
            ArrayList<File> searchResult = new ArrayList<>();
            searchQueue.add(currentDir);
            while(!searchQueue.isEmpty()) {
                if(isCancelled()) {
                    return 0;
                }
                File dir = searchQueue.poll();
                File[] files = dir.listFiles();
                if(files == null){
                    System.out.println("null");
                    continue;
                }
                for(File i : files) {
                    if(judgeFile(i)) {
                        searchResult.add(i);
                        publishProgress(searchResult);
                    }
                    if (i.isDirectory()) {
                        searchQueue.add(i);
                    }
                }
            }
            return searchResult.size();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    };

    boolean judgeFile(File file) {
        return (file.getName().contains(keyWord));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchTask.cancel(true);
    }
}
