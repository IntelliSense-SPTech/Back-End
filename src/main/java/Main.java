import software.amazon.awssdk.services.s3.S3Client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) throws Exception {


        LeituraArquivoExcel leituraArquivoExcel = new LeituraArquivoExcel();
        leituraArquivoExcel.lerArquivo();

        S3Config s3Config = new S3Config();
        S3Client s3Client = s3Config.getS3Client();

        OperacaoAmazon operacaoAmazon = new OperacaoAmazon(s3Client);
        operacaoAmazon.execute();
    }
}
