/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author stuar
 */
public class UserData {

    private String imageRoot;
    private List<String> imagePaths = new ArrayList<>();

    public String getImageRoot() {
        return imageRoot;
    }

    public void setImageRoot(String imageRoot) {
        this.imageRoot = imageRoot;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    void check() {
        ConfigData.testPath(imageRoot);
        for (String path : imagePaths) {
            ConfigData.testPath(imageRoot + File.separator + path);
        }
        if (imagePaths.isEmpty()) {
            imagePaths.add("");
        }
    }

}
