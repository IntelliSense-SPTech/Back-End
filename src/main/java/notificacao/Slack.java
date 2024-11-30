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
    private static final String URL_MENSAGEM = "https://hooks.slack.com/services/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";
    private static final String URL_MEMBROS = "https://slack.com/api/conversations.members";
    private static final String URL_USUARIO = "https://slack.com/api/users.info";
    private static final String TOKEN_SLACK = System.getenv("TOKEN_SLACK");


    private List<String> emailsValidos = new ArrayList<>();

    // Exceção personalizada
    public static class ExcecaoSlack extends Exception {
        public ExcecaoSlack(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }
    }

    // Método para enviar mensagem para o Slack
    public static void enviarMensagem(JSONObject conteudo, String usuarioId) throws ExcecaoSlack {
        try {
            System.out.println("Preparando envio de mensagem para o Slack...");
            System.out.println("Conteúdo da mensagem: " + conteudo.toString());

            HttpRequest requisicao = HttpRequest.newBuilder(
                            URI.create(URL_MENSAGEM))
                    .header("accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(conteudo.toString()))
                    .build();

            HttpResponse<String> resposta = clienteHttp.send(requisicao, HttpResponse.BodyHandlers.ofString());
            System.out.println("Resposta do Slack ao envio da mensagem:");
            System.out.println("Status: " + resposta.statusCode());
            System.out.println("Corpo: " + resposta.body());

            if (resposta.statusCode() != 200) {
                throw new ExcecaoSlack("Erro ao enviar mensagem para o Slack. Status: " + resposta.statusCode(), null);
            }

            // Obter e-mail dos usuários e enviar o e-mail apenas para os e-mails específicos
            List<String> emails = obterEmailsUsuarios();
            for (String email : emails) {
                if (!email.isEmpty()) {
                    // Enviar e-mail para o usuário
                    Email.enviarEmail(
                            email,
                            "Notificação Slack",
                            "Mensagem enviada ao Slack com sucesso: " + conteudo.toString()
                    );
                }
            }

        } catch (IOException | InterruptedException e) {
            throw new ExcecaoSlack("Erro ao enviar mensagem para o Slack", e);
        } catch (MessagingException e) {
            System.err.println("Erro ao enviar o e-mail: " + e.getMessage());
        } finally {
            System.out.println("Envio de mensagem para o Slack finalizado.");
        }
    }

    // Método para obter todos os usuários
    public static List<JSONObject> obterTodosUsuarios() throws ExcecaoSlack {
        try {
            System.out.println("Obtendo todos os usuários da organização...");

            String url = "https://slack.com/api/users.list";
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + TOKEN_SLACK)
                    .GET()
                    .build();

            HttpResponse<String> resposta = clienteHttp.send(requisicao, HttpResponse.BodyHandlers.ofString());
            System.out.println("Resposta da API users.list:");
            System.out.println(resposta.body());

            JSONObject respostaJson = new JSONObject(resposta.body());

            if (!respostaJson.getBoolean("ok")) {
                throw new ExcecaoSlack("Erro ao obter lista de usuários: " + respostaJson.getString("error"), null);
            }

            JSONArray usuarios = respostaJson.getJSONArray("members");
            List<JSONObject> informacoesUsuarios = new ArrayList<>();

            for (int i = 0; i < usuarios.length(); i++) {
                informacoesUsuarios.add(usuarios.getJSONObject(i));  // Armazenar o JSONObject no ArrayList
            }

            System.out.println("Usuários obtidos com sucesso.");
            return informacoesUsuarios;

        } catch (IOException | InterruptedException e) {
            throw new ExcecaoSlack("Erro ao obter lista de usuários", e);
        }
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

            // Enviar a requisição HTTP para a API do Slack
            HttpResponse<String> resposta = clienteHttp.send(requisicao, HttpResponse.BodyHandlers.ofString());
            JSONObject respostaJson = new JSONObject(resposta.body());

            // Log da resposta JSON para diagnóstico
            System.out.println("Resposta da API users.info:");
            System.out.println(respostaJson.toString(4));

            // Verificar se a resposta foi bem-sucedida
            if (!respostaJson.getBoolean("ok")) {
                throw new ExcecaoSlack("Erro ao obter informações do usuário: " + respostaJson.getString("error"), null);
            }

            // Verificar se o campo de e-mail está presente no perfil do usuário
            JSONObject usuario = respostaJson.getJSONObject("user");
            JSONObject perfil = usuario.getJSONObject("profile");

            // Verificar se o email está disponível no perfil
            if (perfil.has("email") && !perfil.isNull("email")) {
                String email = perfil.getString("email");
                System.out.println("E-mail do usuário validado: " + email);
                return email;
            } else {
                // Caso não tenha e-mail, retornar uma mensagem padrão ou uma string vazia
                System.out.println("E-mail não encontrado para o usuário com ID: " + usuarioId);
                return "";
            }

        } catch (IOException | InterruptedException e) {
            throw new ExcecaoSlack("Erro ao obter e-mail do usuário", e);
        }
    }

    public List<String> getEmailsValidos() {
        return emailsValidos;
    }

    public void setEmailsValidos(List<String> emailsValidos) {
        this.emailsValidos = emailsValidos;
    }
}
