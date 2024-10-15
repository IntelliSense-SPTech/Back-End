import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class OperacoesBucket {

    private final S3Client s3Client;

    public OperacoesBucket(S3Provider s3Provider) {
        this.s3Client = s3Provider.getS3Client();
    }

    // Criar um bucket
    public void criarBucket(String bucketName) {
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();
        s3Client.createBucket(createBucketRequest);
        System.out.println("Bucket criado: " + bucketName);
    }

    // Listar buckets
    public void listarBuckets() {
        ListBucketsResponse response = s3Client.listBuckets();
        List<Bucket> buckets = response.buckets();
        for (Bucket bucket : buckets) {
            System.out.println("Bucket: " + bucket.name());
        }
    }

    // Listar objetos em um bucket
    public void listarObjetos(String bucketName) {
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .build();

        ListObjectsResponse response = s3Client.listObjects(listObjectsRequest);
        List<S3Object> objects = response.contents();
        for (S3Object object : objects) {
            System.out.println("Objeto: " + object.key());
        }
    }

    // Enviar um objeto para um bucket
    public void enviarObjeto(String bucketName, String filePath) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(UUID.randomUUID().toString()) // Gera um ID único para o objeto
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(filePath)));
        System.out.println("Objeto enviado: " + filePath);
    }

    // Baixar um objeto específico de um bucket
    public InputStream baixarObjeto(String bucketName, String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        try {
            return s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream());
        } catch (Exception e) {
            System.err.println("Erro ao baixar o objeto " + objectKey + ": " + e.getMessage());
            return null;
        }
    }

    // Baixar todos os objetos de um bucket
    public void baixarObjetos(String bucketName) {
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .build();

        ListObjectsResponse response = s3Client.listObjects(listObjectsRequest);
        List<S3Object> objects = response.contents();
        for (S3Object object : objects) {
            try (InputStream objectContent = baixarObjeto(bucketName, object.key())) {
                if (objectContent != null) {
                    Files.copy(objectContent, new File(object.key()).toPath());
                    System.out.println("Objeto baixado: " + object.key());
                }
            } catch (Exception e) {
                System.err.println("Erro ao salvar o objeto " + object.key() + ": " + e.getMessage());
            }
        }
    }

    // Deletar um objeto de um bucket
    public void deletarObjeto(String bucketName, String objectKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        System.out.println("Objeto deletado: " + objectKey);
    }
}
