package com.hfad.solicitudesformativas;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetalleActivity extends AppCompatActivity {

    // Nombre de la "etiqueta" con la que MainActivity envía los datos
    public static final String EXTRA_RESUMEN = "resumen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar de esta pantalla
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Botón Up (flecha para volver a MainActivity)
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.titulo_detalle);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Mostrar los datos que llegaron desde MainActivity
        String resumen = getIntent().getStringExtra(EXTRA_RESUMEN);
        TextView tvDetalle = findViewById(R.id.tvDetalle);
        tvDetalle.setText(resumen);
    }
}