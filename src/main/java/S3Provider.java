import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3Provider {

    private final AwsSessionCredentials credentials;
    private final S3Client s3Client;

    public S3Provider() {
        this.credentials = AwsSessionCredentials.create(
                System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY"),
                System.getenv("AWS_SESSION_TOKEN")
        );

        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(() -> credentials)
                .build();
    }

    public S3Client getS3Client() {
        return s3Client;
    }

    // Método para testar a conexão com o S3
    public boolean testConnection() {
        try {
            s3Client.listBuckets(ListBucketsRequest.builder().build());
            return true;  // Conexão bem-sucedida
        } catch (S3Exception e) {
            System.out.println("Erro ao conectar com o S3: " + e.getMessage());
            return false;  // Falha na conexão
        }
    }
}
