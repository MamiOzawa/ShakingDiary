package jp.ac.titech.itpro.sdl.shakingdiary;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity(primaryKeys = {"year","month","date"})
public class Diary_Entity {
    public Diary_Entity(int year, int month, int date, String text, Integer smile_score, Integer sad_score, Integer angry_score) {
        this.year= year;
        this.month = month;
        this.date = date;
        this.text = text;
        this.smile_score = smile_score;
        this.sad_score = sad_score;
        this.angry_score = angry_score;
    }

    @NonNull
    @ColumnInfo(name = "year")
    private int year;

    @NonNull
    @ColumnInfo(name = "month")
    private int month;

    @NonNull
    @ColumnInfo(name = "date")
    private int date;

    @Nullable
    @ColumnInfo(name = "text")
    private String text;

    @Nullable
    @ColumnInfo(name = "smile_score")
    private Integer smile_score;

    @Nullable
    @ColumnInfo(name = "sad_score")
    private Integer sad_score;

    @Nullable
    @ColumnInfo(name = "angry_score")
    private Integer angry_score;


    public void setAllData(int year, int month, int date, String text, int smile_score, int sad_score, int angry_score) {
        this.year= year;
        this.month = month;
        this.date = date;
        this.text = text;
        this.smile_score = smile_score;
        this.sad_score = sad_score;
        this.angry_score = angry_score;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDate() { return date; }
    public String getText() { return text; }
    public Integer getSmile_score() { return smile_score; }
    public Integer getSad_score() { return sad_score; }
    public Integer getAngry_score() { return angry_score; }
//    public String getAllinString(){ return "date:"+ date + "\ntext:" + text + "\nsmile:" + smile_score + "\nsad:" + sad_score + "\nangry:" + angry_score;}
}
