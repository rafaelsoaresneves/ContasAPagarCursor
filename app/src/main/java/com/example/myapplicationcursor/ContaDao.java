package com.example.myapplicationcursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContaDao {
    @Query("SELECT * FROM contas ORDER BY vencimento ASC")
    List<Conta> getAll();

    @Insert
    long insert(Conta conta);

    @Update
    void update(Conta conta);

    @Delete
    void delete(Conta conta);

    @Query("SELECT * FROM contas WHERE paga = 0")
    List<Conta> getContasPendentes();
} 