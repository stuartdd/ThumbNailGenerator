/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.config;

/**
 *
 * @author stuart
 */
public class ConfigDataException extends RuntimeException {

    public ConfigDataException(String message) {
        super(message);
    }

    public ConfigDataException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
