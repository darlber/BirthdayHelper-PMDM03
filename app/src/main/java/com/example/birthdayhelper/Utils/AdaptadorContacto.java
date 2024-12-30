package com.example.birthdayhelper.Utils;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.birthdayhelper.Persona.Contactos;
import com.example.birthdayhelper.R;

import java.util.List;

//clase para enlazar el listview a cada item que se rescatara para ser mostrado
public class AdaptadorContacto extends ArrayAdapter<Contactos> {

    private Context context;
    private List<Contactos> contacts;

    public AdaptadorContacto(Context context, List<Contactos> contacts) {
        super(context, R.layout.items_lista, contacts);
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.items_lista, parent, false);
            holder = new ViewHolder();
            holder.contactPhoto = convertView.findViewById(R.id.contactPhoto);
            holder.contactName = convertView.findViewById(R.id.contactNombre);
            holder.contactNumber = convertView.findViewById(R.id.contactPhone);
            holder.contactBirthday = convertView.findViewById(R.id.contactBirthday);
            holder.contactNotificationType = convertView.findViewById(R.id.contactTipoNot);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contactos contact = contacts.get(position);
        holder.contactName.setText(contact.getNombre());
        holder.contactNumber.setText(contact.getTelefono());
        if (contact.getFechaNacimiento() == null) {
            holder.contactBirthday.setText("Fecha de Nacimiento: No disponible");
        } else {
            holder.contactBirthday.setText("Fecha de Nacimiento: " + contact.getFechaNacimiento());
        }
        if (contact.getTipoNotif() == null || contact.getTipoNotif().isEmpty()) {
        holder.contactNotificationType.setText("Aviso: Solo Notificaciones");
        } else {
            holder.contactNotificationType.setText("Aviso: Enviar SMS");
        }


        //async esta un poco desactualizado
        if (contact.getImagenUri() != null) {
            Glide.with(context)
                    .load(contact.getImagenUri())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.contactPhoto);
        } else {
            holder.contactPhoto.setImageResource(R.drawable.ic_launcher_foreground);  // Imagen predeterminada
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView contactPhoto;
        TextView contactName;
        TextView contactNumber;
        TextView contactBirthday;
        TextView contactNotificationType;
    }

}

