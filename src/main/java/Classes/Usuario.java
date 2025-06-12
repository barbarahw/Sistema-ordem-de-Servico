package Classes;

import org.json.JSONObject;

public class Usuario {
    private String usuario;
    private String senha;
    private String nome;
    private String perfil;
    private String token;

    public Usuario(String usuario, String senha, String nome, String perfil) {
        this.usuario = usuario;
        this.senha = senha;
        this.nome = nome;
        this.perfil = perfil != null ? perfil : "comum";
        this.token = null;
    }

    public Usuario(JSONObject json) {
        this.usuario = json.optString("usuario");
        this.senha = json.optString("senha");
        this.nome = json.optString("nome", "");
        this.perfil = json.optString("perfil", "comum");
        this.token = usuario;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("usuario", usuario);
        json.put("senha", senha);
        json.put("nome", nome);
        json.put("perfil", perfil);
        json.put("token", token);
        return json;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    
}
