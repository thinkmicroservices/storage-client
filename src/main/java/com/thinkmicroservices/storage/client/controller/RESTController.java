package com.thinkmicroservices.storage.client.controller;

import com.thinkmicroservices.storage.client.service.StorageException;
import com.thinkmicroservices.storage.client.service.StorageService;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author cwoodward
 */
@RestController
@Slf4j
public class RESTController {

    @Autowired
    StorageService storageService;

    @GetMapping(path = "/buckets")
    /**
     *
     * @return @throws StorageException
     */
    
    public List<Bucket> listBuckets() { 
        return storageService.getAllBuckets();
    }

    @PostMapping(path = "/buckets/{bucket}")
    /**
     *
     * @param bucketName
     * @throws StorageException
     */
    public void createBucket(@PathVariable("bucket") String bucketName)  {
        storageService.createBucket(bucketName);
    }

    @DeleteMapping(path = "buckets/{bucket}")
    /**
     *
     * @param bucketName
     * @throws StorageException
     */
    public void deleteBucket(@PathVariable("bucket") String bucketName)   {
        storageService.removeBucket(bucketName);
    }

    @GetMapping(path = "/{bucket}")
    /**
     *
     * @param bucketName
     * @return
     * @throws StorageException
     */
    public List<Item> listBucketContents(@PathVariable("bucket") String bucketName)   {
        return storageService.listBucketContents(bucketName);
    }

    @PostMapping(path = "/{bucket}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    /**
     *
     * @param bucket
     * @param files
     * @return
     * @throws StorageException
     */
    public Map<String, String> uploadFile(@PathVariable("bucket") String bucket, @RequestParam(value = "filename") MultipartFile files)   {
        log.info("minioService " + storageService);
        log.info("files " + files);
        try {
            storageService.putObject(bucket, files.getOriginalFilename(), files.getBytes());
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
        Map<String, String> result = new HashMap<>();
        result.put("key", files.getOriginalFilename());
        return result;
    }

    @GetMapping(path = "/{bucket}/{object}")
    /**
     *
     * @param bucket
     * @param file
     * @return
     * @throws StorageException
     */
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable("bucket") String bucket, @PathVariable("object") String file) throws StorageException {
        byte[] data = storageService.getObject(bucket, file);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + file + "\"")
                .body(resource);

    }

    @DeleteMapping(path = "/{bucket}/{object}")
    /**
     *
     * @param bucketName
     * @param object
     * @throws StorageException
     */
    public void deleteFile(@PathVariable("bucket") String bucketName, @PathVariable("object") String object) throws StorageException {

        storageService.removeObject(bucketName, object);
    }
    
    @ExceptionHandler(value = StorageException.class)
    public ResponseEntity handleStorageException(StorageException storageException) {
        return new ResponseEntity(storageException.getMessage(), HttpStatus.CONFLICT);
    }

}
