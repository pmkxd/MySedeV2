package com.test.mysede.usuarios;

import com.test.mysede.auth.PlantillaPermisos;
import com.test.mysede.auth.Rol;
import com.test.mysede.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper para generar usuarios de prueba del sistema
 * Incluye ejemplos de cada rol definido
 */
public class UsuarioHelper {

    private static List<Usuario> usuariosPrueba;

    public static List<Usuario> obtenerUsuariosPrueba() {
        if (usuariosPrueba == null) {
            usuariosPrueba = generarUsuariosPrueba();
        }
        return usuariosPrueba;
    }

    private static List<Usuario> generarUsuariosPrueba() {
        List<Usuario> usuarios = new ArrayList<>();

        // Usuario 1: Administrador
        Usuario admin = new Usuario(
                "María González",
                "maria.gonzalez@alerce.cl",
                Rol.ADMINISTRADOR
        );
        admin.setPermisos(PlantillaPermisos.obtenerPermisosPorRol(Rol.ADMINISTRADOR));
        usuarios.add(admin);

        // Usuario 2: Organizador de Actividades
        Usuario organizador = new Usuario(
                "Carlos Pérez",
                "carlos.perez@alerce.cl",
                Rol.ORGANIZADOR_ACTIVIDADES
        );
        organizador.setPermisos(PlantillaPermisos.obtenerPermisosPorRol(Rol.ORGANIZADOR_ACTIVIDADES));
        usuarios.add(organizador);

        // Usuario 3: Programador de Citas
        Usuario programador = new Usuario(
                "Ana Martínez",
                "ana.martinez@alerce.cl",
                Rol.PROGRAMADOR_CITAS
        );
        programador.setPermisos(PlantillaPermisos.obtenerPermisosPorRol(Rol.PROGRAMADOR_CITAS));
        usuarios.add(programador);

        // Usuario 4: Publicista
        Usuario publicista = new Usuario(
                "Roberto Silva",
                "roberto.silva@alerce.cl",
                Rol.PUBLICISTA
        );
        publicista.setPermisos(PlantillaPermisos.obtenerPermisosPorRol(Rol.PUBLICISTA));
        usuarios.add(publicista);

        // Usuario 5: Otro Organizador
        Usuario organizador2 = new Usuario(
                "Laura Fernández",
                "laura.fernandez@alerce.cl",
                Rol.ORGANIZADOR_ACTIVIDADES
        );
        organizador2.setPermisos(PlantillaPermisos.obtenerPermisosPorRol(Rol.ORGANIZADOR_ACTIVIDADES));
        usuarios.add(organizador2);

        return usuarios;
    }

    public static Usuario obtenerUsuarioPorIndice(int indice) {
        List<Usuario> usuarios = obtenerUsuariosPrueba();
        if (indice >= 0 && indice < usuarios.size()) {
            return usuarios.get(indice);
        }
        return null;
    }

    public static Usuario obtenerUsuarioPorEmail(String email) {
        List<Usuario> usuarios = obtenerUsuariosPrueba();
        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(email)) {
                return usuario;
            }
        }
        return null;
    }

    public static void agregarUsuario(Usuario usuario) {
        if (usuariosPrueba == null) {
            usuariosPrueba = generarUsuariosPrueba();
        }
        usuariosPrueba.add(usuario);
    }

    public static void actualizarUsuario(int indice, Usuario usuario) {
        if (usuariosPrueba != null && indice >= 0 && indice < usuariosPrueba.size()) {
            usuariosPrueba.set(indice, usuario);
        }
    }

    public static void eliminarUsuario(int indice) {
        if (usuariosPrueba != null && indice >= 0 && indice < usuariosPrueba.size()) {
            usuariosPrueba.remove(indice);
        }
    }

    public static int contarUsuarios() {
        return obtenerUsuariosPrueba().size();
    }

    public static List<Usuario> obtenerUsuariosPorRol(Rol rol) {
        List<Usuario> usuariosFiltrados = new ArrayList<>();
        for (Usuario usuario : obtenerUsuariosPrueba()) {
            if (usuario.getRol() == rol) {
                usuariosFiltrados.add(usuario);
            }
        }
        return usuariosFiltrados;
    }
}