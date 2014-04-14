package com.cwport.sentencer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.cwport.sentencer.data.DataException;
import com.cwport.sentencer.data.DataHelper;
import com.cwport.sentencer.data.DataManager;
import com.cwport.sentencer.data.FileDataProvider;
import com.cwport.sentencer.model.Lesson;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    static final String TAG = MainActivity.class.getSimpleName();
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;
    ArrayList<Lesson> lessonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ListView listView = (ListView) findViewById(R.id.list_lessons);
        lessonList = new ArrayList<Lesson>();
        try {
            DataManager dataManager = DataManager.getInstance();
            ((FileDataProvider)dataManager.getDataProvider()).setContext(this);
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

    private void startLesson(String lesson, int lessonIndex, int mode) {
        Intent i = new Intent(getApplicationContext(), CardActivity.class);
        // sending data to new activity
        i.putExtra(DataHelper.EXTRA_LESSON_INDEX, lessonIndex);
        i.putExtra(DataHelper.EXTRA_LESSON_TITLE, lesson);
        i.putExtra(DataHelper.EXTRA_LESSON_MODE, mode);
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
//            case R.id.action_settings:
//                showSettings();
//                return true;
            case R.id.action_help:
                showHelp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelp() {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    public class LessonAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;
        private Context mainContext;

        public LessonAdapter(Context ctx) {
            mainContext = ctx;
            layoutInflater = LayoutInflater.from(ctx);
        }

        public int getCount() {
            return lessonList.size();
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
}
