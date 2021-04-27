package com.example.hw3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayMusicActivity extends AppCompatActivity {
    public static String MAIN_ACTION="MAIN_ACTION";
    public static String MUSIC_PREV="MUSIC_PREV";
    public static String MUSIC_NEXT="MUSIC_NEXT";
    public static String MUSIC_PAUSE="MUSIC_PAUSE";
    public static String MUSIC_PLAY="MUSIC_PLAY;";

    public static String MUSIC_TOTAL_LENGTH="MUSIC_TOTAL_LENGTH";
    public static String MUSIC_COMPLETE="MUSIC_COMPLETE";

    TextView music_title;
    ImageView album_cover;

    ArrayList<Music> list;
    ProgressBar progressBar;
    TextView total_Duration;
    TextView current_position;
    Button play_btn;
    int position;

    int current=0;
    Thread current_position_thread;
    IMusicService mBinder=null;

    private ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("musicplayer","onServiceConnected()");
            mBinder=IMusicService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("musicplayer","onServiceConnected()");
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        progressBar=(ProgressBar)findViewById(R.id.music_progress);
        current_position=(TextView)findViewById(R.id.current_length);
        play_btn=(Button)findViewById(R.id.play_btn);
        play_btn.setText("║");

        Intent serviceIntent = new Intent(this,MusicService.class);
        serviceIntent.setPackage("com.example.hw3");
        bindService(serviceIntent,mConnection,BIND_AUTO_CREATE);


        Intent intent = getIntent();
        list=new ArrayList<>();
        list=(ArrayList<Music>)intent.getSerializableExtra("music_list");
        position=(int)intent.getIntExtra("position",0);

        if(list!=null){
            setView();
        }

        Intent intent_music_service=new Intent(this,MusicService.class);
        intent_music_service.setAction(MAIN_ACTION);
        intent_music_service.putExtra("list",list);
        intent_music_service.putExtra("position",position);
        startService(intent_music_service);

        current_position_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted())
                    try{
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    int cur=mBinder.getCurrentPosition();

                                    String Min = String.format("%2d", (cur / 1000 / 60) % 60);
                                    String Sec = String.format("%2d", (cur / 1000) % 60);
                                    current_position.setText(Min+" : "+Sec);

                                    progressBar.setProgress(cur);

                                }catch (RemoteException e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
            }
        });
        current_position_thread.start();
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.prev_btn:
                if(position==0){
                    position=list.size()-1;
                }
                else{
                    position=position-1;
                }

                setView();
                play_btn.setText("║");

                Intent intent=new Intent(this,MusicService.class);
                intent.setAction(MUSIC_PREV);
                startService(intent);
                break;

            case R.id.next_btn:
                if(position==list.size()-1){
                    position=0;
                }
                else {
                    position = position + 1;
                }

                setView();
                play_btn.setText("║");

                Intent intent2=new Intent(this,MusicService.class);
                intent2.setAction(MUSIC_NEXT);
                startService(intent2);
                break;

            case R.id.play_btn:
                if(play_btn.getText().equals("║")){
                    play_btn.setText("ᐅ");

                    Intent intent3=new Intent(this,MusicService.class);
                    intent3.setAction(MUSIC_PAUSE);
                    startService(intent3);
                }
                else{
                    play_btn.setText("║");
                    final Intent intent4=new Intent(this,MusicService.class);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    intent4.setAction(MUSIC_PLAY);
                                    startService(intent4);
                                }
                            });
                        }
                    }).start();

                }
                break;

            default:
                break;
        }
    }

    public void setView(){
        music_title=(TextView)findViewById(R.id.title);
        music_title.setText(list.get(position).getTitle());

        album_cover=(ImageView)findViewById(R.id.album_cover);
        Uri img=Uri.parse(list.get(position).getAlbum_cover());
        Uri no_cover=Uri.parse("content://media/external/audio/albumart/1");

        if(img.toString().equals(no_cover.toString())==true){
            album_cover.setImageResource(R.drawable.default_albumart);
        }
        else{
            album_cover.setImageURI(img);
        }

        int duration=list.get(position).getDuration();
        String Min=String.format("%d",(duration/1000/60)%60);
        String Sec=String.format("%d",(duration/1000)%60);
        total_Duration=(TextView)findViewById(R.id.total_length);
        total_Duration.setText(Min+" : "+Sec);

        progressBar.setMax(duration);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        final String is_complete=intent.getStringExtra("is_complete");

        if(is_complete!=null){
            if(is_complete.equals("yes")){
                if(position==list.size()-1){
                    position=0;
                }
                else{
                    position++;
                }

                setView();
            }
        }

        if(intent.getAction()!=null){
            if(intent.getAction().equals(MUSIC_NEXT)){
                list=(ArrayList<Music>)intent.getSerializableExtra("list");
                position=intent.getIntExtra("position",0);
                setView();

                play_btn.setText("║");
                Intent intent_music_service=new Intent(this,MusicService.class);
                intent_music_service.setAction(MAIN_ACTION);
                intent_music_service.putExtra("list",list);
                intent_music_service.putExtra("position",position);
                startService(intent_music_service);
            }
            else if(intent.getAction().equals(MUSIC_PREV)){
                list=(ArrayList<Music>)intent.getSerializableExtra("list");
                position=intent.getIntExtra("position",0);
                setView();
                play_btn.setText("║");

                Intent intent_music_service=new Intent(this,MusicService.class);
                intent_music_service.setAction(MAIN_ACTION);
                intent_music_service.putExtra("list",list);
                intent_music_service.putExtra("position",position);
                startService(intent_music_service);
            }
            else if(intent.getAction().equals(MUSIC_PAUSE)){
                list=(ArrayList<Music>)intent.getSerializableExtra("list");
                position=intent.getIntExtra("position",0);
                setView();
                play_btn.setText("ᐅ");

                Intent intent_music_service=new Intent(this,MusicService.class);
                intent_music_service.setAction(MUSIC_PAUSE);
                intent_music_service.putExtra("list",list);
                intent_music_service.putExtra("position",position);
                startService(intent_music_service);
            }
            else if(intent.getAction().equals(MUSIC_PLAY)){
                list=(ArrayList<Music>)intent.getSerializableExtra("list");
                position=intent.getIntExtra("position",0);
                setView();
                play_btn.setText("║");

                Intent intent_music_service=new Intent(this,MusicService.class);
                intent_music_service.setAction(MUSIC_PLAY);
                intent_music_service.putExtra("list",list);
                intent_music_service.putExtra("position",position);
            }
        }

    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }
}
