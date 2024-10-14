import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import software.amazon.awssdk.services.s3.S3Client;

public class Main {
    public static void main(String[] args) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        S3Config s3Config = new S3Config();
        S3Client s3Client = s3Config.getS3Client();
        LeituraArquivo leitura = new LeituraArquivo(s3Client);
        ConectarComBD bd = new ConectarComBD();
        OperacaoAmazon operacao = new OperacaoAmazon(s3Client);

        String bucketName = "nome-do-seu-bucket";
        String arquivoKey = "arquivo.xlsx";

        try {
            // Log de quando o processo começa
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Iniciando o processo de leitura do arquivo do S3.");

            // Ler e extrair dados do S3
            leitura.lerArquivoExcel(bucketName, arquivoKey);
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Arquivo '" + arquivoKey + "' obtido do bucket '" + bucketName + "'.");

            // Log de quando o processo de análise começa
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Iniciando análise do arquivo '" + arquivoKey + "'.");

            // Simulação de análise do arquivo (pode ser leitura, processamento, etc.)
            // Insere dados no banco de dados
            bd.inserirDados("Dado a ser inserido");

            // Log de quando os dados são enviados para o BD
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Dados enviados para o banco de dados com sucesso.");

        } catch (IOException e) {
            // Log de erro ao tentar obter ou analisar o arquivo
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Erro ao obter ou analisar o arquivo: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Log de erro ao tentar inserir dados no banco de dados
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Erro ao inserir dados no banco de dados: " + e.getMessage());
            e.printStackTrace();
        }

        // Log de quando o processo termina
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Processo finalizado.");
    }
}
