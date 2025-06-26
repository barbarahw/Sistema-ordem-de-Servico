/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author bwosi
 */
public class UsuarioDAO {

    private final Path path = Path.of("usuarios.json");

    public boolean usuarioExiste(String usuario) {
        if (!Files.exists(path)) {
            return false;
        }
        try {
            String conteudo = Files.readString(path).trim();
            if (conteudo.isEmpty()) {
                return false;
            }

            JSONArray usuarios = new JSONArray(conteudo);
            for (int i = 0; i < usuarios.length(); i++) {
                JSONObject u = usuarios.getJSONObject(i);
                if (u.getString("usuario").equals(usuario)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONObject buscarUsuario(String usuario) {
        if (!Files.exists(path)) {
            return null;
        }
        try {
            String conteudo = Files.readString(path).trim();
            if (conteudo.isEmpty()) {
                return null;
            }

            JSONArray usuarios = new JSONArray(conteudo);
            for (int i = 0; i < usuarios.length(); i++) {
                JSONObject u = usuarios.getJSONObject(i);
                if (u.getString("usuario").equals(usuario)) {
                    return u;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void salvarUsuario(JSONObject novoUsuario) throws IOException {
        JSONArray usuarios;
        if (Files.exists(path)) {
            String conteudo = Files.readString(path);
            usuarios = conteudo.isEmpty() ? new JSONArray() : new JSONArray(conteudo);
        } else {
            usuarios = new JSONArray();
        }

        JSONObject usuarioParaSalvar = new JSONObject();
        usuarioParaSalvar.put("nome", novoUsuario.getString("nome"));
        usuarioParaSalvar.put("usuario", novoUsuario.getString("usuario"));
        usuarioParaSalvar.put("senha", novoUsuario.getString("senha"));
        usuarioParaSalvar.put("perfil", novoUsuario.getString("perfil"));

        usuarios.put(usuarioParaSalvar);
        Files.writeString(path, usuarios.toString(2));
    }
}