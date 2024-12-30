package com.example.birthdayhelper.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class AlarmaUtil {

    public static void configurarAlarma(Context context, int hourOfDay, int minute) {
        // Obtener el calendario actual
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Si la hora seleccionada ya pasó hoy, configurar la alarma para el siguiente día
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, AlarmaBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Configurar el AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                // Verificar si se puede programar alarmas exactas en Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        mostrarToast(context, hourOfDay, minute);
                    } else {
                        permisoAlarma(context);
                    }
                } else {
                    // Configurar la alarma para versiones anteriores a Android 12
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    mostrarToast(context, hourOfDay, minute);
                }
            } catch (SecurityException e) {
                Toast.makeText(context, "No se pudo configurar la alarma: permiso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void mostrarToast(Context context, int hourOfDay, int minute) {
        String horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
        Toast.makeText(context, "Alarma configurada para las " + horaFormateada, Toast.LENGTH_SHORT).show();
    }

    private static void permisoAlarma(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            context.startActivity(intent);
            Toast.makeText(context, "Permiso requerido para alarmas exactas.", Toast.LENGTH_SHORT).show();
        }
    }
}
