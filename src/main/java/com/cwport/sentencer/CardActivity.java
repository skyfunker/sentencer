package com.cwport.sentencer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.cwport.sentencer.data.DataException;
import com.cwport.sentencer.data.DataHelper;
import com.cwport.sentencer.data.DataManager;
import com.cwport.sentencer.model.Card;
import com.cwport.sentencer.model.Lesson;
import com.cwport.sentencer.speak.Speaker;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


public class CardActivity extends ActionBarActivity {

    private final String TAG = CardActivity.class.getSimpleName();
    private String lessonTitle;
    private int lessonIndex;
    private int cardIndex = 0;
    private int cardCount = 0;
    private boolean flip = false;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private Lesson lesson = new Lesson();
    private TextView textView;
    private ImageButton btnNext;
    private ImageButton btnFlip;
    private ImageButton btnPrev;
    private Button btnPlay;
    private boolean showMarked = false;
    private boolean shuffled = false;
    private boolean showBackFirst = false;
    private boolean forceRewind = false;
    private String textLocale; // text locale of the current card side
    private Set<String> markedCardsIdSet = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get parameters from intent
        Intent i = getIntent();
        this.lessonIndex = i.getIntExtra(DataHelper.EXTRA_LESSON_INDEX, 0);
        this.lessonTitle = i.getStringExtra(DataHelper.EXTRA_LESSON_TITLE);

