package com.cwport.sentencer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cwport.sentencer.data.DataHelper;
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
import android.support.v4.app.FragmentManager;

public class LessonsActivity extends FragmentActivity {

    static final String TAG = LessonsActivity.class.getSimpleName();
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;
    FragmentTabHost tabHost;
    final String TAB_ASSET_LESSON = "AssetLessonTab";
    final String TAB_USER_LESSON = "UserLessonTab";

    private ProgressDialog ringProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);
        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.tabcontent);

        tabHost.addTab(tabHost.newTabSpec(TAB_ASSET_LESSON).setIndicator(getResources().getString(R.string.tab_asset_lessons),
                        getResources().getDrawable(R.drawable.ic_action_attachment)),
                        AssetLessonFragment.class, null);

        tabHost.addTab(tabHost.newTabSpec(TAB_USER_LESSON).setIndicator(getResources().getString(R.string.tab_my_lessons),
                        getResources().getDrawable(R.drawable.ic_action_person)),
                UserLessonFragment.class, null
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        } else {
            Log.e(TAG, "Scan error result code: " + resultCode);
            Toast.makeText(this, "No data scanned", Toast.LENGTH_LONG).show();
        }
    }

    private void downloadLesson(String fileUrl) {
        new DownloadFileTask().execute(fileUrl);
    }

    private void scanLessonCode() {
        if(CommonUtils.isConnectedOrConnectingToNetwork(getApplicationContext())) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
        } else {
            Toast.makeText(getApplicationContext(), R.string.msg_no_internet_connection,
                    Toast.LENGTH_LONG).show();
        }

    }

    private void showHelp() {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    private void updateUserLessons() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentByTag(TAB_USER_LESSON);
        if(frag instanceof UserLessonFragment) {
            ((UserLessonFragment) frag).refreshList();
        }
    }

    private class DownloadFileTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            ringProgressDialog = ProgressDialog.show(LessonsActivity.this,
                    LessonsActivity.this.getString(R.string.please_wait),
                    LessonsActivity.this.getString(R.string.download_lesson_file), true);
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
            updateUserLessons();
            tabHost.setCurrentTabByTag(TAB_USER_LESSON);
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "AsyncTask Download File has been cancelled.");
        }

    }
}
