package com.cwport.sentencer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.util.ArrayList;


public class UserLessonFragment extends Fragment {
    static final String TAG = UserLessonFragment.class.getSimpleName();
    DataManager dataManager;
    ListView listView;
    LessonAdapter listAdapter;
    DataProvider dataProvider;
    ArrayList<Lesson> lessonList;
    Context context;
    LayoutInflater layoutInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_lesson, container, false);
        layoutInflater = inflater;
        context = this.getActivity().getApplicationContext();
        dataManager = ((SentencerApp)context).getDataManager();

        listView = (ListView) v.findViewById(R.id.list);
        buildLessonList();
        // listening to single list item on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                startLesson(position);
            }
        });
        return v;
    }

    private void buildLessonList() {
        lessonList = new ArrayList<Lesson>();
        try {
            dataProvider = dataManager.getDataProvider(SourceType.INTERNAL, context);
            if(lessonList.isEmpty()) {
                lessonList = dataProvider.getLessons();
            }

        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            Toast.makeText(context, de.getMessage(), Toast.LENGTH_LONG).show();
        }
        listAdapter = new LessonAdapter(lessonList);
        listAdapter.notifyDataSetChanged();
        listView.setAdapter(listAdapter);
    }

    public void refreshList() {
        buildLessonList();
    }

    private void startLesson(int lessonIndex) {
        // sending data to new activity
        Intent i = new Intent(context, CardActivity.class);
        i.putExtra(DataHelper.EXTRA_LESSON_INDEX, lessonList.get(lessonIndex).getId());
        i.putExtra(DataHelper.EXTRA_LESSON_TITLE, lessonList.get(lessonIndex).getTitle());
        i.putExtra(DataHelper.EXTRA_LESSON_SOURCE, lessonList.get(lessonIndex).getSourceType().ordinal());
        startActivity(i);
    }

    /**
     * LessonAdapter is intended to provide a list of incorporated lessons from assets
     */
    private class LessonAdapter extends ArrayAdapter<Lesson> {
        private ArrayList<Lesson> lessons;
        public LessonAdapter(ArrayList<Lesson> list) {
            super(UserLessonFragment.this.getActivity(), R.layout.list_item_lesson, list);
            lessons = list;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            final Lesson lesson = lessons.get(position);
            if (convertView == null) {
                row = UserLessonFragment.this.layoutInflater.inflate(R.layout.list_item_lesson, null);
            } else {
                row = convertView;
            }
            ImageView icon = (ImageView) row.findViewById(R.id.icon);
            icon.setImageResource(R.drawable.ic_action_person);
            TextView title = (TextView)row.findViewById(R.id.lesson_title);
            title.setText(lesson.getTitle());

            TextView meta = (TextView)row.findViewById(R.id.lesson_meta);

            meta.setText(lesson.getDescription() + " ("
                    + UserLessonFragment.this.getString(R.string.label_cards) +
                    ": " + lesson.getCardCount() + ")");

            return row;
        }
    }

}
