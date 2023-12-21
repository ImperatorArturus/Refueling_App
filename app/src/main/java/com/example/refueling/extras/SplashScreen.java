package com.example.refueling.extras;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SplashScreen extends AppCompatActivity {

    private TextInputLayout txtLayout, txtLayout2;
    private Button buttonShowSnackbar, btnImportData;
    private String vehiculo = "Honda CBF 125";

    private List<String> listaRepost;
    private static final int PICK_FILE_REQUEST_CODE = 456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

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


        txtLayout = findViewById(R.id.textInputLayout);
        txtLayout2 = findViewById(R.id.txtLayout2);
        buttonShowSnackbar = findViewById(R.id.buttonShowSnackbar);
        btnImportData = findViewById(R.id.importData);

        btnImportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importDataFromJson();

            }
        });

        if (jsonRepostajes != null) {
            listaRepostajes = gson.fromJson(jsonRepostajes, listType);
            //Toast.makeText(this, String.valueOf(listaRepostajes.size()), Toast.LENGTH_SHORT).show();


            if (listaRepostajes.size()==0){
                buttonShowSnackbar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String input1 = txtLayout.getEditText().getText().toString();
                        String input2 = txtLayout2.getEditText().getText().toString();

                        if (!input1.isEmpty() && !input2.isEmpty()) {
                            Repostaje repostaje = new Repostaje(UUID.randomUUID().toString(), "", input1, input2, "");
                            listaRepostajes.add(repostaje);
                            String jsonActualizado = gson.toJson(listaRepostajes);
                            editor.putString(vehiculo, jsonActualizado);
                            editor.apply();

                            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (input1.isEmpty()) {
                            txtLayout.setError("Ingresa un gasto para seguir");
                        } else if (input2.isEmpty()) {
                            txtLayout2.setError("Ingresa kms para seguir");
                        }
                    }
                });



            }else {
                Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent2);
                finish();
            }

        }else{

            listaRepostajes = new ArrayList<>();

            buttonShowSnackbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String input1 = txtLayout.getEditText().getText().toString();
                    String input2 = txtLayout2.getEditText().getText().toString();
                    if (!input1.isEmpty() && !input2.isEmpty()) {
                        Repostaje repostaje = new Repostaje(UUID.randomUUID().toString(), "", input1, input2, "");
                        listaRepostajes.add(repostaje);
                        String jsonActualizado = gson.toJson(listaRepostajes);

                        editor.putString(vehiculo, jsonActualizado);
                        editor.apply();

                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else if (input1.isEmpty()) {
                        txtLayout.setError("Ingresa kms para seguir");
                    } else if (input2.isEmpty()) {
                        txtLayout2.setError("Ingresa gasto para seguir");
                    }
                }
            });


        }
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

                // Convierte el JSON a una lista de datos (ajusta esto según tu lógica)
                //List<String> importedData = new Gson().fromJson(jsonData, List.class);
                // Haz algo con los datos importados (por ejemplo, actualizar la lista actual)
                //listaRepost.clear();
                //listaRepost.addAll(importedData);
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
