package com.example.sidhu_sangeet;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

//Kiran


public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<File> mySongs;
//    ArrayList<String> items;
    customAdapter customAdapter;
    NotificationManager notificationManager;
    TextView songName;

    String songnameReceived;

    static MediaPlayer mediaPlayer;
    int posittion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listview);
//        btnPlay =  findViewById(R.id.btnPlay);
        songName = findViewById(R.id.txtSongName);
//        populateTrack();
        runtimePermission();

        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.release();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }


//        intent from PlayerActivity
        songnameReceived = getIntent().getStringExtra("songname");
        songName.setText(songnameReceived);
        songName.setSelected(true);


//        songName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
//                        intent.putExtra("songname", songName.getText().toString());
////                        intent.putExtra("pos", items.indexOf(duplicate.get(position)));
////                            .putExtra("pos",index.get(position)));
//                startActivity(intent);
//
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Type to search..");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                customAdapter.getFilter().filter(s);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.shuffle:
//                Collections.sort(items);
//                Toast.makeText(this, "SORTED", Toast.LENGTH_SHORT).show();
//                customAdapter = new customAdapter(items, this);
//                listView.setAdapter(customAdapter);
//                listView.setTextFilterEnabled(true);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//
//    }


    public void runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySong();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    public ArrayList<File> findSong(File file) {
        ArrayList arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(findSong(singleFile));

                } else {
                    if ((singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav"))) {
                        arrayList.add(singleFile);
                    }
                }
            }
        }
//         Sorting ArrayList in ascending Order
//         using Collection.sort() method
//        Collections.sort(arrayList);
        return arrayList;
    }


    public void displaySong() {
        mySongs = findSong(Environment.getExternalStorageDirectory());
//        items = new String[mySongs.size()];
        ArrayList<String> items;
        items = new ArrayList<String>(mySongs.size());
        for (int i = 0; i < mySongs.size(); i++) {
            items.add(mySongs.get(i).getName().replace(".mp3", "").replace(".wav", ""));

        }
//        to sort the arrayList in ascending order
//        Collections.sort(items);
//        by sorting the list the position of the songs being changed
        customAdapter = new customAdapter(items, this);
        listView.setAdapter(customAdapter);
        listView.setTextFilterEnabled(true);
    }

    public class customAdapter extends BaseAdapter implements Filterable {
        private ArrayList<String> items;
        ArrayList<String> duplicate;
        private Context context;
        private LayoutInflater inflater;

        public customAdapter(ArrayList<String> items, Context context) {
            super();
            this.items = items;
            this.duplicate = items;
            this.context = context;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            getFilter();
        }

        @Override


        public int getCount() {
            return duplicate.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView txtsong = view.findViewById(R.id.txtSong);
            txtsong.setSelected(true);
            txtsong.setText(duplicate.get(position));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                            .putExtra("songs", mySongs)
                            .putExtra("songname", txtsong.getText())
                            .putExtra("pos", items.indexOf(duplicate.get(position))));
//                            .putExtra("pos",index.get(position)));


//                    to put text on main Activity screen
                    songName.setText(txtsong.getText().toString());
                    songName.setSelected(true);


                }
            });
            return view;
        }


        public Filter getFilter() {
            Filter filter = new Filter() {

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    duplicate = (ArrayList<String>) results.values;
                    notifyDataSetChanged();
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    ArrayList<String> FilteredList = new ArrayList<String>();
                    if (constraint == null || constraint.length() == 0) {
                        // No filter implemented we return all the list
                        results.values = items;
                        results.count = items.size();
                    } else {
                        for (int i = 0; i < items.size(); i++) {
                            String data = items.get(i);
                            if (data.toLowerCase().contains(constraint.toString())) {
                                FilteredList.add(data);
//                                index.add(i);
                            }
                        }
                        results.values = FilteredList;
                        results.count = FilteredList.size();
                    }
                    return results;
                }
            };
            return filter;
        }
    }

    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(createNotification.CHANNEL_ID, "Puneet channel", NotificationManager.IMPORTANCE_LOW);
            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}







