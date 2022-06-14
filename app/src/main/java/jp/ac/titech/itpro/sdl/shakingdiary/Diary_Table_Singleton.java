package jp.ac.titech.itpro.sdl.shakingdiary;

import android.content.Context;
import androidx.room.Room;

public class Diary_Table_Singleton {
    private static Diary_Table instance = null;

    public static Diary_Table getInstance(Context context) {
        if (instance != null) {
            return instance;

        }
        instance = Room.databaseBuilder(context,
                Diary_Table.class, "diary_table").build();
        return instance;
    }
}