package com.example.refueling.extras;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.refueling.MainActivity;
import com.example.refueling.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SplashScreen extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 456;
    private TextInputLayout txtLayoutVehiculo, txtLayoutKM, txtLayoutEuro;
    private EditText editTextVehiculo, editTextKM, editTextEuro;
    private Button btnAccept, btnImportData;
    private String vehiculo = "Honda CBF 125";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        instancias();
        acciones();

    }

    private void instancias() {

        txtLayoutVehiculo = findViewById(R.id.txtLayouVehiculo);
        txtLayoutKM = findViewById(R.id.txtLayoutKM);
        txtLayoutEuro = findViewById(R.id.txtLayoutEuro);

        editTextVehiculo = txtLayoutVehiculo.getEditText();
        editTextKM = txtLayoutKM.getEditText();
        editTextEuro = txtLayoutEuro.getEditText();

        btnAccept = findViewById(R.id.btnAccept);
        btnImportData = findViewById(R.id.importData);

    }

    private void acciones(){

        Intent intent = getIntent();
        String nombreVehiculo = intent.getStringExtra("vehiculo");
        if(nombreVehiculo != null){
            vehiculo = nombreVehiculo;
        }

        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String jsonRepostajes = sharedPreferences.getString(vehiculo, null);
        Type listType = new TypeToken<List<Repostaje>>() {}.getType();
        List<Repostaje> listaRepostajes;

        if (jsonRepostajes != null) {
            listaRepostajes = gson.fromJson(jsonRepostajes, listType);

            if (listaRepostajes.size()>0){
                Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent2);
                finish();
            }

        }else{

            listaRepostajes = new ArrayList<>();
        }

        editTextVehiculo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                txtLayoutVehiculo.setErrorEnabled(false);
            }
        });
        editTextKM.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                txtLayoutKM.setErrorEnabled(false);
            }
        });

        editTextEuro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                txtLayoutEuro.setErrorEnabled(false);
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String inputVehiculo = editTextVehiculo.getText().toString();
                String inputKMString = editTextKM.getText().toString();
                int inputKM = Integer.parseInt(inputKMString);
                String inputEuro = editTextEuro.getText().toString();
                // Obtén la fecha actual
                Date fechaActual = new Date();

                // Define el formato deseado para la fecha
                SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                String fechaFormateada = formatoFecha.format(fechaActual);
                // Fuerzo la fecha de creacion para que sea 1 de Enero de 1970
                fechaFormateada = "1 ene 1970";


                System.out.println(fechaFormateada);

                if (!inputVehiculo.isEmpty() && !inputKMString.isEmpty() && !inputEuro.isEmpty()) {

                    // DE MOMENTO NO HACE NADA CON EL STRING VEHICULO. PASAR DESPUES PARA HACER NUEVO APARTADO EN SHAREDPREFERENCES PARA DIFERENCIAR VEHICULOS
                    Repostaje repostaje = new Repostaje(UUID.randomUUID().toString(), fechaFormateada, inputKM, inputEuro, "");
                    listaRepostajes.add(repostaje);
                    String jsonActualizado = gson.toJson(listaRepostajes);
                    editor.putString(vehiculo, jsonActualizado);
                    editor.apply();

                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (inputVehiculo.isEmpty()){
                    txtLayoutVehiculo.setError("Ingresa el nombre del vehiculo");
                } else if (inputKMString.isEmpty()) {
                    txtLayoutKM.setError("Ingresa numero de kilometros");
                } else if (inputEuro.isEmpty()) {
                    txtLayoutEuro.setError("Ingresa un gasto");
                }
            }
        });

        btnImportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importDataFromJson();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            readJsonDataFromFile(uri);
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void importDataFromJson() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");  // Muestra todos los tipos de archivos
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    private void readJsonDataFromFile(Uri uri) {
        try {
            // Abre un InputStream para leer el archivo seleccionado por el usuario
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String jsonData = stringBuilder.toString();

                Gson gson = new Gson();
                SharedPreferences sharedPreferences = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(vehiculo, jsonData);
                editor.apply();
                // Notifica al usuario sobre la importación exitosa
                Toast.makeText(this, "Datos importados con éxito", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("ImportExportActivity", "Error al leer el archivo JSON", e);
            Toast.makeText(this, "Error al importar los datos", Toast.LENGTH_SHORT).show();
        }
    }
}
