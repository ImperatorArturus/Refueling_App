package com.example.refueling.extras;

import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refueling.ActualizarRepostaje;
import com.example.refueling.HistoryActivity;
import com.example.refueling.MainActivity;
import com.example.refueling.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Repostaje> listaRepostajes;

    OnRepostajeSeleccionado abrirRepostaje;



    public RecyclerViewAdapter(Context context, ArrayList<Repostaje> listaRepostajes) {
        this.context = context;
        this.listaRepostajes = listaRepostajes;

        try{
            abrirRepostaje = (RecyclerViewAdapter.OnRepostajeSeleccionado) context;
        }catch (ClassCastException ex){

        }
    }

    //Creamos la forma que va a tener cada elemento de la lista
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_refuel, parent, false);

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {

        final Repostaje repostaje = listaRepostajes.get(position);
        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            holder.gastoTxt.setTextColor(Color.WHITE);

        }
        holder.fechaTextView.setText(repostaje.getFecha().toUpperCase());
        holder.gastoTxt.setText(String.format("%.2f",Double.parseDouble(repostaje.getPrecio())) + " €");
        DecimalFormat decimalFormat = new DecimalFormat("00.00");
        holder.litrosTxt.setText(decimalFormat.format(Double.parseDouble(repostaje.getLitros())) + " Litros");
        //holder.litrosTxt.setText(String.format("%.2f",Double.parseDouble(repostaje.getLitros())) + " Litros");
        holder.kmTxt.setText(repostaje.getKm() + " Km");
        String fecha = repostaje.getFecha();
        holder.posicion.setText(String.valueOf(position+1)+".");

        holder.getLayout_refuel().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog(repostaje, holder.getAdapterPosition(), fecha);
            }
        });

    }


    @Override
    public int getItemCount() {
        return listaRepostajes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fechaTextView, litrosTxt, gastoTxt, kmTxt, posicion;
        LinearLayout layout_refuel;

        public ViewHolder(View itemView) {
            super(itemView);
            litrosTxt = itemView.findViewById(R.id.litros);
            gastoTxt = itemView.findViewById(R.id.gasto);
            fechaTextView = itemView.findViewById(R.id.refuelDate);
            layout_refuel = itemView.findViewById(R.id.layout_refuel);
            kmTxt = itemView.findViewById(R.id.kmTotales);
            posicion = itemView.findViewById(R.id.numRepostaje);
        }

        public TextView getFechaTextView() {
            return fechaTextView;
        }

        public TextView getLitrosTxt() {
            return litrosTxt;
        }

        public TextView getGastoTxt() {
            return gastoTxt;
        }


        public LinearLayout getLayout_refuel() {
            return layout_refuel;
        }
    }

    private void showBottomSheetDialog(Repostaje repostaje, int position, String date) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);

        TextView fechaSheet = bottomSheetDialog.findViewById(R.id.fechaSheet);
        fechaSheet.setText(repostaje.getFecha().toUpperCase());

        LinearLayout edit = bottomSheetDialog.findViewById(R.id.edit);
        LinearLayout delete = bottomSheetDialog.findViewById(R.id.delete);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirRepostaje.onRepostajeSeleccionado(repostaje, position, date, false, v);
                bottomSheetDialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un AlertDialog.Builder
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                builder.setTitle("Borrar " + date.toUpperCase())
                        .setMessage("¿Borrar respotaje seleccionado?")
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                bottomSheetDialog.dismiss();
                                abrirRepostaje.onRepostajeSeleccionado(repostaje, position, date, true, v);
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                // Crea el AlertDialog y lo muestra
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                //bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    public interface OnRepostajeSeleccionado {
        void onRepostajeSeleccionado(Repostaje repostaje, int position, String fecha, Boolean election, View view);
    }
}

