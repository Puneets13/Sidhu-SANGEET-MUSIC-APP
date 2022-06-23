package com.example.sidhu_sangeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    Button btnPlay, btnNext, btnPrevious, btnfastForward, btnFastRewind;
    TextView txtSongName, txtSongStart, txtSongEnd;
    SeekBar seekMusicbar;
    //    BarVisualiser barVisualiser;
    List<Track> track;

    ImageView imageView;
    String songname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int posittion;

    ArrayList<File> mysongs;

    Thread updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnfastForward = findViewById(R.id.btnFastForward);
        btnFastRewind = findViewById(R.id.btnFastBackward);

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

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();


//        to update the sekbar

        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalduration = mediaPlayer.getDuration();

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
                while (currentposition == totalduration) {
                    try {
                        sleep(200);
//                        currentposition = mediaPlayer.getCurrentPosition();
                        seekMusicbar.setProgress(0);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekMusicbar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekMusicbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_700), PorterDuff.Mode.MULTIPLY);
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


        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNotification.CreateNotification(PlayerActivity.this, track.get(posittion), R.drawable.ic_pause, 1, track.size() - 1);

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
            }
        });

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
                createNotification.CreateNotification(PlayerActivity.this, track.get(posittion+1), R.drawable.ic_pause, 1, track.size() - 1);

                mediaPlayer.stop();
                mediaPlayer.release();
                btnPlay.setBackgroundResource(R.drawable.ic_pause);
                posittion = ((posittion + 1)%mysongs.size());
                Uri uri = Uri.parse(mysongs.get(posittion).toString());
                mediaPlayer = mediaPlayer.create(getApplicationContext(), uri);
                songname = mysongs.get(posittion).getName();
                txtSongName.setText(songname);
                String endtime = createTime(mediaPlayer.getDuration());
                txtSongEnd.setText(endtime);
                mediaPlayer.start();

                startAnimation(imageView, 360f);

            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNotification.CreateNotification(PlayerActivity.this, track.get(posittion-1), R.drawable.ic_pause, 1, track.size() - 1);
                btnPlay.setBackgroundResource(R.drawable.ic_pause);
                mediaPlayer.stop();
                mediaPlayer.release();
                posittion = ((posittion-1)<0)?(mysongs.size()-1):posittion-1;
                Uri uri = Uri.parse(mysongs.get(posittion).toString());
                mediaPlayer = mediaPlayer.create(getApplicationContext(), uri);
                songname = mysongs.get(posittion).getName();
                txtSongName.setText(songname);
                String endtime = createTime(mediaPlayer.getDuration());
                txtSongEnd.setText(endtime);
                mediaPlayer.start();
                startAnimation(imageView, -360f);
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
                } else {
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                        arrayList.add(singleFile);
                    }
                }
            }
        }

        return arrayList;
    }

}

