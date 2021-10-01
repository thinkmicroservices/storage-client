package com.thinkmicroservices.storage.client.service.minio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkmicroservices.storage.client.service.StorageService;
import com.thinkmicroservices.storage.client.service.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListBucketsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author cwoodward
 */
@Service
@Qualifier("MinioStorageServiceImpl")
@Slf4j
public class MinioStorageServiceImpl implements StorageService {

    @Value("${minio.access.name}")
    String accessKey;

    @Value("${minio.access.secret}")
    String accessSecret;

    @Value("${minio.host.url}")
    String minioUrl;

    @Value("${minio.bucket.name}")
    String defaultBucketName;

    private String tempDirectory;

    MinioClient minioClient;

    private static final int PUT_OBJECT_PART_SIZE = 10485760;
    /**
     *
     * @param name
     * @param content
     */
    @Override
    public void putObject(String bucketName, String objectName, byte[] content) throws StorageException {
        FileOutputStream fos = null;
        log.info(String.format("upload object: %s to bucket %s", objectName, bucketName));

        boolean bucketExists = false;

        try {
            BucketExistsArgs args = BucketExistsArgs.builder().bucket(bucketName).build();
            bucketExists = getMinioClient().bucketExists(args);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!bucketExists) {

            throw new StorageException(String.format("Bucket [%s] does not exist!", bucketName));
        }

        try {

            File file = new File(tempDirectory, objectName);
            file.canWrite();
            file.canRead();

            fos = new FileOutputStream(file);
            fos.write(content);
            //getMinioClient().putObject(bucketName, filename, file.getAbsolutePath());
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(bais, content.length, PUT_OBJECT_PART_SIZE).build();

            getMinioClient().putObject(args);

        } catch (Exception ex) {

            log.error(ex.getMessage(), ex);
            throw new StorageException(ex);

        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }

    }
    

    @Override
    /**
     *
     * @param bucketName
     * @param objectName
     * @return
     */
    public byte[] getObject(String bucketName, String objectName) {
        log.info(String.format("get object: %s from bucket %s", objectName, bucketName));
        try {
            GetObjectArgs args = GetObjectArgs.builder().bucket(bucketName).object(objectName).build();
            InputStream obj = getMinioClient().getObject(args);
            byte[] content = IOUtils.toByteArray(obj);
            obj.close();
            return content;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);

        }
        return null;
    }

    /**
     *
     * @param bucketName
     * @param objectName
     * @throws StorageException
     */
    public void removeObject(String bucketName, String objectName) throws StorageException {

        log.info(String.format("remove object: %s from bucket %s", objectName, bucketName));
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build();
            getMinioClient().removeObject(args);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new StorageException(ex);

        }
    }

    @Override
    /**
     *
     * @return @throws StorageException
     */
    public List<Bucket> getAllBuckets() throws StorageException {
        log.info("get all buckets");
        try {
             
            List<Bucket> buckets= getMinioClient().listBuckets();//ListBucketsArgs.builder()..build());
            for(Bucket bucket: buckets){
                log.info(bucket.name()+" "+bucket.creationDate());
            }
            return buckets;
        } catch (Exception ex) {
            throw new StorageException(ex);
        }

    }

    @Override
    /**
     *
     * @param bucketName
     * @throws StorageException
     */
    public void createBucket(String bucketName) throws StorageException {
        log.info("create bucket: " + bucketName);
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();

            if (!getMinioClient().bucketExists(bucketExistsArgs)) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
                getMinioClient().makeBucket(makeBucketArgs);
            }
        } catch (Exception ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    /**
     *
     * @param bucketName
     * @return
     * @throws StorageException
     */
    public List<Item> listBucketContents(String bucketName) throws StorageException {
        log.info("list bucket contents: " + bucketName);
        try {
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder().bucket(bucketName).build();
            Iterable<Result<Item>> sourceList = getMinioClient().listObjects(listObjectsArgs);
            /* extract the item from each Result<Iterable> and store it 
             in a separate list 
             */
            List<Item> targetList = new ArrayList<>();

            sourceList.forEach(result -> {
                try {
                    Item item = result.get();
                    log.info(new ObjectMapper().writeValueAsString(item));
                    targetList.add(item);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            });
            return targetList;
        } catch (Exception ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    /**
     *
     * @param bucketName
     * @throws StorageException
     */
    public void removeBucket(String bucketName) throws StorageException {
        log.info("delete bucket: " + bucketName);
        try {
            if(!listBucketContents(bucketName).isEmpty()){
                throw new StorageException(String.format("Can't remove [%s], bucket is not empty",bucketName));
            }
            
            RemoveBucketArgs removeBucketArgs = RemoveBucketArgs.builder().bucket(bucketName).build();
            getMinioClient().removeBucket(removeBucketArgs);
        } catch (Exception ex) {
            throw new StorageException(ex);
        }
    }

    /**
     *
     * @return
     */
    private MinioClient getMinioClient() {

        if (minioClient == null) {
            log.info("Creating Minio client...");
            log.info(minioUrl + " " + accessKey + " " + accessSecret);
            try {

                minioClient = MinioClient.builder().endpoint(minioUrl).credentials(accessKey, accessSecret).build();
                log.info("Created Minio client");

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.debug("reusing Minio client.");
        }

        return minioClient;
    }

    @PostConstruct
    public void postConstruct() {

        log.info("MinioStorageServiceImpl postConstruct");
        tempDirectory = System.getProperty("java.io.tmpdir");
        log.info("Uploaded files are temporarily stored in:" + tempDirectory);

    }
}
