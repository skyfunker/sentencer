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
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.cwport.sentencer.data.DataException;
import com.cwport.sentencer.data.DataHelper;
import com.cwport.sentencer.data.DataManager;
import com.cwport.sentencer.data.SourceType;
import com.cwport.sentencer.model.Card;
import com.cwport.sentencer.model.Lesson;
import com.cwport.sentencer.media.TextToSpeech;

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
    private String lessonId;
    private SourceType lessonSource;
    private int cardIndex = 0;
    private int cardCount = 0;
    private boolean flip = false;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private Lesson lesson;
    private TextView textView;
    private Button btnNext;
    private Button btnFlip;
    private Button btnPrev;
    private Button btnPlay;
    private boolean showMarked = false;
    private boolean shuffled = false;
    private boolean showBackFirst = false;
    private boolean forceRewind = false;
    private String textLocale; // text locale of the current card side
    private Set<String> markedCardsIdSet = new HashSet<String>();
    private ArrayList<String> ttsUrls = new ArrayList<String>();
    private int ttsCounter = 0;
    private DataManager dataManager;
    MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dataManager = ((SentencerApp)getApplicationContext()).getDataManager();
        // get parameters from intent
        Intent i = getIntent();
        this.lessonId = i.getStringExtra(DataHelper.EXTRA_LESSON_INDEX);
        this.lessonTitle = i.getStringExtra(DataHelper.EXTRA_LESSON_TITLE);
        this.lessonSource = SourceType.values()[i.getIntExtra(DataHelper.EXTRA_LESSON_SOURCE, 0)];

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
        this.btnPrev = (Button) findViewById(R.id.button_prev);
        this.btnFlip = (Button) findViewById(R.id.button_flip);
        this.btnNext = (Button) findViewById(R.id.button_next);

        try {
            this.lesson = dataManager.getDataProvider(this.lessonSource, getApplicationContext()).getLesson(this.lessonId);

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
                this.flip = this.showBackFirst;
            }

            initCards();
            showCard();
        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            this.textView.setText(de.getMessage());
        } catch(Exception ex) {
            Log.e(TAG, ex.getMessage());
            this.textView.setText(ex.getMessage());
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
        for (Card c : this.cards) {
            c.setMarked(this.markedCardsIdSet.contains(c.getId()));
        }

        if(this.showMarked) {
            Iterator<Card> iterator = this.cards.iterator();
            while(iterator.hasNext()) {
                Card c = iterator.next();
                if(!c.isMarked()) {
                    iterator.remove();
                }
            }
        }

        if(this.cards.size() > 0 && this.shuffled) {
            Collections.shuffle(this.cards, new Random());
            this.cardIndex = 0;
        }
        this.cardCount = cards.size();
        if(this.cardIndex > (this.cardCount - 1) || this.forceRewind) this.cardIndex = 0; // to avoid exception
        this.forceRewind = false;
    }

    /**
     * Count marked cards in current lesson
     * @return Number of marked cards
     */
    public int countMarked() {
        int count = 0;
        Iterator<Card> iterator = this.cards.iterator();
        while(iterator.hasNext()) {
            Card c = iterator.next();
            if(c.isMarked()) { count++; }
        }
        return count;
    }

    private void initNavigation() {
        if (this.cardCount > 0) {
            this.btnPrev.setVisibility(this.cardIndex > 0 ? View.VISIBLE : View.INVISIBLE);
            this.btnFlip.setVisibility(this.cardCount > 0 ? View.VISIBLE : View.INVISIBLE);
            this.btnNext.setVisibility((this.cardIndex + 1) < this.cardCount ? View.VISIBLE : View.INVISIBLE);
            this.setTitle(this.lessonTitle + " (" + (this.cardIndex + 1)  + "/" + this.cardCount + ")");
            if(TextToSpeech.localeSupported(this.textLocale)) {
                this.btnPlay.setVisibility(View.VISIBLE);
            } else {
                this.btnPlay.setVisibility(View.INVISIBLE);
            }
        } else {

            this.btnPrev.setVisibility(View.INVISIBLE);
            this.btnFlip.setVisibility(View.INVISIBLE);
            this.btnNext.setVisibility(View.INVISIBLE);
            this.btnPlay.setVisibility(View.INVISIBLE);
            this.setTitle(this.lessonTitle + " (0/0)");
        }
    }
    private void showCard() {
        if(this.cards.size() > 0) {
            if(this.flip) {
                textView.setText(cards.get(this.cardIndex).getBackText());
                this.textLocale = this.lesson.getBackLocale();
            } else {
                textView.setText(cards.get(this.cardIndex).getFaceText());
                this.textLocale = this.lesson.getFaceLocale();
            }
            if(cards.get(this.cardIndex).getMarked()) {
                this.textView.setTextColor(this.getResources().getColor(R.color.orange));
            } else {
                this.textView.setTextColor(this.getResources().getColor(R.color.white));
            }
        } else {
            showNoCardsMessage();
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
        if(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_MENU)) {
            getMenuInflater().inflate(R.menu.card, menu);
            for (int i = 0; i < menu.size(); i++) {
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
        } else { // no hardware menu button
            getMenuInflater().inflate(R.menu.card_nomenubutton, menu);
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
                if(!this.showMarked && (countMarked() < 1)) {
                    Toast.makeText(this, R.string.msg_no_marked_cards,
                            Toast.LENGTH_LONG).show();

                } else {
                    this.showMarked = !this.showMarked;
                    item.setChecked(this.showMarked);
                    showMarkedCards();
                }
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
            case R.id.action_options:
                showOptionsDialog();
                return true;
            case R.id.action_help:
                showHelp();
                return true;
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showOptionsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.action_options);
        final CharSequence[] options = {
                this.getResources().getString(R.string.action_shuffle),
                this.getResources().getString(R.string.action_showmarked),
                this.getResources().getString(R.string.action_show_back_first)
        };
        final boolean[] checkedOptions = {this.shuffled, this.showMarked, this.showBackFirst};

        builder.setMultiChoiceItems(options, checkedOptions, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                return;
            }
        });
        // Set the action buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                shuffled = checkedOptions[0];
                showMarked = checkedOptions[1];
                showBackFirst = checkedOptions[2];
                try {
                    initCards();
                    showCard();
                } catch(DataException de) {
                    Log.e(TAG, de.getMessage());
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        builder.show();
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
                showNoCardsMessage();
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
                showNoCardsMessage();
                initNavigation();
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
                showNoCardsMessage();
                initNavigation();
            }
        } catch(DataException de) {
            Log.e(TAG, de.getMessage());
            this.textView.setText(de.getMessage());
        }
        initNavigation();
    }

    private void markCard() {
        Card card = this.cards.get(this.cardIndex);
        card.setMarked(!card.getMarked());
        if(card.isMarked()) {
            if(!this.markedCardsIdSet.contains(card.getId())) {
                this.markedCardsIdSet.add(card.getId());
            }
        } else {
            if (this.markedCardsIdSet.contains(card.getId())) {
                this.markedCardsIdSet.remove(card.getId());
            }
        }
        showCard();
    }

    private void showHelp() {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    private void showNoCardsMessage() {
        this.textView.setTextColor(Color.parseColor("#FFFFFF"));
        this.textView.setText(R.string.msg_no_cards);
    }

    private void textToSpeech(View view, String locale, String text) {
        try {
            ttsUrls = TextToSpeech.textToSpeech(text, locale);
            ttsCounter = 0;
        } catch(UnsupportedEncodingException uex) {
            Log.e(TAG, uex.getMessage());
            Toast.makeText(getApplicationContext(), "Text-to-speech error: " + uex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        if(ttsUrls.size() > 0) {
            playSound();
        } else {
            Toast.makeText(getApplicationContext(), "Cannot prepare text-to-speech request",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void playSound() {
        this.btnPlay.setCompoundDrawablesWithIntrinsicBounds(
                this.getResources().getDrawable(R.drawable.ic_action_pause),
                null, null, null);
        this.btnPlay.setEnabled(false);
        mediaPlayer.reset();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                ttsCounter++;
                if(ttsCounter < ttsUrls.size()) {
                    playSound();
                } else {
                    refreshPlayer();
                }
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(getApplicationContext(), "MediaPlayer error: " + what,
                        Toast.LENGTH_LONG).show();
                refreshPlayer();
                return true;
            }
        });
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(ttsUrls.get(ttsCounter));

//            HashMap<String, String> headers = new HashMap<String, String>();
//            headers.put("Accept-Language", this.textLocale.replace("_", "-"));
//            mediaPlayer.setDataSource(this, Uri.parse(url), headers);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.start();
        } catch(Exception ex) {
            Toast.makeText(getApplicationContext(), "MediaPlayer error: " + ex.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, ex.getLocalizedMessage());
            refreshPlayer();
        }
    }

    private void refreshPlayer() {
        ttsUrls = new ArrayList<String>();
        ttsCounter = 0;
        this.btnPlay.setCompoundDrawablesWithIntrinsicBounds(
                this.getResources().getDrawable(R.drawable.ic_action_play),
                null, null, null);
        btnPlay.setEnabled(true);
        mediaPlayer.reset();
    }
}
