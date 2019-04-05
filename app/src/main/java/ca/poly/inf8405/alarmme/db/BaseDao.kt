package ca.poly.inf8405.alarmme.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {
  
  /**
   * Insert an object in the database
   *
   * @param obj the object to be inserted
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(obj: T)
  
  /**
   * Insert an array of objects in the database
   *
   * @param obj the objects to be inserted
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(vararg obj: T)
  
  /**
   * Insert a collection of objects in the database
   *
   * @param elements the list of objects to be inserted
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(elements: List<T>)
  
  /**
   * Update an object from the database
   *
   * @param obj the object to be updated
   */
  @Update(onConflict = OnConflictStrategy.REPLACE)
  fun update(obj: T)
  
  /**
   * Update an array of objects from the database
   *
   * @param obj the objects to be updated
   */
  @Update(onConflict = OnConflictStrategy.REPLACE)
  fun update(vararg obj: T)
  
  
  /**
   * Delete an object from the database
   *
   * @param obj the object to be deleted
   */
  @Delete
  fun delete(obj: T)
  
  /**
   * Delete an array of objects from the database
   *
   * @param obj the objects to be deleted
   */
  @Delete
  fun delete(vararg obj: T)
}