package notificacao;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class Email {

    // Configurações do servidor SMTP
    private static final String SERVIDOR_SMTP = "smtp.gmail.com";
    private static final String PORTA_SMTP = "587";
    private static final String EMAIL_REMETENTE = "intellisensenotify@gmail.com";
    private static final String SENHA_REMETENTE = "oapb tzif lisw qiyo"; // Substitua com a senha do seu e-mail

    public static void enviarEmail(String emailDestinatario, String assunto, String mensagemCorpo) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", SERVIDOR_SMTP);
        props.put("mail.smtp.port", PORTA_SMTP);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, SENHA_REMETENTE);
            }
        });

        // Criar a mensagem
        Message mensagem = new MimeMessage(session);
        mensagem.setFrom(new InternetAddress(EMAIL_REMETENTE));
        mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinatario));
        mensagem.setSubject(assunto);
        mensagem.setText(mensagemCorpo);

        // Enviar o e-mail
        Transport.send(mensagem);
        System.out.println("E-mail enviado com sucesso para o destinatario " + emailDestinatario);
    }

    public static boolean isValidEmail(String email) {
        // Verifica se o e-mail possui formato válido
        return email != null && email.contains("@") && email.contains(".");
    }
}
