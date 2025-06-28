/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
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
    
    private int gerarNovoId(JSONArray ordens) throws JSONException {
        if (ordens == null || ordens.length() == 0) {
            return 1; // Return 1 if array is empty or null (first ID)
        }

        int maiorId = 0;
        for (int i = 0; i < ordens.length(); i++) {
            JSONObject ordem = ordens.getJSONObject(i);
            if (ordem != null && ordem.has("id")) {
                maiorId = Math.max(maiorId, ordem.getInt("id"));
            }
        }
        return maiorId + 1; // Return next available ID
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

        try {
            JSONArray ordens;
            if (Files.exists(caminho)) {
                String conteudo = Files.readString(caminho).trim();
                ordens = conteudo.isEmpty() ? new JSONArray() : new JSONArray(conteudo);
            } else {
                ordens = new JSONArray();
            }
            
            int id = gerarNovoId(ordens);
            
            JSONObject novaOrdem = new JSONObject();
            novaOrdem.put("id", id);
            novaOrdem.put("autor", token);
            novaOrdem.put("descricao", descricao);
            novaOrdem.put("status", "pendente");

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
    
    public JSONObject listarOrdens(JSONObject dados) {
        JSONObject resposta = new JSONObject();
        String token = dados.optString("token", "").trim();
        String filtro = dados.optString("filtro", "").trim();

        if (!usuariosConectados.contains(token)) {
            resposta.put("status", "erro");
            resposta.put("operacao", "listar_ordens");
            resposta.put("mensagem", "Token invalido");
            return resposta;
        }

        if (!Files.exists(caminho)) {
            resposta.put("status", "erro");
            resposta.put("operacao", "listar_ordens");
            resposta.put("mensagem", "Nenhuma ordem disponível");
            return resposta;
        }

        try {
            String conteudo = Files.readString(caminho).trim();
            JSONArray todas = conteudo.isEmpty() ? new JSONArray() : new JSONArray(conteudo);
            JSONArray ordensFiltradas = new JSONArray();
            
            boolean isAdmin = verificarAdm(token);

            for (int i = 0; i < todas.length(); i++) {
                JSONObject ordem = todas.getJSONObject(i);
                String autor = ordem.optString("autor", "");
                String status = ordem.optString("status", "");

                if (isAdmin || autor.equals(token)) {
                    if (filtro.equals("todas") || filtro.equals(status)) {
                        JSONObject o = new JSONObject();
                        o.put("id", ordem.getInt("id"));
                        o.put("autor", autor);
                        o.put("descricao", ordem.getString("descricao"));
                        o.put("status", status);
                        ordensFiltradas.put(o);
                    }
                }
            }

            if (ordensFiltradas.isEmpty()) {
                resposta.put("status", "erro");
                resposta.put("operacao", "listar_ordens");
                resposta.put("mensagem", "Nenhuma ordem disponível");
            } else {
                resposta.put("status", "sucesso");
                resposta.put("operacao", "listar_ordens");
                resposta.put("ordens", ordensFiltradas);
            }

        } catch (Exception e) {
            resposta.put("status", "erro");
            resposta.put("operacao", "listar_ordens");
            resposta.put("mensagem", "Erro ao ler ordens");
        }

        return resposta;
    }
    
    public JSONObject editarOrdem(JSONObject dados){
        
        int id = dados.getInt("id_ordem");
        String novaDescricao = dados.getString("nova_descricao");
        String token = dados.getString("token");
        
        JSONObject resposta = new JSONObject();
        resposta.put("operacao", "editar_ordem");
        
        if (token == null || !usuariosConectados.contains(token)) {
            resposta.put("status", "erro");
            resposta.put("mensagem", "Token invalido");
            return resposta;
        }
        
        if (novaDescricao == null || novaDescricao.length() < 3 || novaDescricao.length() > 150) {
            resposta.put("status", "erro");
            resposta.put("mensagem", "Descrição inválida");
            return resposta;
        }

        try {
            Path path = Path.of("ordens_servico.json");
            if (!Files.exists(path)) {
                resposta.put("status", "erro");
                resposta.put("mensagem", "Ordem não encontrada");
                return resposta;
            }

            String conteudo = Files.readString(path);
            JSONArray ordens = new JSONArray(conteudo);

            for (int i = 0; i < ordens.length(); i++) {
                JSONObject ordem = ordens.getJSONObject(i);
                if (ordem.getInt("id") == id) {

                    // Só o autor pode editar
                    if (!ordem.getString("autor").equals(token)) {
                        resposta.put("status", "erro");
                        resposta.put("mensagem", "Permissão negada");
                        return resposta;
                    }

                    String status = ordem.getString("status");
                    if (status.equals("finalizada") || status.equals("cancelada")) {
                        resposta.put("status", "erro");
                        resposta.put("mensagem", "Ordem já finalizada");
                        return resposta;
                    }

                    ordem.put("descricao", novaDescricao);

                    // Salva de volta
                    Files.writeString(path, ordens.toString(2));

                    resposta.put("status", "sucesso");
                    resposta.put("mensagem", "Ordem editada com sucesso");
                    return resposta;
                }
            }
            
            resposta.put("status", "erro");
            resposta.put("mensagem", "Ordem não encontrada");

        } catch (Exception e) {
            resposta.put("status", "erro");
            resposta.put("mensagem", "Erro ao editar ordem");
            e.printStackTrace();
        }
        
        return resposta;
        
    }

    private boolean verificarAdm(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Path caminho = Path.of("usuarios.json");
        if (!Files.exists(caminho)) {
            return false;
        }

        try {
            String conteudo = Files.readString(caminho).trim();
            if (conteudo.isEmpty()) {
                return false;
            }

            JSONArray usuarios = new JSONArray(conteudo);
            for (int i = 0; i < usuarios.length(); i++) {
                JSONObject usuario = usuarios.getJSONObject(i);
                if (usuario.getString("usuario").equals(token)) {
                    String perfil = usuario.optString("perfil", "comum");
                    return perfil.equalsIgnoreCase("adm");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


}
