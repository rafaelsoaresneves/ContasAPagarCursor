package com.example.myapplicationcursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContaDao {
    @Query("SELECT * FROM conta ORDER BY vencimento ASC")
    List<Conta> getAll();

    @Query("SELECT * FROM conta WHERE paga = 0 ORDER BY vencimento ASC")
    List<Conta> getContasPendentes();

    @Query("SELECT * FROM conta WHERE paga = 1 ORDER BY vencimento ASC")
    List<Conta> getContasPagas();

    @Query("SELECT * FROM conta WHERE descricao LIKE '%' || :termo || '%' ORDER BY vencimento ASC")
    List<Conta> buscarPorDescricao(String termo);

    @Query("SELECT * FROM conta WHERE descricao LIKE '%' || :termo || '%' AND paga = :paga ORDER BY vencimento ASC")
    List<Conta> buscarPorDescricaoEStatus(String termo, boolean paga);

    @Insert
    long insert(Conta conta);

    @Update
    void update(Conta conta);

    @Delete
    void delete(Conta conta);
} 