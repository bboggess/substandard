package com.example.substandard.database.data;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.List;

/**
 * Base class that prevents copying the same delete, insert, etc for
 * every single table.
 *
 * @param <T> Type that is stored in the table
 */
public interface BaseDao<T> {

    @Delete
    void delete(T obj);

    @Insert
    void insert(T obj);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<T> objs);

    @Update
    void update(List<T> objs);
}
