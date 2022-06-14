package jp.ac.titech.itpro.sdl.shakingdiary;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface Diary_Dao {

    @Query("SELECT * FROM Diary_Entity WHERE year = :year AND month = :month")
    List<Diary_Entity> getAll_by_year_and_month(int year, int month);

    @Query("SELECT * FROM Diary_Entity WHERE year = :year AND month = :month AND date = :date")
    Diary_Entity get_by_date(int year, int month, int date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert_or_update(Diary_Entity diary_entity);

    @Query("DELETE FROM Diary_Entity WHERE year = :year AND month = :month AND date = :date")
    void delete(int year, int month, int date);
}