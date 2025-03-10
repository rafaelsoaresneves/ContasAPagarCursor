package com.example.myapplicationcursor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ContaAdapter.OnContaChangeListener {

    private ArrayList<Conta> contas;
    private ContaAdapter adapter;
    private TextView txtTotalPendente;
    private TextView txtTotalPago;
    private TextView txtContasPendentes;
    private TextView txtContasPagas;
    private ContaDao contaDao;
    private AlertDialog dialogFiltro;
    private String termoBusca = "";
    private int filtroStatus = 0; // 0 = todas, 1 = pendentes, 2 = pagas
    private MaterialButton btnFiltrar;

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

        btnFiltrar = findViewById(R.id.btnFiltrar);
        btnFiltrar.setOnClickListener(v -> showFiltroDialog());
        atualizarTextoBotaoFiltro();

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

    private void showFiltroDialog() {
        if (dialogFiltro == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_filtro, null);
            
            TextInputEditText edtPesquisa = view.findViewById(R.id.edtPesquisa);
            ChipGroup chipGroupStatus = view.findViewById(R.id.chipGroupStatus);
            
            // Restaurar estado anterior
            edtPesquisa.setText(termoBusca);
            switch (filtroStatus) {
                case 1:
                    chipGroupStatus.check(R.id.chipPendentes);
                    break;
                case 2:
                    chipGroupStatus.check(R.id.chipPagas);
                    break;
                default:
                    chipGroupStatus.check(R.id.chipTodas);
            }

            // Listener para pesquisa em tempo real
            edtPesquisa.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    termoBusca = s.toString();
                    aplicarFiltros();
                }
            });

            // Listener para mudança de status
            chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.chipPendentes) {
                    filtroStatus = 1;
                } else if (checkedId == R.id.chipPagas) {
                    filtroStatus = 2;
                } else {
                    filtroStatus = 0;
                }
                aplicarFiltros();
                atualizarTextoBotaoFiltro();
            });

            builder.setTitle(R.string.filtrar_contas)
                   .setView(view)
                   .setPositiveButton(R.string.fechar, (dialog, which) -> {
                       // Atualiza novamente ao fechar para garantir que os filtros foram aplicados
                       aplicarFiltros();
                       atualizarTextoBotaoFiltro();
                   })
                   .setNeutralButton(R.string.limpar_filtros, (dialog, which) -> {
                       termoBusca = "";
                       filtroStatus = 0;
                       edtPesquisa.setText("");
                       chipGroupStatus.check(R.id.chipTodas);
                       aplicarFiltros();
                       atualizarTextoBotaoFiltro();
                   });

            dialogFiltro = builder.create();
        }
        dialogFiltro.show();
    }

    private void aplicarFiltros() {
        List<Conta> contasFiltradas;
        
        try {
            if (termoBusca.isEmpty()) {
                // Se não há termo de busca, filtra apenas por status
                switch (filtroStatus) {
                    case 1: // Pendentes
                        contasFiltradas = contaDao.getContasPendentes();
                        break;
                    case 2: // Pagas
                        contasFiltradas = contaDao.getContasPagas();
                        break;
                    default: // Todas
                        contasFiltradas = contaDao.getAll();
                        break;
                }
            } else {
                // Se há termo de busca, combina com o status
                if (filtroStatus == 1) {
                    contasFiltradas = contaDao.buscarPorDescricaoEStatus(termoBusca, false);
                } else if (filtroStatus == 2) {
                    contasFiltradas = contaDao.buscarPorDescricaoEStatus(termoBusca, true);
                } else {
                    contasFiltradas = contaDao.buscarPorDescricao(termoBusca);
                }
            }

            contas.clear();
            contas.addAll(contasFiltradas);
            adapter.notifyDataSetChanged();
            atualizarTotais();
            
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Erro ao aplicar filtros: " + e.getMessage())
                .setPositiveButton("OK", null)
                .show();
        }
    }

    private void atualizarTextoBotaoFiltro() {
        String statusText;
        switch (filtroStatus) {
            case 1:
                statusText = getString(R.string.pendentes);
                btnFiltrar.setIconTint(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
                btnFiltrar.setTextColor(Color.parseColor("#D32F2F"));
                btnFiltrar.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
                break;
            case 2:
                statusText = getString(R.string.pagas);
                btnFiltrar.setIconTint(ColorStateList.valueOf(Color.parseColor("#1B5E20")));
                btnFiltrar.setTextColor(Color.parseColor("#1B5E20"));
                btnFiltrar.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#1B5E20")));
                break;
            default:
                statusText = getString(R.string.filtrar);
                btnFiltrar.setIconTint(ColorStateList.valueOf(Color.parseColor("#757575")));
                btnFiltrar.setTextColor(Color.parseColor("#757575"));
                btnFiltrar.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#757575")));
        }

        if (!termoBusca.isEmpty()) {
            statusText += " (" + termoBusca + ")";
        }
        
        btnFiltrar.setText(statusText);
    }
}