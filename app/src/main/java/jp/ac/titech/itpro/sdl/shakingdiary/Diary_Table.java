package jp.ac.titech.itpro.sdl.shakingdiary;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Diary_Entity.class}, version = 1)
public abstract class Diary_Table extends RoomDatabase {
    public abstract Diary_Dao Diary_Dao();
}
