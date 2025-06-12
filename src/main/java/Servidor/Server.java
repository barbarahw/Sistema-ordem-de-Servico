package Servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class Server {

    private ServerSocket serverSocket;
    private static final Set<String> usuariosConectados = Collections.synchronizedSet(new HashSet<>());

    // Aguarda e retorna um novo socket de conexão
    private Socket esperaConexao() throws IOException {
        return serverSocket.accept();
    }

    // Verifica se usuário existe no arquivo JSON
    private static boolean usuarioExiste(String usuario) {
        Path path = Path.of("usuarios.json");
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

    // Salva novo usuário no arquivo JSON
    private void salvarUsuario(JSONObject novoUsuario) throws IOException {
        Path path = Path.of("usuarios.json");
        JSONArray usuarios;

        if (Files.exists(path)) {
            String conteudo = Files.readString(path);
            usuarios = conteudo.isEmpty() ? new JSONArray() : new JSONArray(conteudo);
        } else {
            usuarios = new JSONArray();
        }

        // Cria um novo objeto só com os campos que devem ser salvos
        JSONObject usuarioParaSalvar = new JSONObject();
        usuarioParaSalvar.put("nome", novoUsuario.getString("nome"));
        usuarioParaSalvar.put("usuario", novoUsuario.getString("usuario"));
        usuarioParaSalvar.put("senha", novoUsuario.getString("senha"));
        usuarioParaSalvar.put("perfil", novoUsuario.getString("perfil"));

        usuarios.put(usuarioParaSalvar);
        Files.writeString(path, usuarios.toString(2));
    }

    // Valida campos do cadastro
    private boolean validarCadastro(JSONObject usuario) {
        String nome = usuario.optString("nome", "");
        String usuarioStr = usuario.optString("usuario", "");
        String senha = usuario.optString("senha", "");

        if (nome.isEmpty() || usuarioStr.isEmpty() || senha.isEmpty()) {
            return false;
        }

        String regexNomeUsuario = "^[a-zA-Z0-9]{3,30}$";
        String regexSenha = "^[a-zA-Z0-9]{4,10}$";

        return nome.matches(regexNomeUsuario) && usuarioStr.matches(regexNomeUsuario) && senha.matches(regexSenha);
    }

    // Valida login
    private boolean validarLogin(JSONObject usuario) {
        Path path = Path.of("usuarios.json");
        if (!Files.exists(path)) {
            return false;
        }

        String usuarioStr = usuario.optString("usuario", "");
        String senha = usuario.optString("senha", "");

        try {
            String conteudo = Files.readString(path);
            JSONArray usuarios = new JSONArray(conteudo);
            for (int i = 0; i < usuarios.length(); i++) {
                JSONObject u = usuarios.getJSONObject(i);
                if (u.getString("usuario").equals(usuarioStr) && u.getString("senha").equals(senha)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Exclui usuário do arquivo JSON
    private boolean excluirUsuario(String usuario) {
        Path path = Path.of("usuarios.json");
        if (!Files.exists(path)) {
            return false;
        }

        try {
            String conteudo = Files.readString(path);
            JSONArray usuarios = new JSONArray(conteudo);
            for (int i = 0; i < usuarios.length(); i++) {
                JSONObject u = usuarios.getJSONObject(i);
                if (u.getString("usuario").equals(usuario)) {
                    usuarios.remove(i);
                    Files.writeString(path, usuarios.toString(2));
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Pega perfil do usuário pelo token (nome do usuário)
    private static JSONObject pegarPerfil(String token) {
        Path path = Path.of("usuarios.json");
        if (!Files.exists(path)) {
            return null;
        }

        try {
            String conteudo = Files.readString(path);
            JSONArray usuarios = new JSONArray(conteudo);
            for (int i = 0; i < usuarios.length(); i++) {
                JSONObject u = usuarios.getJSONObject(i);
                if (u.getString("usuario").equals(token)) {
                    return u;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Valida campos para edição
    private static boolean validarCamposEdicao(String usuario, String nome, String senha) {
        String regexUsuario = "^[a-zA-Z0-9]{3,30}$";
        String regexSenha = "^[a-zA-Z0-9]{4,10}$";

        boolean usuarioValido = usuario.isEmpty() || usuario.matches(regexUsuario);
        boolean nomeValido = nome.isEmpty() || nome.matches(regexUsuario);
        boolean senhaValida = senha.isEmpty() || senha.matches(regexSenha);

        return usuarioValido && nomeValido && senhaValida;
    }

    private void garantirUsuarioAdmin() throws IOException {
        Path path = Path.of("usuarios.json");
        JSONArray usuarios;

        if (Files.exists(path)) {
            String conteudo = Files.readString(path);
            usuarios = conteudo.isEmpty() ? new JSONArray() : new JSONArray(conteudo);
        } else {
            usuarios = new JSONArray();
        }

        boolean adminExiste = false;
        for (int i = 0; i < usuarios.length(); i++) {
            JSONObject u = usuarios.getJSONObject(i);
            if (u.getString("usuario").equals("admin")) {
                adminExiste = true;
                break;
            }
        }

        if (!adminExiste) {
            JSONObject admin = new JSONObject();
            admin.put("nome", "admin");
            admin.put("usuario", "admin");
            admin.put("senha", "12345");
            admin.put("perfil", "adm");

            usuarios.put(admin);
            Files.writeString(path, usuarios.toString(2));

            System.out.println("Usuário admin criado com sucesso.");
        }
    }

    private static boolean validarEdicaoAdmin(JSONObject requisicao, JSONObject perfilADM) {
        // Verifica se o ADM está tentando mudar seu próprio perfil para comum
        if (requisicao.optString("usuario_alvo", "").equals(perfilADM.optString("usuario", ""))) {
            String novoPerfil = requisicao.optString("novo_perfil", "");
            if (novoPerfil.equals("comum")) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));

        Server server = new Server();

        server.garantirUsuarioAdmin();

        System.out.println("Qual porta o servidor deve usar?");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int porta = Integer.parseInt(br.readLine());

        server.serverSocket = new ServerSocket(porta);
        System.out.println("Servidor carregado na porta " + porta);

        while (true) {
            System.out.println("Aguardando conexão");
            Socket socket = server.esperaConexao();
            System.out.println("Cliente conectado");

            try (
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true); BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
                boolean continuar = true;

                while (continuar) {
                    String recebido = input.readLine();
                    if (recebido == null) {
                        break;
                    }
                    System.out.println("JSON recebido: " + recebido);

                    JSONObject requisicao = new JSONObject(recebido);
                    String operacao = requisicao.getString("operacao");
                    JSONObject respostaJson = new JSONObject();

                    switch (operacao) {
                        case "cadastro" -> {
                            if (requisicao.has("token")) {
                                String token = requisicao.getString("token");

                                if (!usuariosConectados.contains(token)) {
                                    respostaJson.put("status", "erro");
                                    respostaJson.put("operacao", "cadastro");
                                    respostaJson.put("mensagem", "Token inválido");
                                    break;
                                }

                                JSONObject perfilADM = pegarPerfil(token);
                                if (perfilADM == null || !perfilADM.optString("perfil").equals("adm")) {
                                    respostaJson.put("status", "erro");
                                    respostaJson.put("operacao", "cadastro");
                                    respostaJson.put("mensagem", "Token de usuário comum");
                                    break;
                                }
                            }

                            if (!requisicao.has("nome") || !requisicao.has("usuario") || !requisicao.has("senha") || !requisicao.has("perfil")) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("operacao", "cadastro");
                                respostaJson.put("mensagem", "Os campos recebidos não são validos");
                                break;
                            }

                            if (!server.validarCadastro(requisicao)) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("operacao", "cadastro");
                                respostaJson.put("mensagem", "Os campos recebidos não são validos");
                                break;
                            }

                            String novoUsuario = requisicao.getString("usuario");

                            if (server.usuarioExiste(novoUsuario)) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("operacao", "cadastro");
                                respostaJson.put("mensagem", "Usuario já cadastrado");
                                break;
                            }

                            server.salvarUsuario(requisicao);

                            respostaJson.put("status", "sucesso");
                            respostaJson.put("operacao", "cadastro");
                            respostaJson.put("mensagem", "Cadastro realizado com sucesso");
                            break;
                        }
                        case "login" -> {
                            String usuario = requisicao.getString("usuario");
                            if (usuariosConectados.contains(usuario)) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("operacao", "login");
                                respostaJson.put("mensagem", "Usuário já logado");
                            } else if (server.validarLogin(requisicao)) {
                                usuariosConectados.add(usuario);
                                JSONObject perfil = pegarPerfil(usuario);

                                respostaJson.put("status", "sucesso");
                                respostaJson.put("operacao", "login");
                                respostaJson.put("token", usuario);
                                respostaJson.put("perfil", perfil.optString("perfil"));
                            } else {
                                respostaJson.put("status", "erro");
                                respostaJson.put("operacao", "login");
                                respostaJson.put("mensagem", "Informações incorretas");
                            }
                        }
                        case "ler_dados" -> {
                            String token = requisicao.getString("token");
                            if (!usuariosConectados.contains(token)) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("operacao", "ler_dados");
                                respostaJson.put("mensagem", "Token inválido");
                            } else {
                                JSONObject perfil = pegarPerfil(token);
                                if (perfil != null) {
                                    respostaJson.put("status", "sucesso");
                                    respostaJson.put("operacao", "ler_dados");

                                    JSONObject dados = new JSONObject();
                                    dados.put("nome", perfil.getString("nome"));
                                    dados.put("usuario", perfil.getString("usuario"));
                                    dados.put("senha", perfil.getString("senha"));

                                    respostaJson.put("dados", dados);
                                } else {
                                    respostaJson.put("status", "erro");
                                    respostaJson.put("mensagem", "Usuário não encontrado");
                                }
                            }
                        }
                        case "excluir_usuario" -> {
                            respostaJson.put("operacao", "excluir_usuario");

                            String token = requisicao.optString("token", "");
                            String usuarioAlvo = requisicao.optString("usuario_alvo", token); // Se não houver, assume autoexclusão

                            if (token.isEmpty() || !usuariosConectados.contains(token)) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Token inválido");
                                break;
                            }

                            JSONObject perfilSolicitante = pegarPerfil(token);
                            if (perfilSolicitante == null) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Token inválido");
                                break;
                            }

                            boolean autoExclusao = usuarioAlvo.equals(token);
                            boolean solicitanteEhADM = perfilSolicitante.optString("perfil").equals("adm");

                            // Regras de permissão
                            if (!autoExclusao && !solicitanteEhADM) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Token não pertence ao usuário ou não tem permissão");
                                break;
                            }

                            if (!autoExclusao && usuarioAlvo.equals(token) && solicitanteEhADM) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Administradores não podem excluir a si mesmos via usuario_alvo");
                                break;
                            }

                            try {
                                Path path = Path.of("usuarios.json");
                                if (!Files.exists(path)) {
                                    respostaJson.put("status", "erro");
                                    respostaJson.put("mensagem", "Arquivo de usuários não encontrado");
                                    break;
                                }

                                JSONArray usuarios = new JSONArray(Files.readString(path));
                                boolean usuarioEncontrado = false;

                                for (int i = 0; i < usuarios.length(); i++) {
                                    JSONObject u = usuarios.getJSONObject(i);
                                    if (u.getString("usuario").equals(usuarioAlvo)) {
                                        usuarios.remove(i);
                                        usuarioEncontrado = true;
                                        break;
                                    }
                                }

                                if (!usuarioEncontrado) {
                                    respostaJson.put("status", "erro");
                                    respostaJson.put("mensagem", "Usuário não encontrado");
                                    break;
                                }

                                // Salvar alterações
                                Files.writeString(path, usuarios.toString(2));
                                usuariosConectados.remove(usuarioAlvo);

                                respostaJson.put("status", "sucesso");
                                respostaJson.put("mensagem", autoExclusao
                                        ? "Conta excluída com sucesso"
                                        : "Usuário excluído com sucesso");

                            } catch (IOException e) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Erro ao acessar os dados: " + e.getMessage());
                            } catch (Exception e) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Erro inesperado: " + e.getMessage());
                            }
                        }

                        case "editar_usuario" -> {
                            String token = requisicao.optString("token", "");
                            respostaJson.put("operacao", "editar_usuario");

                            // Verificação básica do token
                            if (token.isEmpty() || !usuariosConectados.contains(token)) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Token inválido");
                                break;
                            }

                            JSONObject perfilSolicitante = pegarPerfil(token);
                            if (perfilSolicitante == null) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Usuário não encontrado");
                                break;
                            }

                            boolean isAdmin = perfilSolicitante.optString("perfil").equals("adm");
                            String usuarioAlvo = requisicao.optString("usuario_alvo", token); // padrão é o próprio token

                            // Usuário comum só pode editar a si mesmo
                            if (!isAdmin && !usuarioAlvo.equals(token)) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Você só pode editar seu próprio perfil");
                                break;
                            }

                            // Admin não pode se auto-rebaixar
                            String novoPerfil = requisicao.optString("novo_perfil", "").trim();
                            if (isAdmin && usuarioAlvo.equals(token) && novoPerfil.equals("comum")) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Administradores não podem se auto-rebaixar");
                                break;
                            }

                            // Usuário comum não pode alterar perfil
                            if (!isAdmin && requisicao.has("novo_perfil")) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Apenas administradores podem alterar perfis");
                                break;
                            }

                            // Lê os campos enviados
                            String novoNome = requisicao.optString("novo_nome", "").trim();
                            String novaSenha = requisicao.optString("nova_senha", "").trim();
                            String novoUsuario = requisicao.optString("novo_usuario", "").trim();

                            if (novoNome.isEmpty() && novaSenha.isEmpty() && novoPerfil.isEmpty() && novoUsuario.isEmpty()) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Nenhum campo para atualizar foi fornecido");
                                break;
                            }

                            if (!novoPerfil.isEmpty() && !novoPerfil.equals("adm") && !novoPerfil.equals("comum")) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Perfil inválido (deve ser 'adm' ou 'comum')");
                                break;
                            }

                            try {
                                Path path = Path.of("usuarios.json");
                                JSONArray usuarios = new JSONArray(Files.readString(path));
                                boolean usuarioEncontrado = false;

                                // Verifica se o novo nome de usuário já existe
                                if (!novoUsuario.isEmpty()) {
                                    for (int i = 0; i < usuarios.length(); i++) {
                                        JSONObject u = usuarios.getJSONObject(i);
                                        if (u.getString("usuario").equals(novoUsuario)) {
                                            respostaJson.put("status", "erro");
                                            respostaJson.put("mensagem", "Usuário já cadastrado");
                                            return;
                                        }
                                    }
                                }

                                for (int i = 0; i < usuarios.length(); i++) {
                                    JSONObject u = usuarios.getJSONObject(i);
                                    if (u.getString("usuario").equals(usuarioAlvo)) {
                                        usuarioEncontrado = true;

                                        if (!novoNome.isEmpty()) {
                                            u.put("nome", novoNome);
                                        }
                                        if (!novaSenha.isEmpty()) {
                                            u.put("senha", novaSenha);
                                        }
                                        if (!novoPerfil.isEmpty()) {
                                            u.put("perfil", novoPerfil);
                                        }
                                        if (!novoUsuario.isEmpty()) {
                                            u.put("usuario", novoUsuario);
                                        }

                                        usuarios.put(i, u);
                                        Files.writeString(path, usuarios.toString(2));

                                        // Atualizar token se o nome de usuário foi alterado
                                        if (!novoUsuario.isEmpty()) {
                                            usuariosConectados.remove(token);
                                            usuariosConectados.add(novoUsuario);
                                            respostaJson.put("token", novoUsuario);
                                        } else {
                                            respostaJson.put("token", token);
                                        }

                                        respostaJson.put("status", "sucesso");
                                        respostaJson.put("mensagem", "Dados atualizados com sucesso");
                                        break;
                                    }
                                }

                                if (!usuarioEncontrado) {
                                    respostaJson.put("status", "erro");
                                    respostaJson.put("mensagem", "Usuário alvo não encontrado");
                                }

                            } catch (Exception e) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Erro ao atualizar usuário: " + e.getMessage());
                            }
                        }

                        case "logout" -> {
                            String token = requisicao.optString("token");
                            respostaJson.put("operacao", "logout");
                            if (usuariosConectados.contains(token)) {
                                usuariosConectados.remove(token);
                                respostaJson.put("status", "sucesso");
                                respostaJson.put("mensagem", "Logout realizado");

                            } else {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Token inválido");
                            }
                        }
                        case "listar_usuarios" -> {
                            String token = requisicao.optString("token", "");
                            respostaJson.put("operacao", "listar_usuarios");

                            if (token.isEmpty() || !usuariosConectados.contains(token)) {
                                respostaJson.put("status", "erro");
                                respostaJson.put("mensagem", "Token inválido");
                            } else {
                                JSONObject perfil = pegarPerfil(token);
                                if (perfil == null || !perfil.optString("perfil").equals("adm")) {
                                    respostaJson.put("status", "erro");
                                    respostaJson.put("mensagem", "Token de usuário comum");
                                } else {
                                    Path path = Path.of("usuarios.json");
                                    if (!Files.exists(path)) {
                                        respostaJson.put("status", "erro");
                                        respostaJson.put("mensagem", "Nenhum usuario cadastrado");
                                    } else {
                                        try {
                                            String conteudo = Files.readString(path);
                                            JSONArray usuarios = new JSONArray(conteudo);

                                            // Criar array sem informações sensíveis
                                            JSONArray usuariosPublicos = new JSONArray();
                                            for (int i = 0; i < usuarios.length(); i++) {
                                                JSONObject u = usuarios.getJSONObject(i);
                                                JSONObject usuarioPublico = new JSONObject();
                                                usuarioPublico.put("nome", u.getString("nome"));
                                                usuarioPublico.put("usuario", u.getString("usuario"));
                                                usuarioPublico.put("perfil", u.getString("perfil"));
                                                usuariosPublicos.put(usuarioPublico);
                                            }

                                            respostaJson.put("status", "sucesso");
                                            respostaJson.put("usuarios", usuariosPublicos);
                                        } catch (Exception e) {
                                            respostaJson.put("status", "erro");
                                            respostaJson.put("mensagem", "Erro ao ler usuarios: " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                        default -> {
                            respostaJson.put("status", "erro");
                            respostaJson.put("mensagem", "Operação desconhecida");
                        }
                    }

                    String resposta = respostaJson.toString();
                    System.out.println("Resposta enviada: " + resposta);
                    output.println(resposta);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Conexão encerrada");
                socket.close();
            }
        }
    }
}
