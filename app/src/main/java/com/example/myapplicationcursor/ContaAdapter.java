package com.example.myapplicationcursor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.material.button.MaterialButton;
import android.graphics.Color;
import com.google.android.material.checkbox.MaterialCheckBox;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ContaAdapter extends RecyclerView.Adapter<ContaAdapter.ContaViewHolder> {

    private List<Conta> contas;
    private OnContaClickListener listener;

    public ContaAdapter(List<Conta> contas, OnContaClickListener listener) {
        this.contas = contas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conta, parent, false);
        return new ContaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContaViewHolder holder, int position) {
        Conta conta = contas.get(position);
        holder.bind(conta);
    }

    @Override
    public int getItemCount() {
        return contas.size();
    }

    class ContaViewHolder extends RecyclerView.ViewHolder {
        private TextView txtDescricao;
        private TextView txtVencimento;
        private TextView txtValor;
        private MaterialButton btnEditar;
        private MaterialButton btnExcluir;
        private MaterialCheckBox checkPaga;

        public ContaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtVencimento = itemView.findViewById(R.id.txtVencimento);
            txtValor = itemView.findViewById(R.id.txtValor);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
            checkPaga = itemView.findViewById(R.id.checkPaga);
        }

        public void bind(Conta conta) {
            txtDescricao.setText(conta.getDescricao());
            txtVencimento.setText("Vencimento: " + conta.getVencimento());
            txtValor.setText(NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                    .format(conta.getValor()));
            checkPaga.setChecked(conta.isPaga());

            // Verifica se a conta está vencida
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            try {
                Date dataVencimento = sdf.parse(conta.getVencimento());
                Date hoje = new Date();
                
                ConstraintLayout container = itemView.findViewById(R.id.containerConta);
                if (conta.isPaga()) {
                    // Conta paga - fundo cinza
                    container.setBackgroundColor(Color.parseColor("#F5F5F5"));
                    txtDescricao.setPaintFlags(txtDescricao.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    txtVencimento.setTextColor(Color.parseColor("#757575"));
                    txtValor.setTextColor(Color.parseColor("#757575"));
                } else if (dataVencimento.before(hoje)) {
                    // Conta vencida - vermelho claro
                    container.setBackgroundColor(Color.parseColor("#FFEBEE"));
                    txtDescricao.setPaintFlags(txtDescricao.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    txtVencimento.setTextColor(Color.parseColor("#D32F2F"));
                    txtValor.setTextColor(Color.parseColor("#D32F2F"));
                } else {
                    // Conta não vencida - verde claro
                    container.setBackgroundColor(Color.parseColor("#E8F5E9"));
                    txtDescricao.setPaintFlags(txtDescricao.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    txtVencimento.setTextColor(Color.parseColor("#1B5E20"));
                    txtValor.setTextColor(Color.parseColor("#1B5E20"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            checkPaga.setOnCheckedChangeListener((buttonView, isChecked) -> {
                conta.setPaga(isChecked);
                bind(conta); // Atualiza a aparência do item
                if (listener instanceof OnContaChangeListener) {
                    ((OnContaChangeListener) listener).onContaPagaChanged(conta, getAdapterPosition());
                }
            });

            btnEditar.setOnClickListener(v -> listener.onEditClick(conta, getAdapterPosition()));
            btnExcluir.setOnClickListener(v -> listener.onDeleteClick(conta, getAdapterPosition()));
        }
    }

    public interface OnContaClickListener {
        void onEditClick(Conta conta, int position);
        void onDeleteClick(Conta conta, int position);
    }

    public interface OnContaChangeListener extends OnContaClickListener {
        void onContaPagaChanged(Conta conta, int position);
    }
} 