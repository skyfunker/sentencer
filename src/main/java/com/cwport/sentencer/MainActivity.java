package com.cwport.sentencer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cwport.sentencer.data.AssetDataProvider;
import com.cwport.sentencer.data.InternalDataProvider;
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
    ArrayList<Lesson> lessonList = new ArrayList<Lesson>();
    DataManager dataManager;
    private ProgressDialog ringProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ListView listView = (ListView) findViewById(R.id.list_lessons);
        lessonList = new ArrayList<Lesson>();
        try {
            dataManager = DataManager.getInstance();
            DataProvider dataProvider = dataManager.getDataProvider(SourceType.INTERNAL);
            if ((dataProvider instanceof AssetDataProvider)) {
                ((AssetDataProvider) dataProvider).setContext(this);
            }
            if(dataProvider instanceof InternalDataProvider) {
                ((InternalDataProvider) dataProvider).setContext(this);
            }
            lessonList = dataManager.getLessons();

        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            Toast.makeText(getApplicationContext(), de.getMessage(), Toast.LENGTH_LONG).show();
        }
        listView.setAdapter(new LessonAdapter(this));

        // listening to single list item on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                startLesson(position);
            }
        });
    }

    private void startLesson(int lessonIndex) {
        Intent i = new Intent(getApplicationContext(), CardActivity.class);
        // sending data to new activity
        i.putExtra(DataHelper.EXTRA_LESSON_INDEX, lessonIndex);
        i.putExtra(DataHelper.EXTRA_LESSON_TITLE, lessonList.get(lessonIndex).getTitle());
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
            case R.id.action_scan_code:
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
    public class LessonAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;
        private Context mainContext;

        public LessonAdapter(Context ctx) {
            mainContext = ctx;
            layoutInflater = LayoutInflater.from(ctx);
        }

        public int getCount() {
            int count = 0;
            if(lessonList != null) count = lessonList.size();
            return count;
        }

        public Object getItem (int position) {
            return position;
        }

        public long getItemId (int position) {
            return position;
        }

        public String getString (int position) {
            Lesson lesson = lessonList.get(position);
            return "Title:" + lesson.getTitle() + ";" + lesson.getFaceLocale() + " to " + lesson.getFaceLocale();
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = layoutInflater.inflate(R.layout.list_item, null);

            Lesson lesson = lessonList.get(position);
            TextView title = (TextView)convertView.findViewById(R.id.lesson_title);
            title.setText(lesson.getTitle());

            TextView meta = (TextView)convertView.findViewById(R.id.lesson_meta);
            meta.setText(lesson.getDescription() + " (" + mainContext.getString(R.string.label_cards) + ": " + lesson.getCardCount() + ")");

            return convertView;
        }
    }

    private class DownloadFileTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            ringProgressDialog = ProgressDialog.show(MainActivity.this,
                    "Please wait ...", "Downloading lesson file...", true);
            ringProgressDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            InputStream inputStream;
            String fileName = null;
            byte[] data = new byte[0];
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
                data = outputStream.toByteArray();
                FileOutputStream fileOutputStream;
                fileName = DataHelper.userLessonFileName(uriFile);
                Log.i(TAG, "Create file " + getFilesDir() + "/" + fileName);
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
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "AsyncTask Download File has been cancelled.");
        }

    }
}
