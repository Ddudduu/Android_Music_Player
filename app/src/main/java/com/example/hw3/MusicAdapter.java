package com.example.hw3;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MusicAdapter extends BaseAdapter {

    Context mContext = null;
    ArrayList<Music> mData = null;
    LayoutInflater mLayoutInflater = null;

    public MusicAdapter(Context context, ArrayList<Music> data){
        mContext=context;
        mData=data;
        mLayoutInflater=LayoutInflater.from(mContext);
    }

    public int getCount(){
        return mData.size();}

    public long getItemId(int position){
        return position;
    }

    public Music getItem(int position){
        return mData.get(position);
    }

    public void setAdapterList(ArrayList<Music> list){
        mData=list;
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemLayout=convertView;
        ViewHolder viewHolder=null;

        if(itemLayout==null){
            itemLayout=mLayoutInflater.inflate(R.layout.listview,null);

            viewHolder=new ViewHolder();
            viewHolder.title=(TextView)itemLayout.findViewById(R.id.title);
            viewHolder.album_cover=(ImageView)itemLayout.findViewById(R.id.albumCover);

            itemLayout.setTag(viewHolder);
        }
        else{
            viewHolder=(ViewHolder)itemLayout.getTag();
        }

        //title 불러오기
        viewHolder.title.setText(mData.get(position).title);

        //앨범커버 불러오기
        Uri no_cover=Uri.parse("content://media/external/audio/albumart/1");

        if(mData.get(position).getAlbum_cover().equals(no_cover.toString())==true){
            viewHolder.album_cover.setImageResource(R.drawable.default_albumart);
        }
        else {
            Uri img=Uri.parse(mData.get(position).getAlbum_cover());
            viewHolder.album_cover.setImageURI(img);

        }
        return itemLayout;
    }

    class ViewHolder{
        TextView title;
        ImageView album_cover;
    }
}
