package com.example.refueling;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.refueling.extras.Repostaje;
import com.example.refueling.extras.SplashScreen;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView editTitle, editTotal, editKm, editKmTotales, editGasto, editGastoTotal, editConsumo,  editConsumoMedio, editConsumoMedio2, editPrecio100, editConsumoMes, editKmMes;
    private TextView txtKm, txtKmTotales, txtGasto, txtGastoTotal, txtConsumo, txtConsumoMedio, txtConsumoMedio2, txtPrecio100, txtConsumoMes, txtKmMes;
    private LinearLayout linear1, linear2;
    private Button btnHistory;
    private Spinner spinnerVehiculos;
    private FloatingActionButton btnAddRefueling;
    MaterialSwitch switchBtn;
    ConstraintLayout primaryConstraint;
    private String vehiculo = "Honda CBF 125";
    double kmIniciales, kmTotales, kmFin;
    double lapseKm = 0;
    double lapseKmPrecio = 0;
    double precioInicial, totalPrecios, precioFin;
    double totalLitros = 0;
    double consumo100 = 0;
    double precio100 = 0;
    int color;
    ColorStateList colorStateList;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");

    List<Repostaje> listaRepostajes = new ArrayList<>();

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    //Toast.makeText(MainActivity.this, "Como estamos señores", Toast.LENGTH_SHORT).show();
                    mostrarInfo();
                    recreate();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instancias();
        mostrarInfo();
        acciones();

    }

    private void instancias() {

        primaryConstraint = findViewById(R.id.primaryConstraint);

        editTitle = findViewById(R.id.editTitle);

        editTotal = findViewById(R.id.editTotal);

        editKm = findViewById(R.id.editKm);
        txtKm = findViewById(R.id.txtKm);
        editKmTotales = findViewById(R.id.editKmTotales);
        txtKmTotales = findViewById(R.id.txtKmTotales);

        editGasto = findViewById(R.id.editGasto);
        txtGasto = findViewById(R.id.txtGasto);
        editGastoTotal = findViewById(R.id.editGastoTotal);
        txtGastoTotal = findViewById(R.id.txtGastoTotal);

        editConsumo = findViewById(R.id.editConsumo);
        txtConsumo = findViewById(R.id.txtConsumo);

        editConsumoMedio = findViewById(R.id.editConsumoMedio);
        txtConsumoMedio = findViewById(R.id.txtConsumoMedio);
        editConsumoMedio2 = findViewById(R.id.editConsumoMedio2);
        txtConsumoMedio2 = findViewById(R.id.txtConsumoMedio2);

        editPrecio100 = findViewById(R.id.editPrecio100);
        txtPrecio100 = findViewById(R.id.txtPrecio100);

        editConsumoMes = findViewById(R.id.editConsumoMes);
        txtConsumoMes = findViewById(R.id.txtConsumoMes);

        editKmMes = findViewById(R.id.editKmMes);
        txtKmMes = findViewById(R.id.txtKmMes);

        linear1 = findViewById(R.id.linear1);
        linear2 = findViewById(R.id.linear2);

        switchBtn = findViewById(R.id.switchBtn);
        btnHistory = findViewById(R.id.btnHistory);
        btnAddRefueling = findViewById(R.id.buttonAddRefueling);

        spinnerVehiculos = findViewById(R.id.spinner_vehiculos);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.options_array, R.layout.custom_spinner_item);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerVehiculos.setAdapter(adapter);

        color = ContextCompat.getColor(this, R.color.red);
        colorStateList = ColorStateList.valueOf(color);
    }


    private void acciones() {

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                intent.putExtra("vehiculo", vehiculo);
                someActivityResultLauncher.launch(intent);
            }
        });

        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            final Drawable originalColor = primaryConstraint.getBackground();
            int colorFondoChecked = isModoOscuro(MainActivity.this);
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Toast.makeText(MainActivity.this, String.valueOf(listaRepostajes.size()), Toast.LENGTH_SHORT).show();
                    visibility(MainActivity.this, TRUE);
                    primaryConstraint.setBackgroundColor(colorFondoChecked);
                    btnHistory.setBackgroundColor(colorFondoChecked);
                } else {
                    visibility(MainActivity.this, FALSE);
                    primaryConstraint.setBackground(originalColor);
                    btnHistory.setBackground(originalColor);


                }
            }
        });

        spinnerVehiculos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                vehiculo = parent.getItemAtPosition(position).toString();

                if ("Añadir".equals(vehiculo)) {
                    Toast.makeText(getApplicationContext(), "Estas a punto de añadir un vehiculo", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, SplashScreen.class);
                    startActivity(intent);

                }else{
                    //Toast.makeText(getApplicationContext(), vehiculo, Toast.LENGTH_SHORT).show();
                    switchBtn.setChecked(false);
                    mostrarInfo();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Implementa acciones si no se selecciona nada (opcional)
            }
        });

        btnAddRefueling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Abre la pantalla de ingreso de repostaje
                Intent intent = new Intent(MainActivity.this, RepostajeActivity.class);
                intent.putExtra("vehiculo", vehiculo);
                someActivityResultLauncher.launch(intent);
            }
        });
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void mostrarInfo() {

        totalPrecios = 0;
        totalLitros = 0;

        SharedPreferences sharedPreferences = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
        String stringRepostaje = sharedPreferences.getString(vehiculo, "");

        Type listType = new TypeToken<List<Repostaje>>() {}.getType();
        listaRepostajes = new Gson().fromJson(stringRepostaje, listType);

        linear1.setVisibility(View.VISIBLE);
        linear2.setVisibility(View.GONE);
        editTotal.setVisibility(View.GONE);

        if (listaRepostajes != null){

            //Toast.makeText(MainActivity.this, "Selected : " + selectedOption, Toast.LENGTH_SHORT).show();

            Repostaje rPrimero = listaRepostajes.get(0);
            kmIniciales = Double.parseDouble(rPrimero.getKm());
            precioInicial = Double.parseDouble(rPrimero.getPrecio());

            if (listaRepostajes.size()>1) {

                Repostaje rUltimo = listaRepostajes.get(listaRepostajes.size() - 1);

                double km1 = Double.parseDouble(rUltimo.getKm());
                double litres1 = Double.parseDouble(rUltimo.getLitros());
                double precio1 = Double.parseDouble(rUltimo.getPrecio());


                // SUMO TODOS LOS VALORES DE LITROS Y PRECIO DE TODOS LOS REPOSTAJES
                for (Repostaje repostaje1 : listaRepostajes) {
                    try {
                        double ltr = Double.parseDouble(repostaje1.getLitros());
                        double precio = Double.parseDouble(repostaje1.getPrecio());

                        totalPrecios += precio;
                        totalLitros += ltr;
                    } catch (NumberFormatException e) {
                        // Manejar excepciones de conversión aquí si es necesario
                    }
                }
                // Calcular la diferencia de kilómetros
                kmTotales = km1 - kmIniciales;
                totalPrecios = totalPrecios + precioInicial;

                if (listaRepostajes.size() > 2) {

                    LocalDate fechaFin = LocalDate.parse(rUltimo.getFecha(), formatter);
                    Repostaje rSegundo = listaRepostajes.get(1);
                    double km2 = Double.parseDouble(rSegundo.getKm());
                    LocalDate fechaInicio = LocalDate.parse(rSegundo.getFecha(), formatter);

                    //  OPERACIONES PARA CALCULAR L/100KM
                    lapseKm = (km1 - km2) / 100;
                    consumo100 = (totalLitros - litres1) / lapseKm;

                    // OPERACIONES PARA CALCUALAR €/100KM
                    lapseKmPrecio = kmTotales / 100;
                    precio100 = (totalPrecios - precio1) / (lapseKmPrecio);

                    precioFin = totalPrecios - (precioInicial + precio1);
                    kmFin = kmTotales - (km2 - kmIniciales);

                    Double dias30 = mensual(fechaInicio, fechaFin, precioFin);
                    Double km30 = mensual(fechaInicio, fechaFin, kmFin);

                    // PONEMOS KM Y GASTO EN TXT
                    txtKm.setText(format("%.0f", kmFin) + " Km");
                    txtKmTotales.setText(format("%.0f", kmTotales) + " Km");
                    txtGasto.setText(format("%.2f", precioFin) + " €");
                    txtGastoTotal.setText(format("%.2f", totalPrecios) + " €");
                    txtConsumo.setText(format("%.2f", (totalLitros - litres1)) + " L");

                    txtConsumoMedio.setText(format("%.3f", consumo100) + " L");
                    txtConsumoMedio2.setText(format("%.3f", consumo100) + " L");
                    txtPrecio100.setText(format("%.2f", precio100) + " €");
                    txtConsumoMes.setText(format("%.2f", dias30) + " €");
                    txtKmMes.setText(format("%.0f", km30) + " Km");
                }else{
                    emptyData(this);
                }
            }else{
                emptyData(this);
            }
        }else{
            Intent intent = new Intent(MainActivity.this, SplashScreen.class);
            intent.putExtra("vehiculo", vehiculo);
            someActivityResultLauncher.launch(intent);
        }

    }


    private Double mensual(LocalDate fechaInicio, LocalDate fechaFin, Double total) {

        long diasDiferencia = ChronoUnit.DAYS.between(fechaInicio, fechaFin);

        Double mensual = ((double)diasDiferencia)/30;

        Double gasto30 = total/mensual;

        return gasto30;
    }


    private void emptyData(@NonNull Context context){
        txtKm.setText("0 Km");
        txtGasto.setText("0,00 €");
        txtConsumoMedio.setText("No data");


        txtKmTotales.setText("0 Km");
        txtGastoTotal.setText("0,00 €");
        txtConsumo.setText("0,00 L");

        txtConsumoMedio2.setText("No data");
        txtPrecio100.setText("No data");
        txtConsumoMes.setText("No data");
        txtKmMes.setText("No data");
    }


    public void visibility(@NonNull Context context, boolean visi){
        if (visi){
            linear1.setVisibility(View.GONE);
            linear2.setVisibility(View.VISIBLE);

            editTotal.setVisibility(View.VISIBLE);
        }else{
            linear1.setVisibility(View.VISIBLE);
            linear2.setVisibility(View.GONE);

            editTotal.setVisibility(View.GONE);
        }
    }


    public int isModoOscuro(@NonNull Context context) {

        int colorFondoChecked;

        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            //btnAddRefueling.setBackgroundTintList(colorStateList);
            btnHistory.setTextColor(getResources().getColor(R.color.white));
            btnHistory.setBackgroundColor(getResources().getColor(R.color.darker_red));

            colorFondoChecked = getResources().getColor(R.color.darker_red);

            editTotal.setTextColor(getResources().getColor(R.color.white));

            editKm.setTextColor(getResources().getColor(R.color.white));
            txtKm.setTextColor(getResources().getColor(R.color.white));
            editKmTotales.setTextColor(getResources().getColor(R.color.white));
            txtKmTotales.setTextColor(getResources().getColor(R.color.white));

            editGasto.setTextColor(getResources().getColor(R.color.white));
            txtGasto.setTextColor(getResources().getColor(R.color.white));
            editGastoTotal.setTextColor(getResources().getColor(R.color.white));
            txtGastoTotal.setTextColor(getResources().getColor(R.color.white));

            editConsumo.setTextColor(getResources().getColor(R.color.white));
            txtConsumo.setTextColor(getResources().getColor(R.color.white));

            editConsumoMedio.setTextColor(getResources().getColor(R.color.white));
            txtConsumoMedio.setTextColor(getResources().getColor(R.color.white));
            editConsumoMedio2.setTextColor(getResources().getColor(R.color.white));
            txtConsumoMedio2.setTextColor(getResources().getColor(R.color.white));

            editPrecio100.setTextColor(getResources().getColor(R.color.white));
            txtPrecio100.setTextColor(getResources().getColor(R.color.white));

            editConsumoMes.setTextColor(getResources().getColor(R.color.white));
            txtConsumoMes.setTextColor(getResources().getColor(R.color.white));

            editKmMes.setTextColor(getResources().getColor(R.color.white));
            txtKmMes.setTextColor(getResources().getColor(R.color.white));

            //spinnerVehiculos.setBackgroundColor(getResources().getColor(R.color.white));

        } else {
            colorFondoChecked = getResources().getColor(R.color.red_light );
        }
        return colorFondoChecked;
    }

}
