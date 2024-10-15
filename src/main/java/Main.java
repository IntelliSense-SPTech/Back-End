import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;
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

        String bucketName = System.getenv("S3_BUCKET_NAME");
        String arquivoKey = "OcorrenciaMensal(Criminal)-EstadoSP_20241007_134342.xlsx";

        try {
            jdbcTemplate = dbConnectionProvider.getConnection();
            Connection connection = null;

            try {
                connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Conexão com o banco de dados estabelecida com sucesso.");
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Iniciando o processo de leitura do arquivo do S3.");
                System.out.println("");

                boolean leituraBemSucedida = leitor.lerArquivo(bucketName, arquivoKey);

                if (leituraBemSucedida) {
                    System.out.println("");
                    System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Análise do arquivo '" + arquivoKey + "' concluída.");
                } else {
                    System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Não foi possível ler o arquivo '" + arquivoKey + "'.");
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
    }
}
