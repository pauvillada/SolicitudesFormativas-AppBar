package com.hfad.solicitudesformativas;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // Identificador del canal de notificaciones
    private static final String CANAL_ID = "canal_solicitudes";

    // Número para que cada notificación sea distinta
    private int numeroNotificacion = 1;

    // Campos del formulario
    private EditText etNombre, etCorreo, etActividad, etFecha, etObservaciones;

    // Pide el permiso de notificaciones y, si lo dan, crea la notificación
    private final ActivityResultLauncher<String> pedirPermisoNotificaciones =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    crearNotificacion();
                } else {
                    Toast.makeText(this, "Sin permiso no se pueden mostrar notificaciones",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Usar la toolbar como barra de aplicación
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Conectar los campos del formulario con el código
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etActividad = findViewById(R.id.etActividad);
        etFecha = findViewById(R.id.etFecha);
        etObservaciones = findViewById(R.id.etObservaciones);

        // Botón Compartir
        Button btnCompartir = findViewById(R.id.btnCompartir);
        btnCompartir.setOnClickListener(v -> compartirSolicitud());

        // Crear el canal de notificaciones (obligatorio desde Android 8)
        crearCanalNotificaciones();
    }

    // Arma un texto con todos los datos del formulario
    private String construirResumen() {
        return "SOLICITUD DE ACTIVIDAD FORMATIVA\n"
                + "Solicitante: " + etNombre.getText().toString() + "\n"
                + "Correo: " + etCorreo.getText().toString() + "\n"
                + "Actividad: " + etActividad.getText().toString() + "\n"
                + "Fecha deseada: " + etFecha.getText().toString() + "\n"
                + "Observaciones: " + etObservaciones.getText().toString();
    }

    // Revisa que estén los datos mínimos
    private boolean formularioValido() {
        if (etNombre.getText().toString().trim().isEmpty()
                || etActividad.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Escribe al menos el nombre y la actividad", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Compartir el formulario con un Intent SEND
    private void compartirSolicitud() {
        if (!formularioValido()) return;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Solicitud de actividad formativa");
        intent.putExtra(Intent.EXTRA_TEXT, construirResumen());
        startActivity(Intent.createChooser(intent, "Compartir solicitud con..."));
    }

    // Abrir la pantalla de características
    private void verDetalle() {
        if (!formularioValido()) return;

        Intent intent = new Intent(this, DetalleActivity.class);
        intent.putExtra(DetalleActivity.EXTRA_RESUMEN, construirResumen());
        startActivity(intent);
    }

    // Crea el canal por donde salen las notificaciones de la app
    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    "Solicitudes formativas",
                    NotificationManager.IMPORTANCE_DEFAULT);
            canal.setDescription("Avisos de solicitudes de actividades formativas registradas");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }
    }

    // Crea y muestra la notificación de la solicitud
    private void crearNotificacion() {
        if (!formularioValido()) return;

        // En Android 13 o superior hay que pedir permiso primero
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            pedirPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }

        // Qué se abre al tocar la notificación: la pantalla de características
        Intent intent = new Intent(this, DetalleActivity.class);
        intent.putExtra(DetalleActivity.EXTRA_RESUMEN, construirResumen());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                numeroNotificacion,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Armar la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CANAL_ID)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle("Solicitud registrada")
                .setContentText(etActividad.getText().toString() + " - " + etNombre.getText().toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(construirResumen()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Mostrarla
        NotificationManagerCompat.from(this).notify(numeroNotificacion, builder.build());
        numeroNotificacion++;

        Toast.makeText(this, "Notificación creada", Toast.LENGTH_SHORT).show();
    }

    // Poner los botones del menú en la toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Qué pasa cuando se toca cada botón de la toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_ver_detalle) {
            verDetalle();
            return true;
        } else if (id == R.id.action_notificacion) {
            crearNotificacion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}