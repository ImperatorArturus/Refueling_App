package com.example.refueling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refueling.extras.RecyclerViewAdapter;
import com.example.refueling.extras.Repostaje;
import com.example.refueling.extras.SplashScreen;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements RecyclerViewAdapter.OnRepostajeSeleccionado {

    private View itemView;
    private TextView titleHistory;
    private Boolean deleted = false;
    private Boolean updated = false;
    private Button btnExport;
    private String vehiculo;
    private FloatingActionButton btnAddRefueling;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adaptadorRecyclerRepostajes;
    private ArrayList<Repostaje> listaRepostajes = new ArrayList<>();
    private static final int CREATE_FILE_REQUEST_CODE = 123;
    private static final int PICK_FILE_REQUEST_CODE = 456;
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean isLongPress = false;

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    listarDatos();
                    updated = true;
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemView = getLayoutInflater().inflate(R.layout.activity_history, null);
        setContentView(itemView);
        instancias();
        listarDatos();
        acciones();
    }

    private void instancias() {

        Intent intent = getIntent();
        vehiculo = intent.getStringExtra("vehiculo");

        //Toast.makeText(HistoryActivity.this, "Selected : " + vehiculo, Toast.LENGTH_SHORT).show();


        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        titleHistory = findViewById(R.id.titleHistory);

        recyclerView = findViewById(R.id.recycler_refuel);
        recyclerView.setLayoutManager(layoutManager);
        adaptadorRecyclerRepostajes = new RecyclerViewAdapter(this, listaRepostajes);
        recyclerView.setAdapter(adaptadorRecyclerRepostajes);

        btnAddRefueling = findViewById(R.id.btnAddRefueling);
        btnExport = findViewById(R.id.btnExport);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void acciones() {

        titleHistory.setOnTouchListener(new View.OnTouchListener() {
            private long lastTouchTime = -1;
            private static final long DOUBLE_TAP_TIME_DELTA = 300; // Tiempo en milisegundos para considerar un doble toque

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    long touchTime = System.currentTimeMillis();
                    if (lastTouchTime != -1 && (touchTime - lastTouchTime) < DOUBLE_TAP_TIME_DELTA) {
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(HistoryActivity.this);
                        builder.setTitle("BORRADO DE DATOS");
                        builder.setMessage("¿Borrar todos los datos almacenados?");

                        // Agregar el botón "Aceptar"
                        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Obtener la lista de cadenas JSON almacenadas en SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove(vehiculo);
                                editor.apply();
                                Intent intent = new Intent(HistoryActivity.this, SplashScreen.class);
                                intent.putExtra("vehiculo", vehiculo);
                                startActivity(intent);

                            }
                        });

                        // Agregar el botón "Cancelar"
                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Acciones a realizar al hacer clic en "Cancelar"
                                // Puedes poner aquí el código que quieras ejecutar al hacer clic en "Cancelar"
                            }
                        });

                        // Mostrar el AlertDialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    lastTouchTime = touchTime;
                }
                return true; // Devuelve true para indicar que has gestionado el evento
            }
        });

        btnAddRefueling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Abre la pantalla de ingreso de repostaje
                Intent intent = new Intent(HistoryActivity.this, RepostajeActivity.class);
                intent.putExtra("vehiculo", vehiculo);
                someActivityResultLauncher.launch(intent);
            }
        });

        btnExport.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Inicia un temporizador para verificar si el botón se mantiene presionado durante un tiempo determinado
                    handler.postDelayed(longPressRunnable, 1000);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // Cancela el temporizador si se levanta el dedo o se cancela la acción
                    handler.removeCallbacks(longPressRunnable);
                    if (!isLongPress) {
                        // Si no se mantuvo presionado, realiza la acción normal (exportación)
                        exportDataToJson();
                    }
                }
                return true;
            }
        });

    }

    private void listarDatos() {
        // Obtener la lista de cadenas JSON almacenadas en SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
        String jsonSet = sharedPreferences.getString(vehiculo, "");

        if (!jsonSet.isEmpty()) {
            Type listType = new TypeToken<List<Repostaje>>() {
            }.getType();
            listaRepostajes = new Gson().fromJson(jsonSet, listType);
            ArrayList<Repostaje> listaSinPrimerElemento = new ArrayList<>(listaRepostajes);
            listaSinPrimerElemento.remove(0);
            adaptadorRecyclerRepostajes = new RecyclerViewAdapter(this, listaSinPrimerElemento);
            recyclerView.setAdapter(adaptadorRecyclerRepostajes);

        }

    }

    @Override
    public void onRepostajeSeleccionado(Repostaje repostaje, int position, String date, Boolean election, View v) {

        SharedPreferences sharedPreferences = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        int indexList = listaRepostajes.indexOf(repostaje);

        if (election==true){
            try{
                listaRepostajes.remove(indexList);

                String jsonActualizado = gson.toJson(listaRepostajes);
                editor.putString(vehiculo, jsonActualizado);
                editor.apply();

                Snackbar snackbar = Snackbar.make(itemView ,"Borrado " + date.toUpperCase(), Snackbar.LENGTH_SHORT);
                snackbar.setActionTextColor(getResources().getColor(R.color.red_light));
                snackbar.show();

                deleted = true;
                listarDatos();

            }catch (ClassCastException ex ){
                Toast.makeText(this, "No se ha podido eliminar el repostaje", Toast.LENGTH_SHORT).show();
            }
        }else{
            Intent intent = new Intent(getApplicationContext(), ActualizarRepostaje.class);
            intent.putExtra("repostajeActualizar",repostaje);
            someActivityResultLauncher.launch(intent);
        }

    }

    private void exportDataToJson() {
        // Convierte la lista a formato JSON usando la biblioteca Gson
        Gson gson = new Gson();
        String jsonData = gson.toJson(listaRepostajes);

        // Lanza la intención para permitir al usuario elegir la ubicación y el nombre del archivo
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "refueling_exported_data.json");
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Obtiene la URI del archivo seleccionado por el usuario
            Uri uri = data.getData();

            // Escribe los datos JSON en el archivo seleccionado
            writeJsonDataToFile(uri);

            Toast.makeText(this, "Datos exportados con éxito", Toast.LENGTH_SHORT).show();
        }
    }

    // Runnable para manejar la lógica del botón mantenido presionado
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            // Se ejecuta cuando el botón se ha mantenido presionado durante el tiempo especificado
            isLongPress = true;
            // Realiza la acción de importación
            importDataFromJson();
        }
    };

    private void importDataFromJson() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");  // Muestra todos los tipos de archivos
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    private void writeJsonDataToFile(Uri uri) {
        try {
            DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);

            // Abre un OutputStream para escribir en el archivo seleccionado por el usuario
            if (documentFile != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        outputStream.write(new Gson().toJson(listaRepostajes).getBytes());
                        outputStream.close();
                    }
                }
            }
        } catch (IOException e) {
            Log.e("ExportActivity", "Error al escribir el archivo JSON", e);
            Toast.makeText(this, "Error al exportar los datos", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        if (deleted == true || updated == true) {
            setResult(Activity.RESULT_OK);
        }else{
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
        super.onBackPressed();
    }
}
