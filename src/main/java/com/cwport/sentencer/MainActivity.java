package com.cwport.sentencer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cwport.sentencer.data.DataException;
import com.cwport.sentencer.data.DataHelper;
import com.cwport.sentencer.data.DataManager;
import com.cwport.sentencer.data.DataProvider;
import com.cwport.sentencer.data.SourceType;
import com.cwport.sentencer.model.Lesson;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    static final String TAG = MainActivity.class.getSimpleName();
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;
    ArrayList<Lesson> lessonList;
    private DataManager dataManager;
    private ProgressDialog ringProgressDialog;
    private DataProvider assetDataProvider;
    private DataProvider internalDataProvider;
    LessonAdapter listAdapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataManager = ((SentencerApp)getApplicationContext()).getDataManager();

        listView = (ListView) findViewById(R.id.list_lessons);
        buildLessonList();
        // listening to single list item on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                startLesson(position);
            }
        });
    }

    private void startLesson(int lessonIndex) {
        // sending data to new activity
        Intent i = new Intent(getApplicationContext(), CardActivity.class);
        i.putExtra(DataHelper.EXTRA_LESSON_INDEX, lessonList.get(lessonIndex).getId());
        i.putExtra(DataHelper.EXTRA_LESSON_TITLE, lessonList.get(lessonIndex).getTitle());
        i.putExtra(DataHelper.EXTRA_LESSON_SOURCE, lessonList.get(lessonIndex).getSourceType().ordinal());
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_add_user_lesson:
                scanLessonCode();
                return true;
            case R.id.action_help:
                showHelp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            Log.i(TAG, "QR-code: " + scanResult.getContents());
            downloadLesson(scanResult.getContents());
            // Toast.makeText(this, scanResult.getContents(), Toast.LENGTH_LONG);
        } else {
            Log.e(TAG, "Scan error result code: " + resultCode);
            Toast.makeText(this, "No data scanned", Toast.LENGTH_LONG);
        }
    }

    private void buildLessonList() {
        lessonList = new ArrayList<Lesson>();
        try {
            if(assetDataProvider == null) {
                assetDataProvider = dataManager.getDataProvider(SourceType.ASSET, getApplicationContext());
            }
            if(internalDataProvider == null) {
                internalDataProvider = dataManager.getDataProvider(SourceType.INTERNAL, getApplicationContext());
            }
            if(lessonList.isEmpty()) {
                lessonList = internalDataProvider.getLessons();
                lessonList.addAll(assetDataProvider.getLessons());
            }

        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            Toast.makeText(getApplicationContext(), de.getMessage(), Toast.LENGTH_LONG).show();
        }
        listAdapter = new LessonAdapter(lessonList);
        listAdapter.notifyDataSetChanged();
        listView.setAdapter(listAdapter);
    }

    private void downloadLesson(String fileUrl) {
        new DownloadFileTask().execute(fileUrl);
    }

    private void scanLessonCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    private void showHelp() {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    /**
     * LessonAdapter is intended to provide a list of incorporated lessons from assets
     */
    private class LessonAdapter extends ArrayAdapter<Lesson> {
//        private LayoutInflater layoutInflater;
//        private Context mainContext;
        private ArrayList<Lesson> lessons;
        public LessonAdapter(ArrayList<Lesson> list) {
            super(MainActivity.this, R.layout.list_item, list);
            lessons = list;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            final Lesson lesson = lessons.get(position);
            if (convertView == null) {
                row = getLayoutInflater().inflate(R.layout.list_item_lesson, null);
            } else {
                row = convertView;
            }
            ImageView icon = (ImageView) row.findViewById(R.id.icon);
            if(lesson.getSourceType() == SourceType.INTERNAL) {
                icon.setImageResource(R.drawable.ic_action_person);
            } else {
                icon.setImageResource(R.drawable.ic_action_attachment);
            }
            TextView title = (TextView)row.findViewById(R.id.lesson_title);
            title.setText(lesson.getTitle());

            TextView meta = (TextView)row.findViewById(R.id.lesson_meta);

            meta.setText(lesson.getDescription() + " ("
                    + MainActivity.this.getString(R.string.label_cards) +
                    ": " + lesson.getCardCount() + ")");

            return row;
        }
    }

    private class DownloadFileTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            ringProgressDialog = ProgressDialog.show(MainActivity.this,
                    MainActivity.this.getString(R.string.please_wait),
                    MainActivity.this.getString(R.string.download_lesson_file), true);
            ringProgressDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            InputStream inputStream;
            String fileName = null;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet();
                URI uriFile = new URI(url);
                httpGet.setURI(uriFile);
                HttpResponse response = httpClient.execute(httpGet);
                inputStream = response.getEntity().getContent();
                byte[] buffer = new byte[1024];
                while (true) {
                    int r = inputStream.read(buffer);
                    if (r == -1) break;
                    outputStream.write(buffer, 0, r);
                }
                byte[] data = outputStream.toByteArray();
                FileOutputStream fileOutputStream;
                fileName = DataHelper.userLessonFileName(uriFile);
                if(SentencerApp.DEBUG) {
                    Log.i(TAG, "Create file " + getFilesDir() + "/" + fileName);
                }
                fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                fileOutputStream.write(data);
                fileOutputStream.close();
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return fileName;
        }

        @Override
        protected void onPostExecute(String internalFile) {
            if(ringProgressDialog.isShowing()) ringProgressDialog.dismiss();
            buildLessonList();
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "AsyncTask Download File has been cancelled.");
        }

    }
}
