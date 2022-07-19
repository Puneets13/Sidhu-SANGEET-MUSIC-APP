package com.example.sidhu_sangeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chibde.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {



    Button btnPlay, btnNext, btnPrevious, btnfastForward, btnFastRewind;
    TextView txtSongName, txtSongStart, txtSongEnd;
    SeekBar seekMusicbar;
    BarVisualizer barVisualizer;
    List<Track> track;

    String Play_on = "on";

    ImageView imageView;
    String songname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int posittion;

    ArrayList<File> mysongs;

    Thread updateSeekBar;

//    to dismiss the functionality of back button
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnfastForward = findViewById(R.id.btnFastForward);
        btnFastRewind = findViewById(R.id.btnFastBackward);
        barVisualizer = findViewById(R.id.wave);

        txtSongName = findViewById(R.id.txtSong);
        txtSongStart = findViewById(R.id.txtSongStart);
        txtSongEnd = findViewById(R.id.txtSongEnd);

        seekMusicbar = findViewById(R.id.seekBar);
        imageView = findViewById(R.id.imageView);

        populateTrack();
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mysongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String sName = intent.getStringExtra("songname");
        posittion = bundle.getInt("pos", 0);
        txtSongName.setSelected(true);
        Uri uri = Uri.parse(mysongs.get(posittion).toString());
        songname = mysongs.get(posittion).getName();
        txtSongName.setText(songname);
        try {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        } catch (NullPointerException e) {

        }


//        for backButton
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);


//        to update the sekbar

        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalduration = 100000000;

                int currentposition = 0;
                while (currentposition < totalduration) {
                    try {
                        sleep(500);
                        currentposition = mediaPlayer.getCurrentPosition();
                        seekMusicbar.setProgress(currentposition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        seekMusicbar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekMusicbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.MULTIPLY);
        seekMusicbar.getThumb().setColorFilter(getResources().getColor(R.color.purple_700), PorterDuff.Mode.SRC_IN);


        seekMusicbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });


        String endtime = createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endtime);

//        for updating the time over the seekbar curret position time
        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currenttime = createTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currenttime);
                handler.postDelayed(this, delay);
            }
        }, delay);
//
//        SongNamePass = songname;


        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying()) {
                    btnPlay.setBackgroundResource(R.drawable.ic_play_);
                    mediaPlayer.pause();
                } else {
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();

//            for making the animation in the logo
                    TranslateAnimation moveAnim = new TranslateAnimation(-25, 25, -25, 25);
                    moveAnim.setInterpolator(new AccelerateInterpolator());
                    moveAnim.setDuration(600);
                    moveAnim.setFillAfter(true);
                    moveAnim.setFillEnabled(true);
                    moveAnim.setFillAfter(true);
                    moveAnim.setRepeatMode(Animation.REVERSE);
                    moveAnim.setRepeatCount(Animation.INFINITE);
                    imageView.startAnimation(moveAnim);

                }
                createNotification.CreateNotification(PlayerActivity.this, track.get(posittion), R.drawable.ic_pause, 1, track.size() - 1);

            }
        });

        // set custom color to the line.
        barVisualizer.setColor(ContextCompat.getColor(this, R.color.black));
// define custom number of bars you want in the visualizer between (10 - 256).
        barVisualizer.setDensity(70);
// Set your media player to the visualizer.
        barVisualizer.setPlayer(mediaPlayer.getAudioSessionId());

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.performClick();
                seekMusicbar.setProgress(0);
//                 updateSeekBar.start();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.stop();
                mediaPlayer.release();
                btnPlay.setBackgroundResource(R.drawable.ic_pause);
                posittion = ((posittion + 1) % (mysongs.size()));
                Uri uri = Uri.parse(mysongs.get(posittion).toString());
                mediaPlayer = mediaPlayer.create(getApplicationContext(), uri);
                songname = mysongs.get(posittion).getName();
                txtSongName.setText(songname);

                startAnimation(imageView, 360f);

                mediaPlayer.start();
                seekMusicbar.setMax(mediaPlayer.getDuration());
                String endtime = createTime(mediaPlayer.getDuration());
                txtSongEnd.setText(endtime);

                createNotification.CreateNotification(PlayerActivity.this, track.get(posittion ), R.drawable.ic_pause, 1, track.size() - 1);
                barVisualizer.release();
                // set custom color to the line.
                barVisualizer.setColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
// define custom number of bars you want in the visualizer between (10 - 256).
                barVisualizer.setDensity(70);
// Set your media player to the visualizer.
                barVisualizer.setPlayer(mediaPlayer.getAudioSessionId());

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        btnNext.performClick();
                        seekMusicbar.setProgress(0);
                    }
                });


            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlay.setBackgroundResource(R.drawable.ic_pause);
                mediaPlayer.stop();
                mediaPlayer.release();
                posittion = ((posittion - 1) < 0) ? (mysongs.size() - 1) : posittion - 1;
                Uri uri = Uri.parse(mysongs.get(posittion).toString());
                mediaPlayer = mediaPlayer.create(getApplicationContext(), uri);
                songname = mysongs.get(posittion).getName();
                txtSongName.setText(songname);
                String endtime = createTime(mediaPlayer.getDuration());
                txtSongEnd.setText(endtime);
                mediaPlayer.start();
                seekMusicbar.setMax(mediaPlayer.getDuration());
                startAnimation(imageView, -360f);
                createNotification.CreateNotification(PlayerActivity.this, track.get(posittion), R.drawable.ic_pause, 1, track.size() - 1);

                barVisualizer.release();
                // set custom color to the line.
                barVisualizer.setColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
// define custom number of bars you want in the visualizer between (10 - 256).
                barVisualizer.setDensity(70);
// Set your media player to the visualizer.
                barVisualizer.setPlayer(mediaPlayer.getAudioSessionId());

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        btnNext.performClick();
                        seekMusicbar.setProgress(0);
//                 updateSeekBar.start();
                    }
                });
            }
        });

//        for fastforward and rewind button

        btnfastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                }
            }
        });
        btnFastRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }

        });


    }





    //method for making animation
    public void startAnimation(View view, Float degree) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree);

        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }


    public String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        time = time + min + ":";
        if (sec < 10) {
            time += "0";
        }
        time += sec;
        return time;

    }

    public void populateTrack() {
        track = new ArrayList<>();
        final ArrayList<File> songslist = findSong(Environment.getExternalStorageDirectory());
        String items;
        for (int i = 0; i < songslist.size(); i++) {
            items = songslist.get(i).getName();
            track.add(new Track(items, "UnKnown Artist ", R.drawable.p2));
        }
    }

    public ArrayList<File> findSong(@NonNull File file) {

        ArrayList arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(findSong(singleFile));

//                    deleteFile(singleFile.getAbsolutePath());

                } else {
                    if ((singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav"))) {
                        arrayList.add(singleFile);
                    }
                }
            }
        }

        return arrayList;
    }

    // this event will enable the back
    // function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(PlayerActivity.this,MainActivity.class);
                intent.putExtra("songname",txtSongName.getText().toString());
                intent.putExtra("Play_on",Play_on);

                startActivity(intent);
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}






