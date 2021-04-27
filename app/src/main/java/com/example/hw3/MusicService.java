package com.example.hw3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnErrorListener{

    String path;
    ArrayList<Music> list=new ArrayList<>();;
    private int position;
    private int pause_position;
    public MediaPlayer mediaPlayer;
    boolean is_playing=false;
    int mCurPosition=0;
    Uri img_uri;

    public static String MAIN_ACTION="MAIN_ACTION";
    public static String MUSIC_PREV="MUSIC_PREV";
    public static String MUSIC_NEXT="MUSIC_NEXT";
    public static String MUSIC_PAUSE="MUSIC_PAUSE";
    public static String MUSIC_PLAY="MUSIC_PLAY;";

    public static String MUSIC_TOTAL_LENGTH="MUSIC_TOTAL_LENGTH";
    public static String MUSIC_COMPLETE="MUSIC_COMPLETE";

    RemoteViews remoteView;
    NotificationManagerCompat notificationManager;
    Notification customNotification;
    public static final String NOTIFICATION_CHANNEL_ID = "mychannel";
    NotificationCompat.Builder builder;
    public static final int notificationId=0711;
    NotificationChannel channel;

    Intent notificationIntent;
    PendingIntent notificationPendingIntent;

    IMusicService.Stub mBinder=new IMusicService.Stub() {
        @Override
        public int getCurrentPosition() throws RemoteException {
            return mCurPosition;
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    };

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("music service","onBind()");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer=new MediaPlayer();
        remoteView=new RemoteViews(getPackageName(),R.layout.remote_view);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d("Error : ",Integer.toString(what));
        Log.d("Extra : ",Integer.toString(extra));
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            if (intent.getAction().equals(MAIN_ACTION)) {

                position = intent.getExtras().getInt("position");
                Log.e("Music Service", "position : " + position);

                list = (ArrayList<Music>) intent.getSerializableExtra("list");
                Log.e("Music Service", "title : " + list.get(position).getTitle());

                play_Music(position);
                remoteView.setTextViewText(R.id.remote_play_btn, "║");

                updateRemoteview();
                Log.d("noti", "notinoti");



            } else if (intent.getAction().equals(MUSIC_PREV)) {
                if (position == 0) {
                    position = list.size() - 1;
                } else {
                    position = position - 1;
                }

                play_Music(position);
                Log.e("MUSIC_PREV", "prev");

                remoteView.setTextViewText(R.id.remote_play_btn, "║");
                updateRemoteview();

                Intent intent_next = new Intent(this, PlayMusicActivity.class);
                intent_next.setAction(MUSIC_PREV);
                intent_next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent_next.putExtra("position", position);
                intent_next.putExtra("list", list);
                startActivity(intent_next);

            } else if (intent.getAction().equals(MUSIC_NEXT)) {
                if (position == list.size() - 1) {
                    position = 0;
                } else {
                    position = position + 1;
                }

                play_Music(position);
                Log.e("MUSIC_NEXT", "next");

                remoteView.setTextViewText(R.id.remote_play_btn, "║");
                updateRemoteview();

                Intent intent_next = new Intent(this, PlayMusicActivity.class);
                intent_next.setAction(MUSIC_NEXT);
                intent_next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent_next.putExtra("position", position);
                intent_next.putExtra("list", list);
                startActivity(intent_next);

            } else if (intent.getAction().equals(MUSIC_PAUSE)) {
                if (is_playing) {
                    remoteView.setTextViewText(R.id.remote_play_btn, "ᐅ");
                    mediaPlayer.pause();
                    pause_position = mediaPlayer.getCurrentPosition();
                    is_playing = false;

                    //updateRemoteview();

                    Intent intent_next = new Intent(this, PlayMusicActivity.class);
                    intent_next.setAction(MUSIC_PAUSE);
                    intent_next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent_next.putExtra("position", position);
                    intent_next.putExtra("list", list);
                    startActivity(intent_next);
                }
                Log.e("MUSIC_PAUSE", "pause");

            } else if (intent.getAction().equals(MUSIC_PLAY)) {

                if (is_playing == false) {
                    mediaPlayer.seekTo(pause_position);
                    mediaPlayer.start();
                    is_playing = true;

                    remoteView.setTextViewText(R.id.remote_play_btn, "║");
                    //updateRemoteview();

                    Intent intent_next = new Intent(this, PlayMusicActivity.class);
                    intent_next.setAction(MUSIC_PLAY);
                    intent_next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent_next.putExtra("position", position);
                    intent_next.putExtra("list", list);
                    startActivity(intent_next);
                }
                Log.e("MUSIC_PLAY", "resume");

            } else {

            }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(is_playing){
                    mCurPosition=mediaPlayer.getCurrentPosition();

                    Log.i("service","cur position : "+mCurPosition);

                    try{Thread.sleep(1000);}
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

            }
        }).start();


showCustomLayoutNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Log.e("Music Service","onTaskRemoved");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("Music Service","onConfigurationChanged");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("Music Service","onUnbind");
        return super.onUnbind(intent);
    }

    private void play_Music(int mposition){
        mediaPlayer.reset();
        Log.e("reset","reset complete");

        if(list!=null){
            path=list.get(mposition).getDataPath();
        }
        Log.e("play","path : "+path);
        //mediaPlayer=MediaPlayer.create(this, Uri.parse(path));
        try {
            mediaPlayer.setDataSource(this,Uri.parse(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });
        mediaPlayer.prepareAsync();

        is_playing=true;

        Log.e("is_playing","true");

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (position == list.size() - 1) {
                    position = 0;
                } else {
                    position++;
                }

                Intent complete = new Intent(MusicService.this, PlayMusicActivity.class);
                complete.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                complete.putExtra("is_complete", "yes");
                startActivity(complete);

                play_Music(position);
                //updateRemoteview();

            }
        });

        mediaPlayer.setOnErrorListener(this);
    }

    private void showCustomLayoutNotification(){
        //remoteView=new RemoteViews(getPackageName(),R.layout.remote_view);
        createNotificationChannel();

        Intent nextIntent=new Intent(this,MusicService.class);
        nextIntent.setAction(MUSIC_NEXT);
        PendingIntent nextPendingIntent=PendingIntent.getService(this,0,nextIntent,0);

        Intent prevIntent=new Intent(this,MusicService.class);
        prevIntent.setAction(MUSIC_PREV);
        PendingIntent prevPendingIntent=PendingIntent.getService(this,0,prevIntent,0);

        Intent pauseIntent=new Intent(this,MusicService.class);
        pauseIntent.setAction(MUSIC_PAUSE);
        PendingIntent pausePendingIntent=PendingIntent.getService(this,0,pauseIntent,0);

        Intent playIntent=new Intent(this,MusicService.class);
        playIntent.setAction(MUSIC_PLAY);
        PendingIntent playPendingIntent=PendingIntent.getService(this,0,playIntent,0);

        Intent backIntent=new Intent(this,PlayMusicActivity.class);
        backIntent.setAction(Intent.ACTION_MAIN);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent backPendingIntent=PendingIntent.getActivity(this,0,backIntent,0);

        remoteView.setOnClickPendingIntent(R.id.remote_prev_btn,prevPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.remote_next_btn,nextPendingIntent);

        if(is_playing){
            remoteView.setOnClickPendingIntent(R.id.remote_play_btn,pausePendingIntent);
        }
        else{
            remoteView.setOnClickPendingIntent(R.id.remote_play_btn,playPendingIntent);
        }
        remoteView.setOnClickPendingIntent(R.id.remote_album_cover,backPendingIntent);

        Uri img=Uri.parse(list.get(position).getAlbum_cover());
        Uri no_cover=Uri.parse("content://media/external/audio/albumart/1");

        if(img.toString().equals(no_cover.toString())==true){
            remoteView.setImageViewResource(R.id.remote_album_cover,R.drawable.default_albumart);
        }
        else{
            remoteView.setImageViewUri(R.id.remote_album_cover,Uri.parse(list.get(position).getAlbum_cover()));
        }

        remoteView.setTextViewText(R.id.remote_title,list.get(position).getTitle());

        notificationIntent=new Intent(this,PlayMusicActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationPendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);

        customNotification = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.noti_play)
                .setCustomContentView(remoteView)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setNotificationSilent()
                .setAutoCancel(true)
                .build();

        startForeground(notificationId,customNotification);
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "music notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);

            notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateRemoteview(){
        remoteView.setImageViewUri(R.id.remote_album_cover,Uri.parse(list.get(position).getAlbum_cover()));
        remoteView.setTextViewText(R.id.remote_title,list.get(position).getTitle());

        Log.d("updateremoteview","position : "+position);

    }
}
