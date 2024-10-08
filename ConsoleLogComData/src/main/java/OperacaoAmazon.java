public class OperacaoAmazon {

    CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
            .bucket("nome-do-bucket")
            .build();

    s3Client.createBucket(createBucketRequest);

    List<Bucket> buckets = s3Client.listBuckets().buckets();
for (Bucket bucket : buckets) {
        System.out.println("Bucket: " + bucket.name());
    }
    ListObjectsRequest listObjects = ListObjectsRequest.builder()
            .bucket("nome-do-bucket")
            .build();

    List<S3Object> objects = s3Client.listObjects(listObjects).contents();
for (S3Object object : objects) {
        System.out.println("Objeto: " + object.key());
    }
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket("nome-do-bucket")
            .key(UUID.randomUUID().toString())
            .build();

s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File("file.txt")));

    List<S3Object> objects = s3Client.listObjects(listObjects).contents();
for (S3Object object : objects) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("nome-do-bucket")
                .key(object.key())
                .build();

        InputStream objectContent = s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream());
        Files.copy(objectContent, new File(object.key()).toPath());
    }
    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket("nome-do-bucket")
            .key("identificador-do-objeto")
            .build();

s3Client.deleteObject(deleteObjectRequest);
System.out.println("Objeto deletado: " + "identificador-do-objeto");
}
