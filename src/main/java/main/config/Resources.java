/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author stuar
 */
public class Resources {

    private Map<String, UserData> users = new HashMap<>();

    public Map<String, UserData> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    public void setUsers(Map<String, UserData> users) {
        this.users = users;
    }
}
