package com.example.birthdayhelper.Utils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class BirthdayProvider extends ContentProvider {

    //authority es un identificador unico para cada provider
    public static final String AUTHORITY = "com.example.birthdayhelper.provider";

    //uri para acceder a los datos del provider
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/miscumples");

    private ConnectionDB db;

    @Override
    public boolean onCreate() {
        db = ConnectionDB.getConnection(getContext());
        return true;
    }

    /**
     * https://developer.android.com/guide/topics/providers/content-provider-basics?hl=es-419
     * @param uri
     * @param projection es el conjunto de columnas que una query devuelve
     * @param selection filtro
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = this.db.getReadableDatabase();
        return db.query(ConnectionDB.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = this.db.getWritableDatabase();
        long id = db.insert(ConnectionDB.TABLE_NAME, null, values);
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.db.getWritableDatabase();
        return db.delete(ConnectionDB.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.db.getWritableDatabase();
        return db.update(ConnectionDB.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".miscumples";

    }
}
