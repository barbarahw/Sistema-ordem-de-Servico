/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import Services.UsuarioService;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author bwosi
 */
public class RequisicaoController {

    private final Set<String> usuariosConectados;
    private final UsuarioService usuarioService;

    public RequisicaoController(Set<String> usuariosConectados) {
        this.usuariosConectados = usuariosConectados;
        this.usuarioService = new UsuarioService(usuariosConectados);
    }

    public JSONObject processar(JSONObject requisicao) {
        String operacao = requisicao.optString("operacao", "");
        JSONObject resposta;

        switch (operacao) {
            case "login":
                resposta = usuarioService.login(requisicao);
                break;
            case "logout":
                resposta = usuarioService.logout(requisicao);
                break;
            case "cadastro":
                resposta = usuarioService.cadastro(requisicao);
                break;
            default:
                resposta = new JSONObject();
                resposta.put("status", "erro");
                resposta.put("operacao", operacao);
                resposta.put("mensagem", "Operação desconhecida");
                break;
        }
        return resposta;
    }
}
