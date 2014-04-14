package com.cwport.sentencer.data;

/**
 * Created by isayev on 02.02.14.
 */
public class DataException extends Exception {
    public DataException(String msg) {
        super(msg);
    }
    public DataException(String msg, Throwable ex) {
        super(msg, ex);
    }

}
