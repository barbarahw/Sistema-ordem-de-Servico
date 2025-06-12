package Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import Classes.Operacao;
import Classes.Usuario;
import org.json.JSONArray;

public class Cliente {

    private static Usuario usuarioLogado = null;

    private static JSONObject cadastro(Scanner sc) {
        JSONObject json = new JSONObject();

        json.put("operacao", "cadastro");

        System.out.print("Nome: ");
        String nome = sc.nextLine();

        System.out.print("Usuário: ");
        String usuario = sc.nextLine();

        System.out.print("Senha: ");
        String senha = sc.nextLine();

        String perfil = "comum";

        json.put("nome", nome);
        json.put("usuario", usuario);
        json.put("senha", senha);
        json.put("perfil", perfil);

        return json;
    }

    private static JSONObject login(Scanner sc) {
        JSONObject json = new JSONObject();

        json.put("operacao", "login");

        System.out.print("Usuário: ");
        String usuario = sc.nextLine();

        System.out.print("Senha: ");
        String senha = sc.nextLine();

        json.put("usuario", usuario);
        json.put("senha", senha);

        return json;
    }

    private static JSONObject deslogar(Usuario usuario) {
        JSONObject json = new JSONObject();

        json.put("operacao", "logout");
        json.put("token", usuario.getToken());

        return json;
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Qual o IP do servidor? ");
            String ip = scanner.nextLine();

            System.out.print("Qual a porta do servidor? ");
            int porta = Integer.parseInt(scanner.nextLine());

            try(Socket socket = new Socket(ip, porta); 
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true); 
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                System.out.println("Conectado com sucesso!");

                boolean logado = false;

                while (true) {
                    if (!logado) {
                        System.out.println("\nEscolha a operação:");
                        System.out.println("1 - Cadastro");
                        System.out.println("2 - Login");

                        String escolha = scanner.nextLine();
                        JSONObject json;

                        switch (escolha) {
                            case "1" ->
                                json = cadastro(scanner);
                            case "2" ->
                                json = login(scanner);
                            default -> {
                                System.out.println("Operação inválida.");
                                continue;
                            }
                        }

                        System.out.println("Enviado ao servidor: " + json.toString());
                        output.println(json.toString());

                        String resposta = input.readLine();
                        if (resposta == null) {
                            System.out.println("Conexão encerrada pelo servidor.");
                            break;
                        }
                        System.out.println("Resposta do servidor: " + resposta);

                        JSONObject respostaJson = new JSONObject(resposta);
                        Operacao operacaoResposta = new Operacao(
                                respostaJson.optString("operacao", ""),
                                respostaJson.optString("mensagem", ""),
                                respostaJson.optString("status", "")
                        );

                        System.out.println("Mensagem: " + operacaoResposta.getMensagem());

                        if (operacaoResposta.getStatus().equals("sucesso") && escolha.equals("2")) {
                            String token = respostaJson.optString("token", "");
                            String usuarioNome = respostaJson.optString("usuario", "");
                            String senha = respostaJson.optString("senha", "");
                            String perfil = respostaJson.optString("perfil", "");
                            String nome = respostaJson.optString("nome", ""); // se enviar

                            // Cria objeto usuário com os dados corretos (não passa token no construtor)
                            usuarioLogado = new Usuario(usuarioNome, senha, nome, perfil);

                            // Define o token no objeto depois
                            usuarioLogado.setToken(token);

                            logado = true;

                            if (usuarioLogado.getPerfil().equals("adm")) {
                                System.out.println("\n=== Bem-vindo, administrador ===");
                                boolean sairAdmin = false;

                                while (!sairAdmin) {
                                    System.out.println("\n--- Menu do Administrador ---");
                                    System.out.println("1. Cadastrar novo usuário");
                                    System.out.println("2. Listar todos os usuários");
                                    System.out.println("3. Atualizar usuário");
                                    System.out.println("4. Excluir usuário");
                                    System.out.println("5. Logout");

                                    escolha = scanner.nextLine();

                                    switch (escolha) {
                                        case "1" -> {
                                            System.out.println("Cadastro de novo usuário:");

                                            System.out.print("Nome: ");
                                            nome = scanner.nextLine();

                                            System.out.print("Usuário: ");
                                            String novoUsuario = scanner.nextLine();

                                            System.out.print("Senha: ");
                                            String novaSenha = scanner.nextLine();

                                            perfil = "";
                                            while (!(perfil.equals("adm") || perfil.equals("comum"))) {
                                                System.out.print("Perfil (adm/comum): ");
                                                perfil = scanner.nextLine().toLowerCase();
                                                if (!perfil.equals("adm") && !perfil.equals("comum")) {
                                                    System.out.println("Perfil inválido. Digite 'adm' ou 'comum'.");
                                                }
                                            }

                                            JSONObject novaConta = new JSONObject();
                                            novaConta.put("operacao", "cadastro");
                                            novaConta.put("token", usuarioLogado.getToken()); // token do ADM autenticado
                                            novaConta.put("nome", nome);
                                            novaConta.put("usuario", novoUsuario);
                                            novaConta.put("senha", novaSenha);
                                            novaConta.put("perfil", perfil);

                                            output.println(novaConta.toString());
                                            resposta = input.readLine();
                                            JSONObject respostaCadastro = new JSONObject(resposta);
                                            System.out.println(respostaCadastro.getString("mensagem"));

                                            System.out.println("Enviado ao servidor: " + novaConta.toString());

                                            System.out.println("Resposta do servidor: " + resposta);
                                            break;
                                        }
                                        case "2" -> {
                                            JSONObject listarReq = new JSONObject();
                                            listarReq.put("operacao", "listar_usuarios");
                                            listarReq.put("token", usuarioLogado.getToken());

                                            System.out.println("Enviado ao servidor: " + listarReq.toString());
                                            output.println(listarReq.toString());

                                            String respostaLista = input.readLine();
                                            if (respostaLista == null) {
                                                System.out.println("Conexão encerrada pelo servidor.");
                                                sairAdmin = true;
                                                break;
                                            }

                                            JSONObject respostaJsonLista = new JSONObject(respostaLista);
                                            String status = respostaJsonLista.optString("status", "erro");
                                            String mensagem = respostaJsonLista.optString("mensagem", "");

                                            System.out.println("Resposta do servidor: " + respostaLista);

                                            if (status.equals("sucesso")) {
                                                JSONArray usuarios = respostaJsonLista.getJSONArray("usuarios");
                                                System.out.println("\n=== LISTA DE USUÁRIOS ===");
                                                for (int i = 0; i < usuarios.length(); i++) {
                                                    JSONObject u = usuarios.getJSONObject(i);
                                                    System.out.println("Nome: " + u.getString("nome"));
                                                    System.out.println("Usuário: " + u.getString("usuario"));
                                                    System.out.println("Perfil: " + u.getString("perfil"));
                                                    System.out.println("------------------------");
                                                }
                                            } else {
                                                System.out.println("Erro: " + mensagem);
                                            }
                                        }

                                        case "3" -> {
                                            System.out.println("\n=== ATUALIZAR USUÁRIO ===");

                                            // Listar usuários primeiro
                                            JSONObject listarReq = new JSONObject();
                                            listarReq.put("operacao", "listar_usuarios");
                                            listarReq.put("token", usuarioLogado.getToken());

                                            output.println(listarReq.toString());
                                            String respostaLista = input.readLine();
                                            if (respostaLista == null) {
                                                System.out.println("Conexão encerrada pelo servidor.");
                                                sairAdmin = true;
                                                break;
                                            }

                                            JSONObject respostaJsonLista = new JSONObject(respostaLista);

                                            if (respostaJsonLista.getString("status").equals("sucesso")) {
                                                System.out.println("\nUsuários disponíveis:");
                                                JSONArray usuarios = respostaJsonLista.getJSONArray("usuarios");
                                                for (int i = 0; i < usuarios.length(); i++) {
                                                    JSONObject u = usuarios.getJSONObject(i);
                                                    System.out.println("- " + u.getString("usuario") + " (" + u.getString("nome") + ")");
                                                }
                                            } else {
                                                System.out.println("Erro ao listar usuários: " + respostaJsonLista.optString("mensagem", ""));
                                            }

                                            System.out.print("\nDigite o usuário que deseja atualizar: ");
                                            String usuarioAlvo = scanner.nextLine();

                                            System.out.println("\nDeixe em branco os campos que não deseja alterar:");

                                            System.out.print("Novo nome: ");
                                            String novoNome = scanner.nextLine();

                                            System.out.print("Nova senha: ");
                                            String novaSenha = scanner.nextLine();

                                            String novoPerfil = "";
                                            System.out.print("Novo perfil (adm/comum): ");
                                            String perfilInput = scanner.nextLine().trim();
                                            if (!perfilInput.isEmpty()) {
                                                while (!(perfilInput.equals("adm") || perfilInput.equals("comum"))) {
                                                    System.out.println("Perfil inválido. Digite 'adm' ou 'comum'.");
                                                    System.out.print("Novo perfil (adm/comum): ");
                                                    perfilInput = scanner.nextLine().trim();
                                                }
                                                novoPerfil = perfilInput;
                                            }

                                            // Confirmação
                                            System.out.println("\nConfirmação de atualização:");
                                            System.out.println("Usuário: " + usuarioAlvo);
                                            if (!novoNome.isEmpty()) {
                                                System.out.println("Novo nome: " + novoNome);
                                            }
                                            if (!novaSenha.isEmpty()) {
                                                System.out.println("Nova senha: ******");
                                            }
                                            if (!novoPerfil.isEmpty()) {
                                                System.out.println("Novo perfil: " + novoPerfil);
                                            }

                                            JSONObject atualizarReq = new JSONObject();
                                            atualizarReq.put("operacao", "editar_usuario");
                                            atualizarReq.put("token", usuarioLogado.getToken());
                                            atualizarReq.put("usuario_alvo", usuarioAlvo);
                                            if (!novoNome.isEmpty()) {
                                                atualizarReq.put("novo_nome", novoNome);
                                            }
                                            if (!novaSenha.isEmpty()) {
                                                atualizarReq.put("nova_senha", novaSenha);
                                            }
                                            if (!novoPerfil.isEmpty()) {
                                                atualizarReq.put("novo_perfil", novoPerfil);
                                            }

                                            System.out.println("Enviado ao servidor: " + atualizarReq.toString());
                                            output.println(atualizarReq.toString());

                                            String respostaAtualizacao = input.readLine();
                                            if (respostaAtualizacao == null) {
                                                System.out.println("Conexão encerrada pelo servidor.");
                                                sairAdmin = true;
                                                break;
                                            }

                                            JSONObject respostaJsonAtualizacao = new JSONObject(respostaAtualizacao);
                                            System.out.println("Resposta do servidor: " + respostaJsonAtualizacao.toString());

                                            if (respostaJsonAtualizacao.getString("status").equals("sucesso")) {
                                                System.out.println("Usuário atualizado com sucesso!");

                                                // Se atualizou o próprio perfil, atualiza localmente
                                                if (usuarioAlvo.equals(usuarioLogado.getUsuario())) {
                                                    if (!novoNome.isEmpty()) {
                                                        usuarioLogado.setNome(novoNome);
                                                    }
                                                    if (!novaSenha.isEmpty()) {
                                                        usuarioLogado.setSenha(novaSenha);
                                                    }
                                                }
                                            } else {
                                                System.out.println("Erro: " + respostaJsonAtualizacao.getString("mensagem"));
                                            }
                                        }
                                        case "4" -> {

                                            System.out.println("informe o usuário a ser excluído");
                                            String usuarioAlvo = scanner.nextLine();

                                            JSONObject jsonExclusao = new JSONObject();
                                            jsonExclusao.put("operacao", "excluir_usuario");
                                            jsonExclusao.put("token", usuarioLogado.getToken());
                                            jsonExclusao.put("usuario_alvo", usuarioAlvo);

                                            System.out.println("Enviado ao servidor: " + jsonExclusao.toString());
                                            output.println(jsonExclusao.toString());

                                            System.out.println("Resposta do servidor: " + input.readLine().toString());
                                        }

                                        case "5" -> {
                                            JSONObject logoutReq = new JSONObject();
                                            logoutReq.put("operacao", "logout");
                                            logoutReq.put("token", usuarioLogado.getToken());

                                            System.out.println("Enviado ao servidor: " + logoutReq.toString());
                                            output.println(logoutReq.toString());

                                            String respostaLogout = input.readLine();
                                            if (respostaLogout == null) {
                                                System.out.println("Conexão encerrada pelo servidor.");
                                                sairAdmin = true;
                                                logado = false;
                                                break;
                                            }

                                            JSONObject respostaJsonLogout = new JSONObject(respostaLogout);
                                            String status = respostaJsonLogout.optString("status", "erro");
                                            String mensagem = respostaJsonLogout.optString("mensagem", "");

                                            System.out.println("Resposta do servidor: " + respostaJsonLogout.toString());

                                            if (status.equals("sucesso")) {
                                                System.out.println("Logout realizado com sucesso!");
                                                usuarioLogado = null;
                                                logado = false;  // Esta linha é crucial para retornar ao menu principal
                                                sairAdmin = true; // Sai do loop do menu admin
                                            } else {
                                                System.out.println("Erro ao deslogar: " + mensagem);
                                            }
                                        }

                                    }
                                }
                                continue;
                            } else {
                                System.out.println("\n--- Menu do Usuário ---");
                                System.out.println("1 - Ler dados");
                                System.out.println("2 - Editar Perfil");
                                System.out.println("3 - Excluir Perfil");
                                System.out.println("4 - Logout");

                                escolha = scanner.nextLine();
                                json = new JSONObject();
                                json.put("token", usuarioLogado.getToken());

                                switch (escolha) {
                                    case "1" ->
                                        json.put("operacao", "ler_dados");

                                    case "2" -> {
                                        JSONObject jsonEdicao = new JSONObject();
                                        jsonEdicao.put("operacao", "editar_usuario");
                                        jsonEdicao.put("token", usuarioLogado.getToken());

                                        System.out.print("Novo usuário (deixe em branco para manter): ");
                                        String novoUser = scanner.nextLine();
                                        if (!novoUser.isEmpty()) {
                                            jsonEdicao.put("novo_usuario", novoUser);
                                        }

                                        System.out.print("Novo nome (deixe em branco para manter): ");
                                        String novoNome = scanner.nextLine();
                                        if (!novoNome.isEmpty()) {
                                            jsonEdicao.put("novo_nome", novoNome);
                                        }

                                        System.out.print("Nova senha (deixe em branco para manter): ");
                                        String novaSenha = scanner.nextLine();
                                        if (!novaSenha.isEmpty()) {
                                            jsonEdicao.put("nova_senha", novaSenha);
                                        }

                                        if (novoUser.isEmpty() && novoNome.isEmpty() && novaSenha.isEmpty()) {
                                            System.out.println("Nenhum campo foi alterado. Edição cancelada.");
                                            continue;
                                        }

                                        System.out.println("Enviado ao servidor: " + jsonEdicao.toString());
                                        output.println(jsonEdicao.toString());

                                        String respostaEdicao = input.readLine();
                                        if (respostaEdicao == null) {
                                            System.out.println("Conexão encerrada pelo servidor.");
                                            return;
                                        }

                                        JSONObject respostaJsonEdicao = new JSONObject(respostaEdicao);

                                        Operacao operacaoEdicao = new Operacao(
                                                respostaJsonEdicao.optString("operacao", ""),
                                                respostaJsonEdicao.optString("mensagem", ""),
                                                respostaJsonEdicao.optString("status", "")
                                        );

                                        System.out.println("Resposta do servidor: " + respostaJsonEdicao.toString());

                                        if (operacaoEdicao.getStatus().equals("sucesso")) {
                                            if (respostaJsonEdicao.has("token")) {
                                                usuarioLogado.setToken(respostaJsonEdicao.getString("token"));
                                            }
                                            if (!novoUser.isEmpty()) {
                                                usuarioLogado.setUsuario(novoUser);
                                            }
                                            if (!novaSenha.isEmpty()) {
                                                usuarioLogado.setSenha(novaSenha);
                                            }
                                            System.out.println("Perfil atualizado com sucesso!");
                                        } else {
                                            System.out.println("Erro: " + operacaoEdicao.getMensagem());
                                        }
                                        continue;
                                    }

                                    case "3" -> {
                                        System.out.print("Deseja excluir seu perfil permanentemente? (s/n): ");
                                        String confirmar = scanner.nextLine();

                                        if (confirmar.equalsIgnoreCase("s")) {
                                            JSONObject jsonExclusao = new JSONObject();
                                            jsonExclusao.put("operacao", "excluir_usuario");
                                            jsonExclusao.put("token", usuarioLogado.getToken());

                                            System.out.println("Enviado ao servidor: " + jsonExclusao.toString());
                                            output.println(jsonExclusao.toString());

                                            String respostaExclusao = input.readLine();
                                            if (respostaExclusao == null) {
                                                System.out.println("Conexão encerrada pelo servidor.");
                                                return;
                                            }

                                            JSONObject respostaJsonExcluir = new JSONObject(respostaExclusao);

                                            Operacao operacaoExcluir = new Operacao(
                                                    respostaJsonExcluir.optString("operacao", ""),
                                                    respostaJsonExcluir.optString("mensagem", ""),
                                                    respostaJsonExcluir.optString("status", "")
                                            );

                                            System.out.println("Resposta do servidor: " + respostaJsonExcluir.toString());

                                            if (operacaoExcluir.getStatus().equals("sucesso")) {
                                                System.out.println("Perfil excluído com sucesso!");
                                                logado = false;
                                                usuarioLogado = null;
                                            } else {
                                                System.out.println("Erro: " + operacaoExcluir.getMensagem());
                                            }
                                        }
                                        continue;
                                    }

                                    case "4" ->
                                        json = deslogar(usuarioLogado);

                                    default -> {
                                        System.out.println("Opção inválida.");
                                        continue;
                                    }
                                }

                                System.out.println("Enviado ao servidor: " + json.toString());
                                output.println(json.toString());

                                resposta = input.readLine();
                                if (resposta == null) {
                                    System.out.println("Conexão encerrada pelo servidor.");
                                    break;
                                }
                                System.out.println("Resposta do servidor: " + resposta);

                                if (escolha.equals("4")) {
                                    JSONObject respostaLogout = new JSONObject(resposta);
                                    Operacao operacaoLogout = new Operacao(
                                            respostaLogout.optString("operacao", ""),
                                            respostaLogout.optString("mensagem", ""),
                                            respostaLogout.optString("status", "")
                                    );

                                    System.out.println("Mensagem: " + operacaoLogout.getMensagem());

                                    if (operacaoLogout.getStatus().equals("sucesso")) {
                                        logado = false;
                                        usuarioLogado = null;
                                    }
                                } else if (escolha.equals("1")) {
                                    try {
                                        respostaJson = new JSONObject(resposta);
                                        if (respostaJson.getString("status").equals("sucesso")) {
                                            JSONObject dados = respostaJson.getJSONObject("dados");
                                            System.out.println("\n=== MEUS DADOS ===");
                                            System.out.println("nome: " + dados.getString("nome"));
                                            System.out.println("usuario: " + dados.getString("usuario"));
                                            System.out.println("senha: " + dados.getString("senha"));
                                            System.out.println("====================\n");
                                        } else {
                                            System.out.println("Erro: " + respostaJson.optString("mensagem", "Erro desconhecido"));
                                        }
                                    } catch (JSONException e) {
                                        System.out.println("Erro ao processar resposta: " + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException nfex) {
            System.out.println("Porta inválida. Por favor, digite um número inteiro.");
        }
    }
}
