package com.example.myapplicationcursor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity implements ContaAdapter.OnContaChangeListener {

    private ArrayList<Conta> contas;
    private ContaAdapter adapter;
    private TextView txtTotalPendente;
    private TextView txtTotalPago;
    private TextView txtContasPendentes;
    private TextView txtContasPagas;
    private ContaDao contaDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa o banco de dados
        contaDao = AppDatabase.getInstance(this).contaDao();
        
        contas = new ArrayList<>(contaDao.getAll());

        txtTotalPendente = findViewById(R.id.txtTotalPendente);
        txtTotalPago = findViewById(R.id.txtTotalPago);
        txtContasPendentes = findViewById(R.id.txtContasPendentes);
        txtContasPagas = findViewById(R.id.txtContasPagas);

        RecyclerView recyclerView = findViewById(R.id.recyclerContas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContaAdapter(contas, this);
        recyclerView.setAdapter(adapter);

        ExtendedFloatingActionButton fab = findViewById(R.id.fabAddConta);
        fab.setOnClickListener(v -> showContaDialog(null, -1));

        // Configurar comportamento de scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fab.hide();
                } else {
                    fab.show();
                }
            }
        });

        atualizarTotais();
    }

    private void showContaDialog(Conta contaExistente, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_conta, null);

        TextInputEditText edtDescricao = view.findViewById(R.id.edtDescricao);
        TextInputEditText edtVencimento = view.findViewById(R.id.edtVencimento);
        TextInputEditText edtValor = view.findViewById(R.id.edtValor);

        // Configurar o campo de data
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

        edtVencimento.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view1, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    edtVencimento.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        if (contaExistente != null) {
            edtDescricao.setText(contaExistente.getDescricao());
            edtVencimento.setText(contaExistente.getVencimento());
            edtValor.setText(String.valueOf(contaExistente.getValor()));
            builder.setTitle("Editar Conta");
        } else {
            builder.setTitle("Nova Conta");
        }

        builder.setView(view)
                .setPositiveButton(contaExistente != null ? R.string.salvar : R.string.adicionar,
                        (dialog, which) -> {
                            String descricao = edtDescricao.getText().toString();
                            String vencimento = edtVencimento.getText().toString();
                            double valor = Double.parseDouble(edtValor.getText().toString());

                            if (contaExistente != null) {
                                contaExistente.setDescricao(descricao);
                                contaExistente.setVencimento(vencimento);
                                contaExistente.setValor(valor);
                                contaDao.update(contaExistente);
                                adapter.notifyItemChanged(position);
                            } else {
                                Conta novaConta = new Conta(descricao, vencimento, valor);
                                long id = contaDao.insert(novaConta);
                                novaConta.setId(id);
                                contas.add(novaConta);
                                adapter.notifyItemInserted(contas.size() - 1);
                            }
                            atualizarTotais();
                        })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    @Override
    public void onEditClick(Conta conta, int position) {
        showContaDialog(conta, position);
    }

    @Override
    public void onDeleteClick(Conta conta, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Conta")
                .setMessage("Deseja realmente excluir esta conta?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    contaDao.delete(conta);
                    contas.remove(position);
                    adapter.notifyItemRemoved(position);
                    atualizarTotais();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onContaPagaChanged(Conta conta, int position) {
        contaDao.update(conta);
        atualizarTotais();
    }

    private void atualizarTotais() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        // Calcular totais
        double totalPendente = contas.stream()
            .filter(conta -> !conta.isPaga())
            .mapToDouble(Conta::getValor)
            .sum();

        double totalPago = contas.stream()
            .filter(Conta::isPaga)
            .mapToDouble(Conta::getValor)
            .sum();

        long contasPendentes = contas.stream()
            .filter(conta -> !conta.isPaga())
            .count();

        long contasPagas = contas.stream()
            .filter(Conta::isPaga)
            .count();

        // Atualizar views
        txtTotalPendente.setText(currencyFormat.format(totalPendente));
        txtTotalPago.setText(currencyFormat.format(totalPago));
        txtContasPendentes.setText(contasPendentes + (contasPendentes == 1 ? " pendente" : " pendentes"));
        txtContasPagas.setText(contasPagas + (contasPagas == 1 ? " paga" : " pagas"));
    }
}