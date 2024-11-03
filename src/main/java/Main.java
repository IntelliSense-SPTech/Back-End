import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        JdbcTemplate jdbcTemplate = null;
        S3Provider s3Provider = new S3Provider();
        OperacoesBucket operacoesBucket = new OperacoesBucket(s3Provider);
        Leitor leitor = new Leitor(operacoesBucket);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Nome do bucket S3
        String bucketName = System.getenv("S3_BUCKET_NAME");

        // Lista de chaves dos arquivos a serem processados
        List<String> arquivoKeys = Arrays.asList(
                "OcorrenciaMensal(Criminal)-Grande São Paulo (exclui a Capital)_20240917_174302.xlsx",
                "OcorrenciaMensal(Criminal)-Capital_20240917_174245.xlsx",
                "OcorrenciaMensal(Criminal)-Santos_20240917_174314.xlsx"
        );

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
                for (String arquivoKey : arquivoKeys) {
                    System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Iniciando o processo de leitura do arquivo do S3: " + arquivoKey);
                    System.out.println("");

                    // Tenta ler o arquivo e processá-lo
                    boolean leituraBemSucedida = leitor.lerArquivo(bucketName, arquivoKey);

                    // Verifica se a leitura foi bem-sucedida
                    if (leituraBemSucedida) {
                        System.out.println("");
                    } else {
                        System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Não foi possível ler o arquivo '" + arquivoKey + "'.");
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
    }
}
