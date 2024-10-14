import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import java.io.File;
import java.util.UUID;

public class OperacaoAmazon {
    private final S3Client s3Client;

    public OperacaoAmazon(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void criarBucket(String bucketName) {
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();
        s3Client.createBucket(createBucketRequest);
        System.out.println("Bucket criado: " + bucketName);
    }

    public void uploadArquivo(String bucketName, File file) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(UUID.randomUUID().toString())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        System.out.println("Arquivo enviado para o bucket: " + bucketName);
    }

    public void listarObjetos(String bucketName) {
        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket(bucketName)
                .build();
        s3Client.listObjects(listObjects).contents().forEach(object ->
                System.out.println("Objeto encontrado: " + object.key()));
    }
}
