import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;  // Certifique-se de que você importou DataSource
import java.sql.Connection;  // Importar Connection
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        JdbcTemplate jdbcTemplate = null;
        S3Provider s3Provider = new S3Provider();
        OperacoesBucket operacoesBucket = new OperacoesBucket(s3Provider);
        Leitor leitor = new Leitor(operacoesBucket);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Acessando a variável de ambiente
        String bucketName = System.getenv("S3_BUCKET_NAME");
        String arquivoKey = "OcorrenciaMensal(Criminal)-EstadoSP_20241007_134342.xlsx";

        try {
            // Tenta obter a conexão
            jdbcTemplate = dbConnectionProvider.getConnection();
            Connection connection = null;

            try {
                // Se a conexão for bem-sucedida, loga a mensagem
                connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Conexão com o banco de dados estabelecida com sucesso.");

                // Log de quando o processo começa
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Iniciando o processo de leitura do arquivo do S3.");

                // Ler o arquivo do S3
                leitor.lerArquivo(bucketName, arquivoKey);

                // Log de análise do arquivo
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Análise do arquivo '" + arquivoKey + "' concluída.");

            } catch (DataAccessException e) {
                // Log de erro de conexão
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Falha ao estabelecer a conexão com o banco de dados: " + e.getMessage());
            } finally {
                // Fechar a conexão se ela foi estabelecida
                if (connection != null) {
                    try {
                        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
                    } catch (Exception e) {
                        System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Erro ao fechar a conexão: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            // Log de erro genérico
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Erro: " + e.getMessage());
            e.printStackTrace();
        }

        // Log de quando o processo termina
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Processo finalizado.");
    }
}
