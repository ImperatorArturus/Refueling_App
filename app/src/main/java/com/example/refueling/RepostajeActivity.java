package com.example.refueling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.refueling.extras.Repostaje;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RepostajeActivity extends AppCompatActivity {
    private EditText editTextKilometers, editTextLitres, editTextEuros;
    private MaterialDatePicker materialDatePicker;
    private String vehiculo;
    private Button mPickDateButton, buttonSaveRefueling;
    int km1, km2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refueling);
        instancias();
        acciones();

    }

    private void acciones() {

        mPickDateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
                    }
                });

        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {

                        mPickDateButton.setText(materialDatePicker.getHeaderText());
                    }
                });
        editTextLitres.setOnEditorActionListener(new NextActionListener(editTextLitres));

        buttonSaveRefueling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarRepostaje();
            }
        });
    }

    private void registrarRepostaje(){

        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String date = mPickDateButton.getText().toString();
        String kilometres = editTextKilometers.getText().toString();
        String euros = editTextEuros.getText().toString();
        String litros = editTextLitres.getText().toString();


        //Comprobamos que los datos introducidos no esten vacios
        if (!kilometres.equals("") && !date.equals("") && !date.equals("Select Date") && !euros.equals("") && !litros.equals("")){
            String jsonRepostajes = sharedPreferences.getString(vehiculo, null);
            Type listType = new TypeToken<List<Repostaje>>() {}.getType();
            List<Repostaje> listaRepostajes;

            if (jsonRepostajes != null) {
                listaRepostajes = gson.fromJson(jsonRepostajes, listType);
            } else {
                // Si la lista no existe, crea una nueva lista vacía
                listaRepostajes = new ArrayList<>();
            }

            Repostaje rUltimo = listaRepostajes.get(listaRepostajes.size() - 1);
            km1 = Integer.parseInt(rUltimo.getKm());
            km2 = Integer.parseInt(kilometres);

            if (km2>km1) {

                Repostaje repostaje = new Repostaje(UUID.randomUUID().toString(), date, kilometres, euros, litros);

                listaRepostajes.add(repostaje);

                // Convierte la lista actualizada a JSON
                String jsonActualizado = gson.toJson(listaRepostajes);

                editor.putString(vehiculo, jsonActualizado);
                editor.apply();
                Toast.makeText(getApplicationContext(), "Repostaje agregado", Toast.LENGTH_SHORT).show();
                //setResult(Activity.RESULT_OK);
                finish();

            } else {
                Toast.makeText(this, "Los km no pueden ser menos de "+km1+" Km", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Introduzca todos los datos", Toast.LENGTH_SHORT).show();
        }
    }

    private class DecimalTextWatcher implements TextWatcher {

        private EditText editText2;

        public DecimalTextWatcher(EditText editText) {
            this.editText2 = editText;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            // No es necesario realizar acciones antes de cambiar el texto
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // No es necesario realizar acciones mientras se cambia el texto
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // Reemplazar la coma por un punto en el texto
            String text = editable.toString();
            if (text.contains(",")) {
                text = text.replace(",", ".");
                editText2.setText(text);
                editText2.setSelection(text.length());
            }
        }
    }

    private class NextActionListener implements TextView.OnEditorActionListener {
        private EditText nextEditText;

        public NextActionListener(EditText nextEditText) {
            this.nextEditText = nextEditText;
        }

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                buttonSaveRefueling.performClick();

                // Ejemplo: Mostrar un mensaje
                Toast.makeText(RepostajeActivity.this, "Presionaste Siguiente", Toast.LENGTH_SHORT).show();

                // Devuelve true para indicar que la acción se ha manejado
                return true;
            }
            return false; // Devuelve false para que se maneje de manera predeterminada
        }
    }

    //Instanciamos las variables
    private void instancias() {
        Intent intent = getIntent();
        vehiculo = intent.getStringExtra("vehiculo");



        editTextKilometers = findViewById(R.id.editTextKm);
        mPickDateButton = findViewById(R.id.pick_date_button);
        editTextEuros = findViewById(R.id.edit_euros);
        editTextLitres = findViewById(R.id.edit_litres);
        editTextEuros.addTextChangedListener(new DecimalTextWatcher(editTextEuros));
        editTextLitres.addTextChangedListener(new DecimalTextWatcher(editTextLitres));


        Calendar calendar = Calendar.getInstance();
        long currentDate = calendar.getTimeInMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(currentDate));
        mPickDateButton.setText(String.valueOf(formattedDate));

        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.datePicker()
                .setSelection(currentDate);
        materialDatePicker = materialDateBuilder.build();


        buttonSaveRefueling = findViewById(R.id.buttonSaveRefueling);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                editTextKilometers.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editTextKilometers, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 250);

    }
}
