package com.aslearn;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.aslearn.db.DatabaseAccess;
import com.aslearn.db.Word;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * Created by jakejarvis98
 *
 * This activity shows the user the info pages for all of the signs in a specified lesson.
 */

public class InfoLesson extends AppCompatActivity{

    TextView wordView;
    TextView infoView;
    VideoView videoView;
    ImageView imageView;
   // Intent intent;
    Button nextSignButton;
    Button prevSignButton;
    ProgressBar progressBar;
    private Word word;
    private ArrayList<Word> words;
    private int index;

    /**
     * Finds the views from the xml, loads the database to an array of Words, then
     * @param savedInstanceState used for the super method call
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Intent intent = getIntent();
        index = 0;
        setContentView(R.layout.infopage);
        wordView = findViewById(R.id.wordText);
        imageView = findViewById(R.id.signJpg);
        infoView = findViewById(R.id.topInfo);
        videoView = (VideoView) findViewById(R.id.signVideo);
        nextSignButton = findViewById(R.id.nextSignButton);
        prevSignButton = findViewById(R.id.prevSignButton);
        progressBar = findViewById(R.id.progressBar);
//        DatabaseAccess dbAccess = DatabaseAccess.getInstance(this);
//        words = dbAccess.selectWordsByLesson(intent.getStringExtra("lessonName"));
        ExampleAsyncTask task = new ExampleAsyncTask(this);
        task.execute(this);
//        System.out.println("Number of words: " + words.size());
//        setTitle(intent.getStringExtra("lessonName"));
//        setupSign();
    }

    /**
     * Fills in the views with data about each sign. It also checks if the given media is a picture
     * or a video, and will assign it to the correct view.
     */
    private void setupSign() {
        word = words.get(index);
        if (index == words.size() - 1) nextSignButton.setText("To Quiz");
        else nextSignButton.setText("Next Sign");
        if (index <= 0) {
            prevSignButton.setEnabled(false);
            prevSignButton.setAlpha(0.5f);
        } else {
            prevSignButton.setEnabled(true);
            prevSignButton.setAlpha(1f);
        }
        System.out.println("Word: " + word.getWord());
        if (wordView == null){
            System.out.println("Wordview null");
        }
        wordView.setText(word.getWord());
        infoView.scrollTo(0,0);
        infoView.setText(word.getBasicInfo() + "\n\n" + word.getMoreInfo());
        infoView.setMovementMethod(new ScrollingMovementMethod());
        String fileName = word.getVisualFile();
        System.out.println(fileName);
        String[] fileNameSplit = fileName.split("\\.");
        System.out.println(fileNameSplit.length);
        fileName = fileNameSplit[0];
        if(fileNameSplit[1].equals(("jpg"))) {
            int resID = getResources().getIdentifier(fileName, "drawable", getPackageName());
            videoView.setVisibility(View.INVISIBLE);
            imageView.setImageResource(resID);
            imageView.setVisibility(View.VISIBLE);
        } else {
            int resID = getResources().getIdentifier(fileName, "raw", getPackageName());
            android.net.Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resID);
            videoView.setVideoURI(uri);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setVolume(0f, 0f);
                    mp.setLooping(true);
                }
            });
            videoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            videoView.start();
        }
    }

    /**
     * On pressing the prev sign button, it will load and display info about the sign prior to the
     * current sign if there is any.
     * @param view the prevSign button
     */
    public void prevSign(View view) {
        index--;
        setupSign();
    }

    /**
     * Loads the next sign.  If it has reached the end of the index, it will present the quiz
     * for the lesson.
     * @param view the nextSign button
     */
    public void nextSign(View view) {
        index++;
        if(index < words.size()) {
            prevSignButton.setText(R.string.prevSignBtn);
            setupSign();
        } else {
            Intent intent = new Intent(this, Quiz.class);
            intent.putExtra("lessonName", word.getLesson());
            startActivity(intent);
        }
    }

    private static class ExampleAsyncTask extends AsyncTask<InfoLesson, Void, Void> {
        private WeakReference<InfoLesson> infoLessonWeakReference;

        ExampleAsyncTask(InfoLesson infoLesson){
            infoLessonWeakReference = new WeakReference<InfoLesson>(infoLesson);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            InfoLesson infoLesson = infoLessonWeakReference.get();
            if (infoLesson == null || infoLesson.isFinishing()) {
                return;
            }
            infoLesson.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(InfoLesson... infoLessons) {
            InfoLesson infoLesson = infoLessonWeakReference.get();
            DatabaseAccess dbAccess = DatabaseAccess.getInstance(infoLessons[0]);
            infoLesson.words = dbAccess.selectWordsByLesson(infoLessons[0].getIntent().getStringExtra("lessonName"));
            System.out.println("Database access successful on background thread!!!!!");
            System.out.println("Number of words: " + infoLesson.words.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            InfoLesson infoLesson = infoLessonWeakReference.get();
            if (infoLesson == null || infoLesson.isFinishing()) {
                return;
            }
            infoLesson.setTitle(infoLesson.getIntent().getStringExtra("lessonName"));
            infoLesson.setupSign();
            infoLesson.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
