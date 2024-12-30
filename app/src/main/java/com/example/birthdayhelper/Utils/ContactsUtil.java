package com.example.birthdayhelper.Utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.birthdayhelper.Persona.Contactos;


import java.util.ArrayList;

import java.util.List;

public class ContactsUtil {

    /**
     * @param contentResolver para obtener los datos de contactos del telfono
     * @return el arraylist con los contactos en formato string, mapeados de manera que cada nombre, tenga su numero
     */

    public static List<Contactos> getContacts(ContentResolver contentResolver) {
        List<Contactos> contactList = new ArrayList<>();

        try (Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.PHOTO_URI}, null, null, null)) {

            if (cursor != null) {
                int contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int hasPhoneNumberIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                int photoUriIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);

                while (cursor.moveToNext()) {
                    String contactId = cursor.getString(contactIdIndex);
                    String name = cursor.getString(nameIndex);
                    int hasPhoneNumber = cursor.getInt(hasPhoneNumberIndex);
                    String photoUri = cursor.getString(photoUriIndex);

                    ArrayList<String> listaTelefonos = hasPhoneNumber > 0 ? getTelefono(contentResolver, contactId) : new ArrayList<>();

                    String birthday = getCumple(contentResolver, contactId);
                    Log.d("Contactos", "Contacto: " + name + ", Fecha de nacimiento: " + birthday);
                    Contactos contacto = new Contactos(Integer.parseInt(contactId),
                            name,
                            (listaTelefonos.isEmpty() ? null : listaTelefonos.get(0)), // Guarda el primer número como "teléfono principal"
                            birthday,
                            null,
                            "",
                            null,
                            photoUri,
                            listaTelefonos
                    );
                    contactList.add(contacto);
                }
            }
        }

        return contactList;
    }


    private static ArrayList<String> getTelefono(ContentResolver contentResolver, String contactId) {
        ArrayList<String> listTelefono = new ArrayList<>();
        try (Cursor c = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null)) {

            if (c != null) {
                while (c.moveToNext()) {
                    listTelefono.add(c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                }
            }
        }
        return listTelefono;
    }

    private static String getCumple(ContentResolver contentResolver, String contactId) {
        String birthday = null;


        try (Cursor cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Event.START_DATE}, ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{contactId, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE}, null)) {

            if (cursor != null) {
                int birthdayIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
                if (cursor.moveToFirst()) {
                    birthday = cursor.getString(birthdayIndex);
                }
            }
        }
        return birthday;
    }



    //solo rellena de contactos nuevos que no esten en la database
    public static void rellenarDatabase(ContentResolver cr, ConnectionDB db) {
        List<Contactos> contactos = getContacts(cr);
        db.insertarContactosDatabase(contactos);
    }

}
