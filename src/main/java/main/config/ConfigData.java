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
import main.GLoaderException;

/**
 *
 * @author stuar
 */
public class ConfigData {

    private String thumbNailsRoot;
    private String fileTimeStamp;
    private Resources resources;
    private boolean verbose = false;
    private boolean test = false;
    private String logFilePath;
    private String logErrorFileName;
    private String logFileName;
    private String logLineSeparator = "\n";
    private String logFileTimeStamp;
    private String logLineTimeStamp;
    private List<String> imageExtensions = new ArrayList<>();


    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public String getThumbNailsRoot() {
        return thumbNailsRoot;
    }

    public void setThumbNailsRoot(String thumbNailsRoot) {
        this.thumbNailsRoot = thumbNailsRoot;
    }

    public String getFileTimeStamp() {
        return fileTimeStamp;
    }

    public void setFileTimeStamp(String fileTimeStamp) {
        this.fileTimeStamp = fileTimeStamp;
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

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getLogLineSeparator() {
        return logLineSeparator;
    }

    public void setLogLineSeparator(String logLineSeparator) {
        this.logLineSeparator = logLineSeparator;
    }

    public String getLogFileTimeStamp() {
        return logFileTimeStamp;
    }

    public void setLogFileTimeStamp(String logFileTimeStamp) {
        this.logFileTimeStamp = logFileTimeStamp;
    }

    public String getLogLineTimeStamp() {
        return logLineTimeStamp;
    }

    public void setLogLineTimeStamp(String logLineTimeStamp) {
        this.logLineTimeStamp = logLineTimeStamp;
    }

    public String getLogErrorFileName() {
        return logErrorFileName;
    }

    public void setLogErrorFileName(String logErrorFileName) {
        this.logErrorFileName = logErrorFileName;
    }

    @JsonIgnore
    public String formatFileTimeStamp(Date dateTimeOriginal) {
        SimpleDateFormat sdf = new SimpleDateFormat(getFileTimeStamp());
        return sdf.format(dateTimeOriginal);
    }

    @JsonIgnore
    public String formatLogFileTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(getLogFileTimeStamp());
        return sdf.format(new Date());
    }

    @JsonIgnore
    public String formatLogLineTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(getLogLineTimeStamp());
        return sdf.format(new Date());
    }

    public void loaded(Object cdb) {
        testPath(thumbNailsRoot);
        List<String> temp = new ArrayList<>();
        for (String s : imageExtensions) {
            temp.add(s.toLowerCase());
        }
        imageExtensions = temp;
        resources.check(this);
        if (logFilePath != null) {
            testPath(logFilePath);
        }
    }

    public static File testPath(final String path) {
        File p = new File(path);
        p = new File(p.getAbsolutePath());
        if (p.exists()) {
            if (p.isDirectory()) {
                return p;
            }
            throw new GLoaderException("The path [" + p.getAbsolutePath() + "] must be a directory.");
        }
        throw new GLoaderException("The path [" + p.getAbsolutePath() + "] does not exist.");
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
