package com.thinkmicroservices.storage.client.service;

import com.thinkmicroservices.storage.client.service.StorageException;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import java.util.List;

/**
 *
 * @author cwoodward
 */
public interface StorageService {

    /**
     *
     * @param bucketName
     * @throws StorageException
     */
    void createBucket(String bucketName) throws StorageException;

    /**
     *
     * @return @throws StorageException
     */
    List<Bucket> getAllBuckets() throws StorageException;

    /**
     *
     * @param bucketName
     * @throws StorageException
     */
    void removeBucket(String bucketName) throws StorageException;

    /**
     *
     * @param bucketName
     * @return
     * @throws StorageException
     */
    List<Item> listBucketContents(String bucketName) throws StorageException;

    /**
     *
     * @param name
     * @param content
     */
    void putObject(String bucketName, String filename, byte[] content) throws StorageException;

    /**
     *
     * @param objectName
     * @return
     */
    byte[] getObject(String bucketName, String objectName) throws StorageException;
    
    /**
     * 
     * @param bucketName
     * @param objectName
     * @throws StorageException 
     */
    void removeObject(String bucketName, String objectName) throws StorageException;
}
