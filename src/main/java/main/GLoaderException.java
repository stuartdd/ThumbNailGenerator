/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author stuart
 */
public class GLoaderException extends RuntimeException {

    public GLoaderException(String message) {
        super(message);
    }

    public GLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
