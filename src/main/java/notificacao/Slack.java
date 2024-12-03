package notificacao;

import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Slack {

    private static HttpClient clienteHttp = HttpClient.newHttpClient();
    private static final String URL_MENSAGEM = System.getenv("URL_SLACK");
    private static final String URL_USUARIO = "https://slack.com/api/users.info";
    private static final String URL_USUARIOS = "https://slack.com/api/users.list";
    private static final String TOKEN_SLACK = System.getenv("TOKEN_SLACK");

    private List<String> emailsValidos = new ArrayList<>();

    // Exceção personalizada para erros relacionados ao Slack
    public static class ExcecaoSlack extends Exception {
        public ExcecaoSlack(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }
    }

    // Método para obter todos os usuários da organização Slack
    public static List<JSONObject> obterTodosUsuarios() throws ExcecaoSlack {
        int maxTentativas = 5;  // Número máximo de tentativas
        int tentativaAtual = 0;

        while (tentativaAtual < maxTentativas) {
            try {
                System.out.println("Obtendo todos os usuários da organização...");

                String url = URL_USUARIOS;
                HttpRequest requisicao = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + TOKEN_SLACK)
                        .GET()
                        .build();

                HttpResponse<String> resposta = clienteHttp.send(requisicao, HttpResponse.BodyHandlers.ofString());
                JSONObject respostaJson = new JSONObject(resposta.body());

                if (!respostaJson.getBoolean("ok")) {
                    throw new ExcecaoSlack("Erro ao obter lista de usuários: " + respostaJson.getString("error"), null);
                }

                JSONArray usuarios = respostaJson.getJSONArray("members");
                List<JSONObject> informacoesUsuarios = new ArrayList<>();

                for (int i = 0; i < usuarios.length(); i++) {
                    informacoesUsuarios.add(usuarios.getJSONObject(i));
                }

                System.out.println("Usuários obtidos com sucesso.");
                return informacoesUsuarios;

            } catch (IOException | InterruptedException e) {
                if (e.getMessage().contains("ratelimited")) {
                    tentativaAtual++;
                    System.out.println("Limitação de taxa alcançada, aguardando antes de tentar novamente...");
                    try {
                        // Espera 30 segundos antes de tentar novamente
                        Thread.sleep(30000);  // 30 segundos
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();  // Restaurar o estado de interrupção
                    }
                } else {
                    throw new ExcecaoSlack("Erro ao obter lista de usuários", e);
                }
            }
        }

        throw new ExcecaoSlack("Limite de tentativas atingido", null);
    }

    // Método para obter os e-mails de todos os usuários
    public static List<String> obterEmailsUsuarios() throws ExcecaoSlack {
        List<String> emails = new ArrayList<>();
        try {
            List<JSONObject> usuarios = obterTodosUsuarios();
            for (JSONObject usuario : usuarios) {
                String usuarioId = usuario.getString("id");
                String email = obterEmailUsuario(usuarioId);
                if (!email.isEmpty()) {
                    emails.add(email);
                }
            }
            return emails;
        } catch (ExcecaoSlack e) {
            throw new ExcecaoSlack("Erro ao obter e-mails dos usuários", e);
        }
    }

    // Método para obter o e-mail de um usuário específico
    public static String obterEmailUsuario(String usuarioId) throws ExcecaoSlack {
        try {
            System.out.println("Obtendo e-mail do usuário com ID: " + usuarioId);

            // Montar a URL da API para obter informações do usuário
            String url = URL_USUARIO + "?user=" + usuarioId;
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + TOKEN_SLACK)
                    .GET()
                    .build();

            HttpResponse<String> resposta = clienteHttp.send(requisicao, HttpResponse.BodyHandlers.ofString());
            JSONObject respostaJson = new JSONObject(resposta.body());

            if (!respostaJson.getBoolean("ok")) {
                throw new ExcecaoSlack("Erro ao obter informações do usuário: " + respostaJson.getString("error"), null);
            }

            JSONObject usuario = respostaJson.getJSONObject("user");
            JSONObject perfil = usuario.getJSONObject("profile");

            if (perfil.has("email") && !perfil.isNull("email")) {
                return perfil.getString("email");
            } else {
                return "";
            }

        } catch (IOException | InterruptedException e) {
            throw new ExcecaoSlack("Erro ao obter e-mail do usuário", e);
        }
    }

    // Método para enviar mensagem para o Slack e também enviar e-mail para os usuários
    public static void enviarMensagem(JSONObject conteudo) throws ExcecaoSlack {
        try {
            // Enviando a requisição HTTP para a API do Slack
            HttpRequest requisicao = HttpRequest.newBuilder(URI.create(URL_MENSAGEM))
                    .header("accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(conteudo.toString()))
                    .build();

            HttpResponse<String> resposta = clienteHttp.send(requisicao, HttpResponse.BodyHandlers.ofString());

            // Verificando resposta da API do Slack
            if (resposta.statusCode() != 200) {
                throw new ExcecaoSlack("Erro ao enviar mensagem para o Slack. Status: " + resposta.statusCode(), null);
            }

            // Obter e-mails dos usuários e enviar notificações por e-mail
            List<String> emails = obterEmailsUsuarios();

            // Enviar e-mails para todos os usuários
            Email.enviarNotificacaoParaUsuarios(emails);

        } catch (IOException | InterruptedException e) {
            throw new ExcecaoSlack("Erro ao enviar mensagem para o Slack", e);
        } finally {
            System.out.println("Envio de mensagem para o Slack finalizado.");
        }
    }

    public List<String> getEmailsValidos() {
        return emailsValidos;
    }

    public void setEmailsValidos(List<String> emailsValidos) {
        this.emailsValidos = emailsValidos;
    }
}
