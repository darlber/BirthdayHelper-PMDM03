package com.example.birthdayhelper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.birthdayhelper.Persona.Contactos;
import com.example.birthdayhelper.Utils.ConnectionDB;

import java.util.ArrayList;

public class EditarContacto extends AppCompatActivity {

    private ImageView ivFoto;
    private TextView tvNombre, tvFechaNaci;
    private EditText etMensaje;
    private Spinner spinTelfs;
    private CheckBox cbAutoSMS;
    private Button btnGuardar, btnEditar;
    private Contactos contacto;
    private ActivityResultLauncher<Intent> editarEnAndroidLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_contacto);
        ConnectionDB db = ConnectionDB.getConnection(this);

        inicializarVistas();

        contacto = (Contactos) getIntent().getSerializableExtra("contacto");

        if (contacto != null) {
            mostrarDatosContacto(contacto, db);

        }

        listeners();
        refrescarTrasGuardar(db);
    }

    private void listeners() {
        btnEditar.setOnClickListener(v -> {
            editarContactoEnAplicacion(contacto);
        });

        btnGuardar.setOnClickListener(v -> {
            if (contacto != null) {
                ConnectionDB db = ConnectionDB.getConnection(EditarContacto.this);
                String mensaje = etMensaje.getText().toString();

                contacto.setMensaje(mensaje);
                db.actualizarMensaje(contacto.getId(), mensaje);

                contacto.setTipoNotif(cbAutoSMS.isChecked() ? "S" : null);
                db.actualizarTipoNotif(contacto.getId(), contacto.getTipoNotif());

                mostrarDatosContacto(contacto, db);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("contacto_actualizado", contacto);
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(this, "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        cbAutoSMS.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // No hacemos nada aquí, solo actualizamos el estado del CheckBox
            // La actualización en la base de datos se hará en el botón "Guardar"
        });
    }


    private void inicializarVistas() {
        ivFoto = findViewById(R.id.ivFoto);
        tvNombre = findViewById(R.id.tvNombre);
        tvFechaNaci = findViewById(R.id.tvFechaNaci);
        etMensaje = findViewById(R.id.etMensaje);
        spinTelfs = findViewById(R.id.spinTelfs);
        cbAutoSMS = findViewById(R.id.cbAutoSMS);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEditar = findViewById(R.id.btnEditar);
    }

    private void refrescarTrasGuardar(ConnectionDB db) {
        editarEnAndroidLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {

                        Contactos contactoActualizado = recargarContacto(contacto.getId());

                        if (contactoActualizado != null) {
                            contacto = contactoActualizado;
                            mostrarDatosContacto(contacto, db);
                            Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No se pudo actualizar los datos del contacto", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("EditarContacto", "La edición del contacto fue cancelada o no se guardaron cambios.");
                    }
                }
        );
    }

    private void mostrarDatosContacto(Contactos contacto, ConnectionDB db) {

        tvNombre.setText(contacto.getNombre());
        tvFechaNaci.setText(contacto.getFechaNacimiento());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contacto.getSetTelefonos());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTelfs.setAdapter(adapter);

        //async esta un poco desactualizado
        if (!TextUtils.isEmpty(contacto.getImagenUriString())) {
            Glide.with(this).load(Uri.parse(contacto.getImagenUriString())).into(ivFoto);
        } else {
            ivFoto.setImageResource(R.drawable.ic_launcher_foreground);
        }

        boolean isAutoSMS = db.verificarTipoNotifEnDB(contacto.getId());
        cbAutoSMS.setChecked(isAutoSMS);

        String mensaje = db.obtenerMensajeDesdeDB(contacto.getId());
        etMensaje.setText(mensaje);

    }

    private void editarContactoEnAplicacion(Contactos c) {
        if (c != null) {
            Uri contactoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, c.getId());
            Log.d("EditarContacto", "URI del contacto construida: " + contactoUri.toString());

            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setDataAndType(contactoUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            intent.putExtra("finishActivityOnSaveCompleted", true);
            editarEnAndroidLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Contacto no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private Contactos recargarContacto(long id) {
        ContentResolver resolver = getContentResolver();

        Uri contactoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);

        String[] proyeccionContacto = {
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
        };

        Contactos contacto = null;
        try (Cursor cursorContacto = resolver.query(contactoUri, proyeccionContacto, null, null, null)) {
            if (cursorContacto != null && cursorContacto.moveToFirst()) {
                String nombre = cursorContacto.getString(cursorContacto.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                String imagenUri = cursorContacto.getString(cursorContacto.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI));

                ArrayList<String> telefonos = obtenerTelefonosContacto(id, resolver);

                String fechaNacimiento = obtenerFechaNacimiento(id, resolver);

                contacto = new Contactos(
                        (int) id,
                        nombre,
                        telefonos.isEmpty() ? null : telefonos.get(0),
                        fechaNacimiento,
                        null,
                        null,
                        null,
                        imagenUri,
                        telefonos
                );
            }
        } catch (Exception e) {
            Log.e("EditarContacto", "Error al obtener detalles del contacto", e);
        }

        if (contacto == null) {
            Log.e("EditarContacto", "No se pudo recargar el contacto con ID: " + id);
        }

        return contacto;
    }

    private ArrayList<String> obtenerTelefonosContacto(long id, ContentResolver resolver) {
        ArrayList<String> telefonos = new ArrayList<>();
        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String filtro = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String[] argsFiltro = {String.valueOf(id)};

        try (Cursor phoneCursor = resolver.query(phoneUri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, filtro, argsFiltro, null)) {
            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    String telefono = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    telefonos.add(telefono);
                }
            }
        }

        return telefonos;
    }

    private String obtenerFechaNacimiento(long id, ContentResolver resolver) {
        String fechaNacimiento = null;

        Uri dataUri = ContactsContract.Data.CONTENT_URI;
        String filtro = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?";
        String[] argsFiltro = {String.valueOf(id), ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE};

        try (Cursor cursor = resolver.query(dataUri, new String[]{ContactsContract.CommonDataKinds.Event.START_DATE}, filtro, argsFiltro, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE));
            }
        }
        return fechaNacimiento;
    }
}
