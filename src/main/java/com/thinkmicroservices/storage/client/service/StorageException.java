package com.thinkmicroservices.storage.client.service;

/**
 *
 * @author cwoodward
 */
public class StorageException extends RuntimeException {

    /**
     *
     * @param msg
     */
    public StorageException(String msg) {
        super(msg);
    }

    /**
     *
     * @param ex
     */
    public StorageException(Exception ex) {
        super(ex);
    }
}
