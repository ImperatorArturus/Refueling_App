package com.example.refueling;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.refueling.extras.Repostaje;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ActualizarRepostaje extends AppCompatActivity {

    private Button btnUpdate;
    private EditText txtKm, txtImporte, txtLitros;
    private MaterialTextView txtFecha;
    long milisecs;
    private String vehiculo;
    MaterialDatePicker.Builder<Long> materialDateBuilder;
    MaterialDatePicker<Long> materialDatePicker;
    Repostaje repostaje = null;
    TextWatcher textWatcher;
    private boolean camposModificados = false;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_repostaje);

        instancias();
        recuperarRepostaje();
        acciones();
    }

    private void instancias() {

        btnUpdate = findViewById(R.id.btnActualizarEquipo);
        txtFecha = findViewById(R.id.txtFecha);
        txtKm = findViewById(R.id.actualizarKm);
        txtImporte = findViewById(R.id.actualizarImporte);
        txtLitros = findViewById(R.id.actualizarLitros);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No es necesario implementar esto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                camposModificados = true; // Establece la bandera si algún campo ha sido modificado
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No es necesario implementar esto
            }
        };
    }

    private void acciones() {

        txtFecha.addTextChangedListener(textWatcher);
        txtKm.addTextChangedListener(textWatcher);
        txtImporte.addTextChangedListener(textWatcher);
        txtLitros.addTextChangedListener(textWatcher);


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actualizacion();
            }
        });

        txtFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(), materialDatePicker.toString());
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (camposModificados == true) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActualizarRepostaje.this);
                    builder.setTitle("");
                    builder.setMessage("¿Cancelar actualizacion?");

                    // Agregar el botón "Aceptar"
                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();

                        }
                    });

                    // Agregar el botón "Cancelar"
                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    // Mostrar el AlertDialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    finish();
                }
            }
        });

    }

    private void actualizacion(){

        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String idR = repostaje.getId_r();
        String date = txtFecha.getText().toString();
        String kilometresString = txtKm.getText().toString();
        int kilometres = Integer.parseInt(kilometresString);
        String euros = txtImporte.getText().toString();
        String litros = txtLitros.getText().toString();

        String jsonRepostajes = sharedPreferences.getString(vehiculo, null);
        Type listType = new TypeToken<List<Repostaje>>() {}.getType();
        List<Repostaje> listaRepostajes;

        Repostaje repostajeActualizado = new Repostaje(idR, date, kilometres, euros, litros);

        listaRepostajes = gson.fromJson(jsonRepostajes, listType);
        if (listaRepostajes != null) {
            Log.d("DEBUG", "Tamaño de la lista: " + listaRepostajes.size()); // Registro para verificar el tamaño de la lista
            int pLista = positionInList(listaRepostajes, idR);
            if (pLista != -1){
                //Toast.makeText(getApplicationContext(), "Encontre el elemento en la lista : " + pLista, Toast.LENGTH_SHORT).show();
                listaRepostajes.set(pLista, repostajeActualizado);
                String jsonActualizado = gson.toJson(listaRepostajes);
                editor.putString(vehiculo, jsonActualizado);
                editor.apply();
                setResult(Activity.RESULT_OK);
                finish();
            }

        }else {
            Log.e("ERROR", "La lista de repostajes es nula"); // Registro para indicar que la lista es nula
            // Mostrar mensaje de error
            }


    }

    private void recuperarRepostaje() {
        Intent intent = getIntent();
        vehiculo = intent.getStringExtra("vehiculo");
        repostaje = (Repostaje) getIntent().getExtras().get("repostajeActualizar");
        txtFecha.setText(repostaje.getFecha());
        txtKm.setText(String.valueOf(repostaje.getKm()));
        txtImporte.setText(repostaje.getPrecio());
        txtLitros.setText(repostaje.getLitros());

        milisecs = conversorFecha(txtFecha.getText().toString());
        materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setSelection(milisecs);
        materialDatePicker = materialDateBuilder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            txtFecha.setText(materialDatePicker.getHeaderText());
        });
    }

    public int positionInList(List<Repostaje> listaRepostajes, String idBuscado) {
        int posicion = -1;

        for (int i = 0; i < listaRepostajes.size(); i++) {
            Repostaje repostaje = listaRepostajes.get(i);
            if (repostaje.getId_r().equals(idBuscado)) {
                posicion = i;
                break;
            }
        }

        return posicion;
    }

    private long conversorFecha(String fechaString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

        Date fecha = null;

        try {
            fecha = sdf.parse(fechaString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long fechaMili = (fecha != null) ? fecha.getTime() : System.currentTimeMillis();
        fechaMili = fechaMili + 43200000;

        return fechaMili;
    }

    public int isModoOscuro(@NonNull Context context) {

        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {

        }
        return currentNightMode;
    }

}
