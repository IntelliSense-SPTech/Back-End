import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseTransformer;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class OperacaoAmazon {
    private final S3Client s3Client;
    LocalDateTime agora = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public OperacaoAmazon(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void execute() throws Exception {
        // Criar um novo bucket
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket("nome-do-bucket")
                .build();
        s3Client.createBucket(createBucketRequest);
        System.out.println("Bucket criado: nome-do-bucket");

        // Listar todos os buckets
        List<Bucket> buckets = s3Client.listBuckets().buckets();
        for (Bucket bucket : buckets) {
            System.out.println("Bucket: " + bucket.name());
        }

        // Listar objetos no bucket
        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket("nome-do-bucket")
                .build();
        List<S3Object> objects = s3Client.listObjects(listObjects).contents();
        for (S3Object object : objects) {
            System.out.println("Objeto: " + object.key());
        }

        // Adicionar um novo objeto ao bucket
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("nome-do-bucket")
                .key(UUID.randomUUID().toString())
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File("file.txt")));
        System.out.println("Novo objeto adicionado ao bucket");

        // Baixar os objetos do bucket
        for (S3Object object : objects) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket("nome-do-bucket")
                    .key(object.key())
                    .build();

            InputStream objectContent = s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream());
            Files.copy(objectContent, new File(object.key()).toPath());
            objectContent.close();
        }

        // Deletar um objeto do bucket
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket("nome-do-bucket")
                .key("identificador-do-objeto")
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        System.out.println("Objeto deletado: " + "identificador-do-objeto");
    }
}
