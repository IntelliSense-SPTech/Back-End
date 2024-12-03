package notificacao;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.List;
import java.util.Properties;

public class Email {

    // Configurações do servidor SMTP
    private static final String SERVIDOR_SMTP = "smtp.gmail.com";
    private static final String PORTA_SMTP = "587";
    private static final String EMAIL_REMETENTE = "intellisensenotify@gmail.com";
    private static final String SENHA_REMETENTE = "oapb tzif lisw qiyo"; // Substitua com a senha do seu e-mail


    public static void enviarNotificacaoParaUsuarios(List<String> emails) {
        for (String email : emails) {
            if (isEmailValido(email)) {
                try {
                    enviarEmail(
                            email,
                            "Notificação - IntelliSense",
                            "Aqui está o conteúdo da sua notificação.",
                            "Local Aumento",
                            "Crime Aumento",
                            "Aumento Percentual",
                            "Local Reducao",
                            "Crime Reducao",
                            "Reducao Percentual"
                    );
                    System.out.println("Notificação enviada para: " + email);
                } catch (MessagingException e) {
                    System.err.println("Erro ao enviar e-mail para " + email + ": " + e.getMessage());
                }
            } else {
                System.out.println("E-mail inválido: " + email);
            }
        }
    }

    public static void enviarEmail(String emailDestinatario, String assunto, String corpoMensagem, String localAumento, String crimeAumento, String aumentoPercentual, String localReducao, String crimeReducao, String reducaoPercentual) throws MessagingException {
        // Configurar as propriedades do servidor SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", SERVIDOR_SMTP);
        props.put("mail.smtp.port", PORTA_SMTP);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Criar a sessão de e-mail
        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, SENHA_REMETENTE);
            }
        });

        // Criar a mensagem de e-mail
        Message mensagem = new MimeMessage(session);
        mensagem.setFrom(new InternetAddress(EMAIL_REMETENTE));
        mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinatario));
        mensagem.setSubject(assunto);

        // Criar o corpo da mensagem de forma dinâmica
        String mensagemCorpo = criarCorpoMensagem(localAumento, crimeAumento, aumentoPercentual, localReducao, crimeReducao, reducaoPercentual);

        // Definir o conteúdo do e-mail com HTML
        mensagem.setContent(mensagemCorpo, "text/html");

        // Enviar o e-mail
        Transport.send(mensagem);
        System.out.println("E-mail enviado com sucesso para o destinatário: " + emailDestinatario);
    }

    private static String criarCorpoMensagem(String localAumento, String crimeAumento, String aumentoPercentual, String localReducao, String crimeReducao, String reducaoPercentual) {
        // Criar o texto da mensagem com formatação HTML
        return "<html><body>" +
                "<h3><strong>Notificação de Aumento de Crimes:</strong></h3>" +
                "<p>Notamos um aumento significativo nas ocorrências reportadas recentemente. Aqui estão os detalhes para sua análise:</p>" +
                "<ul>" +
                "<li><strong>Localidade:</strong> " + localAumento + "</li>" +
                "<li><strong>Crime:</strong> " + crimeAumento + "</li>" +
                "<li><strong>Aumento Percentual:</strong> " + aumentoPercentual + "</li>" +
                "</ul>" +
                "<h3><strong>Notificação de Redução de Crimes:</strong></h3>" +
                "<p>Notamos uma redução significativa nas ocorrências reportadas recentemente. Aqui estão os detalhes para sua análise:</p>" +
                "<ul>" +
                "<li><strong>Localidade:</strong> " + localReducao + "</li>" +
                "<li><strong>Crime:</strong> " + crimeReducao + "</li>" +
                "<li><strong>Redução Percentual:</strong> " + reducaoPercentual + "</li>" +
                "</ul>" +
                "</body></html>";
    }

    public static boolean isEmailValido(String email) {
        // Verifica se o e-mail possui formato válido com expressão regular mais robusta
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(regex);
    }
}
