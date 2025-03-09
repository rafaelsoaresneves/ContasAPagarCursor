package com.example.myapplicationcursor;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contas")
public class Conta {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String descricao;
    private String vencimento;
    private double valor;
    private boolean paga;

    public Conta(String descricao, String vencimento, double valor) {
        this.descricao = descricao;
        this.vencimento = vencimento;
        this.valor = valor;
        this.paga = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getVencimento() {
        return vencimento;
    }

    public void setVencimento(String vencimento) {
        this.vencimento = vencimento;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isPaga() {
        return paga;
    }

    public void setPaga(boolean paga) {
        this.paga = paga;
    }
} 