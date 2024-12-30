package com.example.birthdayhelper.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.birthdayhelper.Persona.Contactos;

import java.util.List;

public class ConnectionDB extends SQLiteOpenHelper {
    //https://www.geeksforgeeks.org/how-to-create-and-add-data-to-sqlite-database-in-android/
    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "birthday";
    public static final String TABLE_NAME = "miscumples";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_TIPO_NOTIF = "TipoNotif";
    public static final String COLUMN_MENSAJE = "Mensaje";
    public static final String COLUMN_TELEFONO = "Telefono";
    public static final String COLUMN_FECHA_NACIMIENTO = "FechaNacimiento";
    public static final String COLUMN_NOMBRE = "Nombre";


    private static ConnectionDB db;

    private ConnectionDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    public static synchronized ConnectionDB getConnection(Context context) {
        if (db == null) {
            db = new ConnectionDB(context.getApplicationContext());
        }
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TIPO_NOTIF + " CHAR(1), " +
                COLUMN_MENSAJE + " VARCHAR(160), " +
                COLUMN_TELEFONO + " VARCHAR(15), " +
                COLUMN_FECHA_NACIMIENTO + " VARCHAR(15), " +
                COLUMN_NOMBRE + " VARCHAR(128))";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS miscumples");
        onCreate(db);
    }


    public void insertarContactosDatabase(List<Contactos> contactos) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (Contactos c : contactos) {
                int contactoId = c.getId();

                // Verificar si el contacto ya existe en la base de datos por su ID
                if (!contactoExiste(contactoId)) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_NOMBRE, c.getNombre());
                    values.put(COLUMN_TELEFONO, c.getTelefono()); // Puede ser nulo
                    values.put(COLUMN_MENSAJE, c.getMensaje());
                    values.put(COLUMN_FECHA_NACIMIENTO, c.getFechaNacimiento());
                    values.put(COLUMN_TIPO_NOTIF, c.getTipoNotif());

                    // Insertar el contacto en la base de datos
                    db.insert(TABLE_NAME, null, values);
                } else {
                    Log.d("Database", "El contacto con ID " + contactoId + " ya existe. No se inserta.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al insertar contactos en la base de datos", e);
        } finally {
         //   db.close(); // Cerrar la conexión después de la operación
        }
    }

    //comprobamos si el numero de telefono existe en la database
    //generalmente 1 persona esta linkada a 1 numero, aunque luego pueda tener mas
    //pero este numero nos llevara al id de la persona en cuestion
    public boolean contactoExiste(int contactoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_ID},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(contactoId)},
                null, null, null
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public String obtenerTipoNotifDesdeDB(int contactoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tipoNotif = null;


        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_TIPO_NOTIF},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(contactoId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {

            tipoNotif = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPO_NOTIF));
            cursor.close();
        }

        return tipoNotif;
    }

    public String obtenerMensajeDesdeDB(int contactoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String mensaje = null;

        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_MENSAJE},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(contactoId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            mensaje = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MENSAJE));
            cursor.close();
        }

        return mensaje;
    }


    public void actualizarMensaje(int contactoId, String nuevoMensaje) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MENSAJE, nuevoMensaje);

        int rowsUpdated = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(contactoId)});

        if (rowsUpdated > 0) {
            Log.d("ConnectionDB", "Mensaje actualizado correctamente en la base de datos.");
        } else {
            Log.d("ConnectionDB", "No se actualizó ninguna fila. Verifica que el ID del contacto sea correcto.");
        }
    }


    public void actualizarTipoNotif(int contactoId, String tipoNotif) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tipoNotif", tipoNotif);

        // Actualiza la fila con el ID correspondiente
        int rowsUpdated = db.update("miscumples", values, "id = ?", new String[]{String.valueOf(contactoId)});

        if (rowsUpdated > 0) {
            Log.d("ConnectionDB", "TipoNotif actualizado correctamente en la base de datos.");
        } else {
            Log.d("ConnectionDB", "No se actualizó ninguna fila.");
        }
    }

    public boolean verificarTipoNotifEnDB(int contactoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tipoNotif = null;

        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_TIPO_NOTIF},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(contactoId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {

            tipoNotif = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPO_NOTIF));
            cursor.close();
        }

        return "S".equals(tipoNotif);
        }



    //depuracion
    public void vaciarTabla() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        //restablece el contador de id
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_NAME + "';");
    }

}

