package com.example.myapplicationcursor;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Conta.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract ContaDao contaDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "contas_db")
                    .allowMainThreadQueries() // Apenas para exemplo, em produção use AsyncTask ou Coroutines
                    .build();
        }
        return instance;
    }
} 