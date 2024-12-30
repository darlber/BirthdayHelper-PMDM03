package com.example.birthdayhelper.Persona;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;

public class Contactos implements Serializable {
    private int id;
    private String nombre;
    private String telefono;
    private String fechaNacimiento;
    private String tipoNotificacion;
    private String mensaje;
    private String imagenId;
    private String imagenUriString;
    private ArrayList<String> setTelefonos;

    public Contactos(int id, String nombre, String telefono, String fechaNacimiento,
                     String tipoNotificacion, String mensaje, String imagenId, String imagenUriString, ArrayList<String> setTelefonos) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoNotificacion = tipoNotificacion;
        this.mensaje = mensaje;
        this.imagenId = imagenId;
        this.imagenUriString = imagenUriString;
        this.setTelefonos = setTelefonos != null ? setTelefonos : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipoNotif() {
        return tipoNotificacion;
    }

    public void setTipoNotif(String tipoNotif) {
        this.tipoNotificacion = tipoNotif;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImagenId() {
        return imagenId;
    }

    public void setImagenId(String imagenId) {
        this.imagenId = imagenId;
    }

    public String getImagenUriString() {
        return imagenUriString;
    }

    public void setImagenUriString(String imagenUriString) {
        this.imagenUriString = imagenUriString;
    }

    // Método para obtener el Uri a partir del String
    public Uri getImagenUri() {
        return imagenUriString != null ? Uri.parse(imagenUriString) : null;
    }

    // Método para establecer el Uri, almacenando la ruta como String
    public void setImagenUri(Uri imagenUri) {
        this.imagenUriString = imagenUri != null ? imagenUri.toString() : null;
    }

    public ArrayList<String> getSetTelefonos() {
        return setTelefonos;
    }

    public void setSetTelefonos(ArrayList<String> setTelefonos) {
        this.setTelefonos = setTelefonos != null ? setTelefonos : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Contactos{" +
                "id=" + id +
                ", tipoNotif='" + tipoNotificacion + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", telefono='" + telefono + '\'' +
                ", fechaNacimiento='" + fechaNacimiento + '\'' +
                ", nombre='" + nombre + '\'' +
                ", imagenId='" + imagenId + '\'' +
                ", rutaImagen=" + imagenUriString +
                ", setTelefonos=" + setTelefonos +
                '}';
    }
}
