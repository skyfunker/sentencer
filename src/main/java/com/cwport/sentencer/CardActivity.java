package com.cwport.sentencer;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cwport.sentencer.data.DataException;
import com.cwport.sentencer.data.DataHelper;
import com.cwport.sentencer.data.DataManager;
import com.cwport.sentencer.model.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;


public class CardActivity extends ActionBarActivity {

    private final String TAG = CardActivity.class.getSimpleName();
    private String lessonTitle;
    private int lessonIndex;
    private int cardIndex = 0;
    private int cardCount = 0;
    private boolean flip = false;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private TextView textView;
    private ImageButton btnNext;
    private ImageButton btnFlip;
    private ImageButton btnPrev;
    private boolean showMarked = false;
    private boolean shuffled = false;
    private boolean showBackFirst = false;
    private boolean forceRewind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get parameters from intent
        Intent i = getIntent();
        this.lessonIndex = i.getIntExtra(DataHelper.EXTRA_LESSON_INDEX, 0);
        this.lessonTitle = i.getStringExtra(DataHelper.EXTRA_LESSON_TITLE);
        this.flip = this.showBackFirst;
        this.textView = (TextView) findViewById(R.id.card_text);
        this.textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                markCard();
                return false;
            }
        });
        this.btnPrev = (ImageButton) findViewById(R.id.button_prev);
        this.btnFlip = (ImageButton) findViewById(R.id.button_flip);
        this.btnNext = (ImageButton) findViewById(R.id.button_next);
        if(savedInstanceState != null) {
            this.cardIndex = savedInstanceState.getInt(DataHelper.PARAM_CARD_INDEX);
            this.flip = savedInstanceState.getBoolean(DataHelper.PARAM_FLIP);
            this.shuffled = savedInstanceState.getBoolean(DataHelper.PARAM_SHUFFLED);
            this.showMarked = savedInstanceState.getBoolean(DataHelper.PARAM_SHOWMARKED);
            this.showBackFirst = savedInstanceState.getBoolean(DataHelper.PARAM_SHOWBACKFIRST);
        }
        try {
            initCards();
//            initNavigation();
            showCard();
        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            this.textView.setText(de.getMessage());
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(DataHelper.PARAM_LESSON, lessonTitle);
        bundle.putInt(DataHelper.PARAM_CARD_INDEX, this.cardIndex);
        bundle.putBoolean(DataHelper.PARAM_FLIP, this.flip);
        bundle.putBoolean(DataHelper.PARAM_SHUFFLED, this.shuffled);
        bundle.putBoolean(DataHelper.PARAM_SHOWMARKED, this.showMarked);
        bundle.putBoolean(DataHelper.PARAM_SHOWBACKFIRST, this.showBackFirst);
    }

    private void initCards() throws DataException {
        this.cards = new ArrayList<Card>(DataManager.getInstance().getLesson(this.lessonIndex).getCards());
        if(this.showMarked) {
            Iterator<Card> iterator = this.cards.iterator();
            while(iterator.hasNext()) {
                Card c = iterator.next();
                if(!c.isMarked()) { iterator.remove(); }
            }
        }
        if(this.cards.size() > 0 && this.shuffled) {
            Collections.shuffle(this.cards, new Random(this.cards.size()));
        }
        this.cardCount = cards.size();
        if(this.cardIndex > (this.cardCount - 1) || this.forceRewind) this.cardIndex = 0; // to avoid exception
        this.forceRewind = false;
    }

    private void initNavigation() {
        if (this.cardCount == 0) {
            this.btnPrev.setVisibility(View.INVISIBLE);
            this.btnFlip.setVisibility(View.INVISIBLE);
            this.btnNext.setVisibility(View.INVISIBLE);
            return;
        }
        this.btnPrev.setVisibility(this.cardIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        this.btnFlip.setVisibility(View.VISIBLE);
        this.btnNext.setVisibility((this.cardIndex + 1) < this.cardCount ? View.VISIBLE : View.INVISIBLE);
        this.setTitle(this.lessonTitle + " (" + (this.cardIndex + 1)  + "/" + this.cardCount + ")");
    }
    private void showCard() {
        if(this.flip) {
            textView.setText(cards.get(this.cardIndex).getBackText());
        } else {
            textView.setText(cards.get(this.cardIndex).getFaceText());
        }
        if(cards.get(this.cardIndex).getMarked()) {
            this.textView.setTextColor(Color.parseColor("#FFBB33"));
        } else {
            this.textView.setTextColor(Color.parseColor("#FFFFFF"));
        }
        initNavigation();
    }

    public void nextCard(View view) {
        this.flip = this.showBackFirst;
        if(( this.cardIndex + 1) < this.cardCount) {
            this.cardIndex++;
        }
//        initNavigation();
        showCard();
    }

    public void prevCard(View view) {
        this.flip = this.showBackFirst;
        if(this.cardIndex > 0) {
            this.cardIndex--;
        }
//        initNavigation();
        showCard();
    }

    public void flipCard(View view) {
        this.flip = !this.flip;
        showCard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card, menu);
        for(int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            int id = item.getItemId();
            switch (id) {
                case R.id.action_showmarked:
                    item.setChecked(this.showMarked);
                    break;
                case R.id.action_shuffle:
                    item.setChecked(this.shuffled);
                    break;
                case R.id.action_showbackfirst:
                    item.setChecked(this.showBackFirst);
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_showmarked:
                this.showMarked = !this.showMarked;
                item.setChecked(this.showMarked);
                showMarkedCards();
                return true;
            case R.id.action_shuffle:
                this.shuffled = !this.shuffled;
                item.setChecked(this.shuffled);
                shuffleCards();
                return true;
            case R.id.action_showbackfirst:
                this.showBackFirst = !this.showBackFirst;
                item.setChecked(this.showBackFirst);
                showBackFirst();
                return true;
//            case R.id.action_settings:
//                showSettings();
//                return true;
            case R.id.action_help:
                showHelp();
                return true;
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBackFirst() {
        try {
            initCards();
//            initNavigation();
            this.flip = this.showBackFirst;
            if(this.cards.size() > 0) {
                showCard();
            } else {
                this.textView.setText(R.string.msg_no_cards);
            }
        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            this.textView.setText(de.getMessage());
        }
    }

    private void shuffleCards() {
        this.forceRewind = true;
        try {
            initCards();
//            initNavigation();
            if(this.cards.size() > 0) {
                showCard();
            } else {
                this.textView.setText(R.string.msg_no_cards);
            }
        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            this.textView.setText(de.getMessage());
        }
    }
    private void showMarkedCards() {
        this.forceRewind = true;
        try {
            initCards();
//            initNavigation();
            if(this.cards.size() > 0) {
                showCard();
            } else {
                this.textView.setText(R.string.msg_no_cards);
            }
        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            this.textView.setText(de.getMessage());
        }

    }

    private void markCard() {
        Card card = cards.get(this.cardIndex);
        card.setMarked(!card.getMarked());
        showCard();
    }

    private void showHelp() {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }
}
