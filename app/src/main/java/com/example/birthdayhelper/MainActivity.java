package com.example.birthdayhelper;

import static com.example.birthdayhelper.Utils.AlarmaBroadcast.CHANNEL_ID;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.birthdayhelper.Persona.Contactos;
import com.example.birthdayhelper.Utils.AdaptadorContacto;
import com.example.birthdayhelper.Utils.AlarmaUtil;
import com.example.birthdayhelper.Utils.ConnectionDB;
import com.example.birthdayhelper.Utils.ContactsUtil;
import com.example.birthdayhelper.Utils.TimePickerFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ConnectionDB db;

    private final ActivityResultLauncher<Intent> editarContactoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Obtener el contacto actualizado
                    Contactos contactoActualizado = (Contactos) result.getData().getSerializableExtra("contacto_actualizado");
                    if (contactoActualizado != null) {
                        // Actualizar la lista de contactos
                        mostrarLista();
                    }
                }
            }
    );

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


        permisos();
        createNotificationChannel();
        db = ConnectionDB.getConnection(this);
        //db.vaciarTabla();
        ContactsUtil.rellenarDatabase(getContentResolver(), db);
        mostrarLista();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.confFeli) {

            TimePickerFragment timePickerFragment = new TimePickerFragment();
            timePickerFragment.setOnTimeSetListener((view, hora, minute) -> {
                Log.w("NuevoTime", "Hora: " + hora + " <|> Minutos: " + minute);
                AlarmaUtil.configurarAlarma(MainActivity.this, hora, minute);
            });

            timePickerFragment.show(getSupportFragmentManager(), "timePicker");
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void permisos() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.WRITE_CONTACTS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsNeeded.toArray(new String[0]));
        }
    }

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean smsPermissionGranted = result.get(Manifest.permission.SEND_SMS);
                Boolean contactsPermissionGranted = result.get(Manifest.permission.READ_CONTACTS);
                Boolean writeContactsPermissionGranted = result.get(Manifest.permission.WRITE_CONTACTS);
                Boolean notificationsPermissionGranted = result.get(Manifest.permission.POST_NOTIFICATIONS);

                if (smsPermissionGranted != null && smsPermissionGranted) {
                    Log.d("Permissions", "Permiso para enviar SMS concedido");
                } else {
                    Log.d("Permissions", "Permiso para enviar SMS denegado");
                }

                if (contactsPermissionGranted != null && contactsPermissionGranted) {
                    Log.d("Permissions", "Permiso para leer contactos concedido");
                } else {
                    Log.d("Permissions", "Permiso para leer contactos denegado");
                }

                if (writeContactsPermissionGranted != null && writeContactsPermissionGranted) {
                    Log.d("Permissions", "Permiso para escribir en contactos concedido");
                } else {
                    Log.d("Permissions", "Permiso para escribir en contactos denegado");
                }

                if (notificationsPermissionGranted != null && notificationsPermissionGranted) {
                    Log.d("Permissions", "Permiso para notificaciones concedido");
                } else {
                    Log.d("Permissions", "Permiso para notificaciones denegado");
                }
            });

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Cumpleaños";
            String description = "Notificaciones para cumpleaños";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //mostrar al principio y en cada actualizacion
    private void mostrarLista() {
        List<Contactos> listaContactos = ContactsUtil.getContacts(getContentResolver());

        for (Contactos contacto : listaContactos) {
            // Supongamos que tienes un método para obtener el tipo de notificación desde la base de datos
            String tipoNotif = db.obtenerTipoNotifDesdeDB(contacto.getId());
            contacto.setTipoNotif(tipoNotif); // Actualizar el contacto con el tipo de notificación
        }
        ListView listView = findViewById(R.id.contactListView);
        AdaptadorContacto ac = new AdaptadorContacto(this, listaContactos);
        listView.setAdapter(ac);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Contactos contactoSeleccionado = (Contactos) parent.getItemAtPosition(position);

            Intent intent = new Intent(MainActivity.this, EditarContacto.class);
            intent.putExtra("contacto", contactoSeleccionado);

            editarContactoLauncher.launch(intent);
        });
    }

    //Gestiona la contestación a la petición de permisos @Override
    //prints por depuracion
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de lectura concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso denegado para leer los contactos", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de escritura concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de escritura denegado", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso para notificaciones concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso para notificaciones denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

}