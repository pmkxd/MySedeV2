package com.test.mysede.usuarios;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.mysede.R;
import com.test.mysede.model.Usuario;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private Context context;
    private List<Usuario> usuarios;
    private OnUsuarioClickListener listener;

    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario, int posicion);
    }

    public UsuarioAdapter(Context context, List<Usuario> usuarios, OnUsuarioClickListener listener) {
        this.context = context;
        this.usuarios = usuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position);

        holder.tvNombre.setText(usuario.getNombre());
        holder.tvEmail.setText(usuario.getEmail());
        holder.tvRol.setText(usuario.getRol().getNombreCompleto());

        // Mostrar estado
        if (usuario.isActivo()) {
            holder.tvEstado.setText("Activo");
            holder.tvEstado.setTextColor(context.getColor(R.color.md_theme_primary));
        } else {
            holder.tvEstado.setText("Inactivo");
            holder.tvEstado.setTextColor(context.getColor(R.color.md_theme_error));
        }

        // Mostrar cantidad de permisos
        int cantidadPermisos = usuario.getPermisos().size();
        holder.tvPermisos.setText(cantidadPermisos + " permisos");

        // Icono según rol
        int iconoResId = obtenerIconoPorRol(usuario.getRol().name());
        holder.ivIcono.setImageResource(iconoResId);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUsuarioClick(usuario, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    private int obtenerIconoPorRol(String rol) {
        switch (rol) {
            case "ADMINISTRADOR":
                return android.R.drawable.ic_menu_manage;
            case "ORGANIZADOR_ACTIVIDADES":
                return android.R.drawable.ic_menu_edit;
            case "PROGRAMADOR_CITAS":
                return android.R.drawable.ic_menu_today;
            case "PUBLICISTA":
                return android.R.drawable.ic_menu_view;
            default:
                return android.R.drawable.ic_menu_info_details;
        }
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcono;
        TextView tvNombre;
        TextView tvEmail;
        TextView tvRol;
        TextView tvEstado;
        TextView tvPermisos;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcono = itemView.findViewById(R.id.ivIconoUsuario);
            tvNombre = itemView.findViewById(R.id.tvNombreUsuario);
            tvEmail = itemView.findViewById(R.id.tvEmailUsuario);
            tvRol = itemView.findViewById(R.id.tvRolUsuario);
            tvEstado = itemView.findViewById(R.id.tvEstadoUsuario);
            tvPermisos = itemView.findViewById(R.id.tvPermisosUsuario);
        }
    }
}