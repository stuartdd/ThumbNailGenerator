/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author stuar
 */
public class ConfigData {

    private String thumbNailsRoot;
    private String thumbNailTimeStamp;
    private String thumbNailFileSuffix;
    private Resources resources;
    private boolean test = false;
    private String logPath;
    private List<String> imageExtensions = new ArrayList<>();

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        if (!System.getProperties().contains("logPath")) {
            System.getProperties().put("logPath", logPath);
        }
        this.logPath = logPath;
    }

    public String getThumbNailsRoot() {
        return thumbNailsRoot;
    }

    public void setThumbNailsRoot(String thumbNailsRoot) {
        this.thumbNailsRoot = thumbNailsRoot;
    }

    public String getThumbNailTimeStamp() {
        return thumbNailTimeStamp;
    }

    public void setThumbNailTimeStamp(String thumbNailTimeStamp) {
        this.thumbNailTimeStamp = thumbNailTimeStamp;
    }

    public String getThumbNailFileSuffix() {
        return thumbNailFileSuffix;
    }

    public void setThumbNailFileSuffix(String thumbNailFileSuffix) {
        this.thumbNailFileSuffix = thumbNailFileSuffix;
    }

    public List<String> getImageExtensions() {
        return imageExtensions;
    }

    public void setImageExtensions(List<String> imageExtensions) {
        this.imageExtensions = imageExtensions;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    @JsonIgnore
    public String formatThumbNailFileTimeStamp(Date dateTimeOriginal) {
        SimpleDateFormat sdf = new SimpleDateFormat(getThumbNailTimeStamp());
        return sdf.format(dateTimeOriginal);
    }

    public boolean isImageFile(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String name = file.getName().toLowerCase();
        for (String s : imageExtensions) {
            if (name.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

}
