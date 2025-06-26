/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author bwosi
 */
public class OrdemServicoService {

    private final Set<String> usuariosConectados;
    private final Path caminho = Path.of("ordens_servico.json");

    public OrdemServicoService(Set<String> usuariosConectados) {
        this.usuariosConectados = usuariosConectados;
    }

    public JSONObject cadastrarOrdem(JSONObject dados) {
        JSONObject resposta = new JSONObject();

        String token = dados.optString("token", "").trim();
        String descricao = dados.optString("descricao", "").trim();

        if (!usuariosConectados.contains(token)) {
            resposta.put("status", "erro");
            resposta.put("operacao", "cadastrar_ordem");
            resposta.put("mensagem", "Token invalido");
            return resposta;
        }

        if (descricao.length() < 3 || descricao.length() > 150) {
            resposta.put("status", "erro");
            resposta.put("operacao", "cadastrar_ordem");
            resposta.put("mensagem", "Descrição inválida");
            return resposta;
        }

        JSONObject novaOrdem = new JSONObject();
        novaOrdem.put("usuario", token);
        novaOrdem.put("descricao", descricao);
        novaOrdem.put("status", "pendente");

        try {
            JSONArray ordens;
            if (Files.exists(caminho)) {
                String conteudo = Files.readString(caminho).trim();
                ordens = conteudo.isEmpty() ? new JSONArray() : new JSONArray(conteudo);
            } else {
                ordens = new JSONArray();
            }

            ordens.put(novaOrdem);
            Files.writeString(caminho, ordens.toString(2));

            resposta.put("status", "sucesso");
            resposta.put("operacao", "cadastrar_ordem");
            resposta.put("mensagem", "Ordem cadastrada com sucesso");
        } catch (Exception e) {
            resposta.put("status", "erro");
            resposta.put("operacao", "cadastrar_ordem");
            resposta.put("mensagem", "Erro ao salvar ordem");
        }

        return resposta;
    }
}
