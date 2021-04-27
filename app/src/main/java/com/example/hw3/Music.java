package com.example.hw3;
import java.io.Serializable;
public class Music implements Serializable{

    public String title;
    public String album_cover;
    private String Datapath;
    private int duration;
    public int current_position;


    public Music(){
        //this.title=title;
        //this.album_cover=album_cover;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public String getAlbum_cover(){
        return album_cover;
    }

    public void setAlbum_cover(String album_cover){
        this.album_cover=album_cover;
    }

    public String getDataPath(){
        return Datapath;
    }

    public void setDatapath(String Datapath){
        this.Datapath=Datapath;
    }

    public int getDuration(){
        return duration;
    }

    public void setDuration(int duration){
        this.duration=duration;
    }

    public int getCurrentPosition(){
        return current_position;
    }

    public void setCurrent_position(int position){
        this.current_position=position;
    }


}
