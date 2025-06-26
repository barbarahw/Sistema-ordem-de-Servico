/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Services;

import DAO.UsuarioDAO;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author bwosi
 */
public class UsuarioService {

    private final Set<String> usuariosConectados;
    private final UsuarioDAO usuarioDAO;

    public UsuarioService(Set<String> usuariosConectados) {
        this.usuariosConectados = usuariosConectados;
        this.usuarioDAO = new UsuarioDAO();
    }

    private boolean validaCampo(String valor, int min, int max) {
        // Sem espaços e sem caracteres especiais, só letras e números (exemplo)
        return valor != null
                && valor.length() >= min
                && valor.length() <= max
                && valor.matches("[a-zA-Z0-9]+");
    }

    
    public JSONObject login(JSONObject dados) {
        JSONObject resposta = new JSONObject();

        String usuario = dados.optString("usuario", "").trim();
        String senha = dados.optString("senha", "").trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            resposta.put("status", "erro");
            resposta.put("operacao", "login");
            resposta.put("mensagem", "Informações incorretas");
            return resposta;
        }

        if (usuariosConectados.contains(usuario)) {
            resposta.put("status", "erro");
            resposta.put("operacao", "login");
            resposta.put("mensagem", "Usuario já logado");
            return resposta;
        }

        JSONObject usuarioEncontrado = usuarioDAO.buscarUsuario(usuario);

        if (usuarioEncontrado != null && usuarioEncontrado.getString("senha").equals(senha)) {
            usuariosConectados.add(usuario);

            resposta.put("status", "sucesso");
            resposta.put("operacao", "login");
            resposta.put("token", usuario);
            resposta.put("perfil", usuarioEncontrado.getString("perfil"));
        } else {
            resposta.put("status", "erro");
            resposta.put("operacao", "login");
            resposta.put("mensagem", "Informações incorretas");
        }

        return resposta;
    }
    
    
    public JSONObject logout(JSONObject dados) {
        JSONObject resposta = new JSONObject();

        String token = dados.optString("token", "").trim();

        if (token.isEmpty() || !usuariosConectados.contains(token)) {
            resposta.put("status", "erro");
            resposta.put("operacao", "logout");
            resposta.put("mensagem", "Token invalido");
            return resposta;
        }

        usuariosConectados.remove(token);

        resposta.put("status", "sucesso");
        resposta.put("operacao", "logout");
        resposta.put("mensagem", "Logout realizado com sucesso");

        return resposta;
    }
    
    public JSONObject cadastro(JSONObject dados) {
        JSONObject resposta = new JSONObject();

        String nome = dados.optString("nome", "").trim();
        String usuario = dados.optString("usuario", "").trim();
        String senha = dados.optString("senha", "").trim();
        // perfil será sempre "comum" para cadastro usuário comum
        String perfil = "comum";

        // Valida os campos
        if (!validaCampo(nome, 3, 30) || !validaCampo(usuario, 3, 30) || !validaCampo(senha, 4, 10)) {
            resposta.put("status", "erro");
            resposta.put("operacao", "cadastro");
            resposta.put("mensagem", "Os campos recebidos não são validos");
            return resposta;
        }

        // Verifica se usuário já existe
        if (usuarioDAO.usuarioExiste(usuario)) {
            resposta.put("status", "erro");
            resposta.put("operacao", "cadastro");
            resposta.put("mensagem", "Usuario já cadastrado");
            return resposta;
        }

        // Monta o objeto para salvar
        JSONObject novoUsuario = new JSONObject();
        novoUsuario.put("nome", nome);
        novoUsuario.put("usuario", usuario);
        novoUsuario.put("senha", senha);
        novoUsuario.put("perfil", perfil);

        try {
            usuarioDAO.salvarUsuario(novoUsuario);
            resposta.put("status", "sucesso");
            resposta.put("operacao", "cadastro");
            resposta.put("mensagem", "Cadastro realizado com sucesso");
        } catch (Exception e) {
            resposta.put("status", "erro");
            resposta.put("operacao", "cadastro");
            resposta.put("mensagem", "Erro ao salvar usuário");
        }

        return resposta;
    }


}
