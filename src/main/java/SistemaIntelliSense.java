import jakarta.mail.MessagingException;
import notificacao.Email;
import notificacao.Slack;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SistemaIntelliSense {
    public static void main(String[] args) {

        // Continuação do processo com S3 e banco de dados
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        JdbcTemplate jdbcTemplate = null;
        S3Provider s3Provider = new S3Provider();
        OperacoesBucket operacoesBucket = new OperacoesBucket(s3Provider);
        BancoDados bancoDados = new BancoDados();
        Leitor leitor = new Leitor(operacoesBucket, bancoDados);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Nome do bucket S3
        String bucketName = System.getenv("S3_BUCKET_NAME");

        bancoDados.truncateCrimes();

        // Verificação de conexão com S3
        if (!s3Provider.testConnection()) {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Erro ao conectar com o S3. A aplicação será encerrada.");
            System.exit(1);
        }

        try {
            jdbcTemplate = dbConnectionProvider.getConnection();
            Connection connection = null;

            try {
                connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Conexão com o banco de dados estabelecida com sucesso.");

                // Itera sobre cada arquivo na lista
                for (String arquivo : leitor.arquivoKeys) {
                    System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Iniciando o processo de leitura do arquivo do S3: " + arquivo);
                    System.out.println("");

                    // Tenta ler o arquivo e processá-lo
                    boolean leituraBemSucedida = leitor.lerArquivo(bucketName, arquivo);

                    // Verifica se a leitura foi bem-sucedida
                    if (leituraBemSucedida) {
                        System.out.println("");
                    } else {
                        System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Não foi possível ler o arquivo '" + arquivo + "'.");
                    }
                }

            } catch (DataAccessException e) {
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Falha ao estabelecer a conexão com o banco de dados: " + e.getMessage());
            } finally {
                if (connection != null) {
                    try {
                        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
                    } catch (Exception e) {
                        System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Erro ao fechar a conexão: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Erro: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Processo finalizado.");

        bancoDados.notificacaoReducaoDeCrimes();
        bancoDados.notificacaoAumentoDeCrimes();

        try {
            System.out.println("Preparando envio de mensagem para o Slack...");

            // Buscar a lista de usuários apenas uma vez
            List<JSONObject> usuarios = null;
            try {
                // Simula obtenção dos usuários do Slack
                usuarios = Slack.obterTodosUsuarios();
            } catch (Exception e) {
                System.err.println("Erro ao buscar usuários: " + e.getMessage());
                // Continue o fluxo sem interromper a execução
            }

            // Criar instância do Slack para acessar métodos não estáticos
            Slack slack = new Slack();

            // Preencher a lista de e-mails válidos
            slack.setEmailsValidos(Slack.obterEmailsUsuarios());

            // Após preencher a lista, envia e-mails um por um
            if (!slack.getEmailsValidos().isEmpty()) {
                for (String email : slack.getEmailsValidos()) {
                    try {
                        // Enviar e-mail para cada usuário na lista de e-mails válidos
                        String assunto = "Notificação importante";
                        String mensagemCorpo = "Olá, esta é uma notificação importante do canal Intellisense-SenseNotify.";
                        Email.enviarEmail(
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
                        System.out.println("E-mail enviado para: " + email);
                    } catch (MessagingException e) {
                        System.err.println("Erro ao enviar e-mail para: " + email + ". Erro: " + e.getMessage());
                        // Continue enviando e-mails para os outros usuários
                    }
                }
            } else {
                System.out.println("Nenhum e-mail válido encontrado.");
            }


            // Criação do JSON com as informações de mensagem
            JSONObject conteudoMensagem = new JSONObject();

            // Criar o texto da mensagem em formato adequado para o Slack
            String mensagem =  "*Notificação de Aumento de Crimes:*\n" +
                    "Notamos um aumento significativo nas ocorrências reportadas recentemente. Aqui estão os detalhes para sua análise:\n" +
                    "- Localidade: Grande São Paulo\n" +
                    "- Crime: LATROCÍNIO\n" +
                    "- Aumento Percentual: 500,00%\n\n\n";

            mensagem += "*Notificação de Redução de Crimes:*\n" +
                    "Notamos uma redução significativa nas ocorrências reportadas recentemente. Aqui estão os detalhes para sua análise:\n" +
                    "- Localidade: Litoral\n" +
                    "- Crime:** ROUBO DE CARGA\n" +
                    "- Redução Percentual: 40,00%\n\n";

            // A mensagem precisa ser definida diretamente no campo "text"
            conteudoMensagem.put("text", mensagem); // Definindo o campo "text" para o Slack

            // Exibindo o JSON gerado
            System.out.println("Conteúdo da mensagem: " + conteudoMensagem.toString());

            // Enviar mensagem para o Slack UMA ÚNICA VEZ
            try {
                String usuarioId = "usuário_id_aqui"; // Insira o ID do usuário correto
                Slack.enviarMensagem(conteudoMensagem);  // Certifique-se de que o método 'enviarMensagem' está implementado corretamente
                System.out.println("Mensagem enviada para o Slack com sucesso!");
            } catch (Exception e) {
                System.err.println("Erro ao enviar mensagem para o Slack: " + e.getMessage());
                // Continue o fluxo sem interromper a execução
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar as mensagens ou enviar e-mails: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
