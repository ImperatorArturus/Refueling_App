package com.example.refueling;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.refueling.extras.Repostaje;
import com.example.refueling.extras.SplashScreen;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RepostajeActivity extends AppCompatActivity {
    private EditText editTextKilometers, editTextLitres, editTextEuros;
    private MaterialDatePicker materialDatePicker;
    private String vehiculo;
    private Button mPickDateButton, buttonSaveRefueling;

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

        String date = mPickDateButton.getText().toString();
        String kilometresString = editTextKilometers.getText().toString();
        int kilometres = Integer.parseInt(kilometresString);
        String euros = editTextEuros.getText().toString();
        String litros = editTextLitres.getText().toString();


        //Comprobamos que los datos introducidos no esten vacios
        if (!kilometresString.equals("") && !date.equals("") && !date.equals("Select Date") && !euros.equals("") && !litros.equals("")){
            String jsonRepostajes = sharedPreferences.getString(vehiculo, null);
            Type listType = new TypeToken<List<Repostaje>>() {}.getType();
            List<Repostaje> listaRepostajes;

            if (jsonRepostajes != null) {
                listaRepostajes = gson.fromJson(jsonRepostajes, listType);
            } else {
                // Si la lista no existe, crea una nueva lista vacía
                listaRepostajes = new ArrayList<>();
            }

            String litrosFormateado = parseoCantidades(litros);
            String eurosFormateado = parseoCantidades(euros);

            Repostaje nuevoRepostaje = new Repostaje(UUID.randomUUID().toString(), date, kilometres, eurosFormateado, litrosFormateado);

            addRefuel(nuevoRepostaje, listaRepostajes);

        }else{
            Toast.makeText(this, "Introduzca todos los datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void addRefuel(Repostaje nuevoRepostaje, List<Repostaje> listaRepostajes){
        Gson gson = new Gson();

        SharedPreferences sharedPreferences = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        listaRepostajes.add(nuevoRepostaje);

        // Ordena la lista por Kilometros (el método compareTo de la interfaz Comparable se encargará de la comparación)
        Collections.sort(listaRepostajes, Comparator.comparing(Repostaje::getKm));

        // Convierte la lista actualizada a JSON
        String jsonActualizado = gson.toJson(listaRepostajes);
        String stringFechaAnterior = null;
        String stringFechaPosterior = null;

        int index = Collections.binarySearch(listaRepostajes, nuevoRepostaje, Comparator.comparing(Repostaje::getKm));

        if (index>=0){
            // Se encontró el Repostaje
            Repostaje repostajeEncontrado = listaRepostajes.get(index);

            // Obtiene la fecha del elemento anterior, si existe
            Date fechaAnterior = null;
            if (index > 0) {
                Repostaje repostajeAnterior = listaRepostajes.get(index - 1);
                stringFechaAnterior = repostajeAnterior.getFecha();
                try {
                    fechaAnterior = formatter.parse(stringFechaAnterior);
                    //System.out.println(stringFechaAnterior);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // Obtiene la fecha del elemento posterior, si existe
            Date fechaPosterior = null;
            if (index < listaRepostajes.size() - 1) {
                Repostaje repostajePosterior = listaRepostajes.get(index + 1);
                stringFechaPosterior = repostajePosterior.getFecha();
                try {
                    fechaPosterior = formatter.parse(stringFechaPosterior);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }



            // Verifica si la fecha del Repostaje está entre las fechas anterior y posterior
            try {
                Date fechaRespostaje = formatter.parse(repostajeEncontrado.getFecha());

                System.out.println("Index: " + index + " longitud de la lista: "+ listaRepostajes.size());
                System.out.println("Fecha anterior--> " + fechaAnterior);
                System.out.println("Fecha repostaje introducido --> " + fechaRespostaje);
                System.out.println("Fecha posterior--> " + fechaPosterior);


                // La fecha del Repostaje está entre las fechas anterior y posterior
                if (fechaAnterior == null || fechaRespostaje.equals(fechaAnterior) || fechaRespostaje.after(fechaAnterior)){
                    if (fechaPosterior == null || fechaRespostaje.equals(fechaPosterior) || fechaRespostaje.before(fechaPosterior)){

                        editor.putString(vehiculo, jsonActualizado);
                        editor.apply();
                        Toast.makeText(getApplicationContext(), "Repostaje agregado", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }else{
                        mostrarAlerta(Boolean.FALSE, stringFechaPosterior);
                    }
                } else {
                    mostrarAlerta(Boolean.TRUE, stringFechaAnterior);
                }
            }catch (ParseException e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Repostaje no encontrado", Toast.LENGTH_SHORT).show();
        }


    }

    public void mostrarAlerta(Boolean cuando, String fecha){

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RepostajeActivity.this);
        String formattedText = null;
        builder.setTitle("FECHA INCORRECTA");

        if (cuando==Boolean.TRUE){
            formattedText = "La fecha debe ser posterior a: \n" + fecha.toUpperCase();
        }else{
            formattedText = "La fecha deber ser anterior a: \n" + fecha.toUpperCase();
        }

        SpannableString spannableString = new SpannableString(formattedText);

        // Obtener la posición de inicio y fin de la parte que deseas resaltar
        int start = formattedText.indexOf(fecha.toUpperCase());
        int end = start + fecha.length();

        // Aplicar el estilo negrita a la parte específica del texto
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setMessage(spannableString);

        // Agregar el botón "Aceptar"
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // Mostrar el AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
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
            if (!text.contains(".")){
                if (text.length() == 3) {
                    String modifiedText = new StringBuilder(text)
                            .insert(2,".")
                            .toString();

                    editText2.removeTextChangedListener(this);
                    editText2.setText(modifiedText);
                    editText2.setSelection(modifiedText.length());
                    editText2.addTextChangedListener(this);
                }
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
                return true;
            }
            return false; // Devuelve false para que se maneje de manera predeterminada
        }
    }

    private String parseoCantidades(String cifra){

        String[] cifraSplit = cifra.split("\\.");
        String cifraEntero = cifraSplit[0];
        String cifraDecimal = (cifraSplit.length > 1) ? cifraSplit[1] : "00";
        if (cifraEntero.length() == 1) {
            cifraEntero = "0" + cifraEntero;
        }
        if (cifraDecimal.length() == 1){
            cifraDecimal = cifraDecimal + "0";
        }
        // Formatear el resultado
        String cifraFormateada = cifraEntero + "." + cifraDecimal;

        return cifraFormateada;

    }

    //Instanciamos las variables

}
