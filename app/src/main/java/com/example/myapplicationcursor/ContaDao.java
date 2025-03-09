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

    @Query("SELECT * FROM contas WHERE descricao LIKE '%' || :termo || '%' ORDER BY vencimento ASC")
    List<Conta> buscarPorDescricao(String termo);

    @Query("SELECT * FROM contas WHERE descricao LIKE '%' || :termo || '%' AND paga = :paga ORDER BY vencimento ASC")
    List<Conta> buscarPorDescricaoEStatus(String termo, boolean paga);
} 