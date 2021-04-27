package com.example.hw3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String[] permission_list={
            android.Manifest.permission.MEDIA_CONTENT_CONTROL, android.Manifest.permission.READ_EXTERNAL_STORAGE};

    ListView mListView=null;
    MusicAdapter adapter;
    transient ArrayList<Music> list = new ArrayList<>();
    String sdPath;
    String ext= Environment.getExternalStorageState();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sdPath= Environment.getExternalStorageDirectory().getAbsolutePath();

        init();

        if(ext.equals(Environment.MEDIA_MOUNTED)==false){
            Toast.makeText(this,"SD 카드가 반드시 필요합니다.",Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkPermission();
        loadAudio();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private void init(){
        adapter=new MusicAdapter(this,list);

        mListView=(ListView)findViewById(R.id.list_view);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Listen_music(position);
            }
        });
    }

    public void checkPermission(){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M)
            return;

        for(String permission : permission_list){
            int check = checkCallingOrSelfPermission(permission);

            if(check== PackageManager.PERMISSION_DENIED){
                requestPermissions(permission_list,0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        if(requestCode==0){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){

                }
                else{
                    Toast.makeText(getApplicationContext(),"앱 권한 설정하세요",Toast.LENGTH_LONG);
                    //finish();
                }
            }
        }
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sort = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(uri, null, selection, null, null);
        cursor.moveToFirst();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Music music = new Music();

                int album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String title_prev = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));


                String data_path=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                //Toast.makeText(this,cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),Toast.LENGTH_LONG).show();
                music.setDatapath(data_path);

                if(title_prev.contains("+")){
                    String[] title_array1=title_prev.split("\\+-\\+");
                    String title2=title_array1[1].replaceAll("\\+"," ");
                    String[] title_array2=title2.split("\\[NCS Release]");

                    music.setTitle(title_array2[0]+".mp3");
                }
                else{
                    music.setTitle(title_prev+".mp3");
                }

                Uri sArtworkUri=Uri.parse("content://media/external/audio/albumart");
                Uri uri_id= ContentUris.withAppendedId(sArtworkUri,album_id);

                music.setAlbum_cover(uri_id.toString());
                music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                list.add(music);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setAdapterList(list);
                    }
                });

            }
        }
        cursor.close();
    }


    public void Listen_music(int position){
        Intent intent=new Intent(MainActivity.this, PlayMusicActivity.class);

        intent.putExtra("music_list",list);
        intent.putExtra("position",position);
        startActivity(intent);
    }
}
