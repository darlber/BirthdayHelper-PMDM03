package com.example.birthdayhelper.Utils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.birthdayhelper.Persona.Contactos;
import com.example.birthdayhelper.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmaBroadcast extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "birthday_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar hoy = Calendar.getInstance();
        int diaHoy = hoy.get(Calendar.DAY_OF_MONTH);
        int mesHoy = hoy.get(Calendar.MONTH) + 1;
        ConnectionDB db = ConnectionDB.getConnection(context);

        List<Contactos> contactos = ContactsUtil.getContacts(context.getContentResolver());

        StringBuilder cumpleaneros = new StringBuilder();

        for (Contactos contacto : contactos) {
            String fechaNacimiento = contacto.getFechaNacimiento();
            if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                try {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    if (fechaNacimiento.length() < 8) { // Ajuste para fechas sin año "--MM-DD"
                        sdf = new SimpleDateFormat("--MM-dd", Locale.getDefault());
                    }

                    Date fechaNac = sdf.parse(fechaNacimiento);
                    Calendar calNac = Calendar.getInstance();
                    calNac.setTime(fechaNac);

                    int diaNac = calNac.get(Calendar.DAY_OF_MONTH);
                    int mesNac = calNac.get(Calendar.MONTH) + 1;

                    if (diaNac == diaHoy && mesNac == mesHoy) {
                        if (cumpleaneros.length() > 0) {
                            cumpleaneros.append(", ");
                        }
                        cumpleaneros.append(contacto.getNombre());
                        // Obtener el TipoNotif y el mensaje desde la base de datos
                        String tipoNotif = db.obtenerTipoNotifDesdeDB(contacto.getId());
                        String mensaje = db.obtenerMensajeDesdeDB(contacto.getId());

                        Log.d("AlarmaBroadcast", "El TipoNotif es " + tipoNotif + " y el mensaje es " + mensaje);

                        // Verificar si el TipoNotif es "S"
                        if (tipoNotif != null && tipoNotif.equals("S")) {
                            // Obtener el teléfono del contacto
                            String telefono = contacto.getSetTelefonos().get(0);
                            if (telefono != null && mensaje != null && !telefono.isEmpty() && !mensaje.isEmpty()) {
                                // Enviar el SMS con el mensaje recuperado de la base de datos
                                enviarSMS(context, telefono, mensaje);
                            }
                        }
                    }
                } catch (ParseException e) {
                    Log.e("AlarmaBroadcast", "Error al parsear fecha: " + fechaNacimiento, e);
                    Toast.makeText(context, "Error en formato de fecha de un contacto", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (cumpleaneros.length() > 0) {
            notificacionCumple(context, cumpleaneros.toString());
        }
    }

    private void notificacionCumple(Context context, String cumpleaneros) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).setContentTitle("¡Feliz Cumpleaños!").setContentText("Hoy es el cumpleaños de: " + cumpleaneros).setSmallIcon(R.drawable.cake512).setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            Toast.makeText(context, "Permiso para notificaciones no otorgado", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarSMS(Context context, String telefono, String mensaje) {
        Log.d("AlarmaBroadcast", "Entrando a la función enviarSMS");
        if (telefono == null || telefono.isEmpty()) {
            Log.e("AlarmaBroadcast", "No se puede enviar SMS: número de teléfono no válido.");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(telefono, null, mensaje, null, null);
                Log.d("AlarmaBroadcast", "SMS enviado a " + telefono);
                Toast.makeText(context, "SMS enviado a " + telefono, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("AlarmaBroadcast", "Error al enviar SMS: " + e.getMessage(), e);
                Toast.makeText(context, "Error al enviar SMS", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(context, "Permiso para enviar SMS no otorgado", Toast.LENGTH_SHORT).show();
            Log.e("AlarmaBroadcast", "Permiso para enviar SMS no otorgado.");
        }
    }
}
