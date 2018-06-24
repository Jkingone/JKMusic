package com.jkingone.jkmusic.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/9/11.
 */

public class Song implements Parcelable{
    private String song_id;
    private String title;
    private String author;
    private String pic_big;
    private String url;

    public Song(String song_id, String title, String author, String pic_big, String url) {
        this.song_id = song_id;
        this.title = title;
        this.author = author;
        this.pic_big = pic_big;
        this.url = url;
    }

    protected Song(Parcel in) {
        song_id = in.readString();
        title = in.readString();
        author = in.readString();
        pic_big = in.readString();
        url = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getSong_id() {
        return song_id;
    }

    public void setSong_id(String song_id) {
        this.song_id = song_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPic_big() {
        return pic_big;
    }

    public void setPic_big(String pic_big) {
        this.pic_big = pic_big;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Song{" +
                "song_id='" + song_id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", pic_big='" + pic_big + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(song_id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(pic_big);
        dest.writeString(url);
    }
}