        this.textView = (TextView) findViewById(R.id.card_text);
        this.textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                markCard();
                return false;
            }
        });
        this.btnPlay = (Button) findViewById(R.id.button_play);
        this.btnPlay.setOnClickListener( new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     if(CommonUtils.isConnectedOrConnectingToNetwork(getApplicationContext())) {
                         textToSpeech(v, textLocale, textView.getText().toString());
                     } else {
                         Toast.makeText(getApplicationContext(), R.string.msg_no_internet_connection,
                                 Toast.LENGTH_LONG).show();
                     }
                 }
             }
        );
        this.btnPrev = (ImageButton) findViewById(R.id.button_prev);
        this.btnFlip = (ImageButton) findViewById(R.id.button_flip);
        this.btnNext = (ImageButton) findViewById(R.id.button_next);

        try {
            this.lesson = DataManager.getInstance().getLesson(this.lessonIndex);

            if(savedInstanceState != null) {
                this.cardIndex = savedInstanceState.getInt(DataHelper.PARAM_CARD_INDEX);
                this.flip = savedInstanceState.getBoolean(DataHelper.PARAM_FLIP);
                this.shuffled = savedInstanceState.getBoolean(DataHelper.PARAM_SHUFFLED);
                this.showMarked = savedInstanceState.getBoolean(DataHelper.PARAM_SHOWMARKED);
                this.showBackFirst = savedInstanceState.getBoolean(DataHelper.PARAM_SHOWBACKFIRST);
                String markedCardIdArray[] = savedInstanceState.getStringArray(DataHelper.PARAM_MARKED_CARDS);
                this.markedCardsIdSet = new HashSet<String>();
                Collections.addAll(this.markedCardsIdSet, markedCardIdArray);
            } else {
                SharedPreferences sharedPref = this.getSharedPreferences(
                        DataHelper.PREF_PREFIX + this.lesson.getFilename(), Context.MODE_PRIVATE);
                this.cardIndex = sharedPref.getInt(DataHelper.PARAM_CARD_INDEX, 0);
                this.shuffled = sharedPref.getBoolean(DataHelper.PARAM_SHUFFLED, false);
                this.showMarked = sharedPref.getBoolean(DataHelper.PARAM_SHOWMARKED, false);
                this.showBackFirst = sharedPref.getBoolean(DataHelper.PARAM_SHOWBACKFIRST, false);
                this.markedCardsIdSet = sharedPref.getStringSet(DataHelper.PARAM_MARKED_CARDS, new HashSet<String>());
            }
            this.flip = this.showBackFirst;

            initCards();
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
        bundle.putStringArray(DataHelper.PARAM_MARKED_CARDS, getMarkedCardIdArray());
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save SharedPreferences on activity stop
        SharedPreferences sharedPref = this.getSharedPreferences(
                DataHelper.PREF_PREFIX + this.lesson.getFilename(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(DataHelper.PARAM_CARD_INDEX, this.cardIndex);
        editor.putBoolean(DataHelper.PARAM_SHUFFLED, this.shuffled);
        editor.putBoolean(DataHelper.PARAM_SHOWMARKED, this.showMarked);
        editor.putBoolean(DataHelper.PARAM_SHOWBACKFIRST, this.showBackFirst);
        editor.putStringSet(DataHelper.PARAM_MARKED_CARDS, getMarkedCardsIdSet());
        editor.commit();
    }

    private Set<String> getMarkedCardsIdSet() {
        Set<String> markedSet = new HashSet<String>();
        for(Card c: this.cards) {
            if(c.isMarked()) {
                markedSet.add(c.getId());
            }
        }
        return markedSet;
    }

    private String[] getMarkedCardIdArray() {
        Set<String> markedSet = getMarkedCardsIdSet();
        String markedIdArray[] = new String[markedSet.size()];
        markedIdArray = markedSet.toArray(markedIdArray);
        return  markedIdArray;
    }

    private void initCards() throws DataException {
        this.cards = new ArrayList<Card>(lesson.getCards());

        Iterator<Card> it = this.cards.iterator();
        while(it.hasNext()) {
            Card c = it.next();
            c.setMarked(this.markedCardsIdSet.contains(c.getId()));
        }

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
            this.btnPlay.setVisibility(View.INVISIBLE);
            return;
        }
        this.btnPrev.setVisibility(this.cardIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        this.btnFlip.setVisibility(this.cardCount > 0 ? View.VISIBLE : View.INVISIBLE);
        this.btnNext.setVisibility((this.cardIndex + 1) < this.cardCount ? View.VISIBLE : View.INVISIBLE);
        this.setTitle(this.lessonTitle + " (" + (this.cardIndex + 1)  + "/" + this.cardCount + ")");
        if(Speaker.localeSupported(this.textLocale)) {
            this.btnPlay.setVisibility(View.VISIBLE);
        } else {
            this.btnPlay.setVisibility(View.INVISIBLE);
        }
    }
    private void showCard() {
        if(this.flip) {
            textView.setText(cards.get(this.cardIndex).getBackText());
            this.textLocale = this.lesson.getBackLocale();
        } else {
            textView.setText(cards.get(this.cardIndex).getFaceText());
            this.textLocale = this.lesson.getFaceLocale();
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
        showCard();
    }

    public void prevCard(View view) {
        this.flip = this.showBackFirst;
        if(this.cardIndex > 0) {
            this.cardIndex--;
        }
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
                case R.id.action_goto:
                    break;
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
            case R.id.action_goto:
                goTo();
                break;
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

    /**
     * Build and show position selection dialog and navigate to selected position
     */
    private void goTo() {
        final AlertDialog.Builder gotoDialog = new AlertDialog.Builder(this);
        gotoDialog.setTitle(R.string.action_goto);
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(this.cardCount);
        numberPicker.setMinValue(1);
        numberPicker.setValue(this.cardIndex + 1);
        gotoDialog.setView(numberPicker);
        // Set the action buttons
        gotoDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK, so save the mSelectedItems results somewhere
                // or return them to the component that opened the dialog
                cardIndex = numberPicker.getValue() - 1;
                showCard();
                return;
            }
        });
        gotoDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        gotoDialog.show();
    }

    private void showBackFirst() {
        try {
            initCards();
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
            if(this.cards.size() > 0) {
                showCard();
                return;
            } else {
                this.textView.setText(R.string.msg_no_cards);
            }
        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            this.textView.setText(de.getMessage());
        }
        initNavigation();
    }

    private void markCard() {
        Card card = cards.get(this.cardIndex);
        card.setMarked(!card.getMarked());
        if(!card.isMarked() && this.markedCardsIdSet.contains(card.getId())) {
            this.markedCardsIdSet.remove(card.getId());
        }
        showCard();
    }

    private void showHelp() {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    private void textToSpeech(View view, String locale, String text) {
        String url;
        try {
            url = "http://translate.google.com/translate_tts?tl=" + locale +
                    "&q=" + java.net.URLEncoder.encode(text, "UTF-8");
        } catch(UnsupportedEncodingException uex) {
            Log.e(TAG, uex.getMessage());
            return;
        }
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlay.setEnabled(true);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(getApplicationContext(), "MediaPlayer error: " + what,
                        Toast.LENGTH_LONG).show();
                btnPlay.setEnabled(true);
                return true;
            }
        });
        try {
            this.btnPlay.setEnabled(false);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
//            HashMap<String, String> headers = new HashMap<String, String>();
//            headers.put("Accept-Language", this.textLocale.replace("_", "-"));
//            mediaPlayer.setDataSource(this, Uri.parse(url), headers);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.start();
        } catch(Exception ex) {
            Toast.makeText(getApplicationContext(), "MediaPlayer error: " + ex.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, ex.getLocalizedMessage());
            btnPlay.setEnabled(true);
        }
    }
}
