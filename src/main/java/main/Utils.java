/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import main.config.ConfigData;
import main.config.ConfigDataException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author stuart
 */
public class Utils {

    private final SimpleDateFormat thumbNailTimeStamp;
    private final int thumbNailtimeStampLength;
    private final String thumbNailFileSuffix;
    private final FileFilter thumbNailFileFilter;
    private static final ObjectMapper jsonMapper;
    private static Utils instance;
    private final Logger logger;
    private final Logger logErr;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static Utils instance(ConfigData configData) {
        if (instance == null) {
            instance = new Utils(configData);
        }
        return instance;
    }

    public static Utils instance() {
        return instance;
    }

    private Utils(ConfigData configData) {
        String fmt = configData.getThumbNailTimeStamp();
        if ((fmt == null) || (fmt.isEmpty())) {
            throw new ConfigDataException("Config data 'thumbNailTimeStamp' is empty");
        }
        thumbNailTimeStamp = new SimpleDateFormat(fmt);
        thumbNailtimeStampLength = thumbNailTimeStamp.format(new Date()).length();

        thumbNailFileSuffix = configData.getThumbNailFileSuffix();
        if ((thumbNailFileSuffix == null) || (thumbNailFileSuffix.isEmpty())) {
            throw new ConfigDataException("Config data 'thumbNailFileSuffix' is empty");
        }
        thumbNailFileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.isFile() && pathname.getName().endsWith(thumbNailFileSuffix));
            }
        };
        logger = LogManager.getLogger("Main :");
        logErr = LogManager.getLogger("Error:");
    }

    public Logger getLogger() {
        return logger;
    }

    public Logger getLogErr() {
        return logErr;
    }

    public Logger getLogger(String id) {
        return LogManager.getLogger("Main :");
    }

    public FileFilter getThumbNailFileFilter() {
        return thumbNailFileFilter;
    }

    public String fileNameFromThumbNailName(String thumbNailFileName) {
        int pos = thumbNailFileName.lastIndexOf('/');
        if (pos < 0) {
            return thumbNailFileName.substring(thumbNailtimeStampLength + 1, thumbNailFileName.length() - thumbNailFileSuffix.length());
        }
        return thumbNailFileName.substring(0, pos + 1) + thumbNailFileName.substring((pos + 1) + thumbNailtimeStampLength + 1, thumbNailFileName.length() - thumbNailFileSuffix.length());
    }

    public String thumbNailFileNameFromFileName(String fileName, Date date) {
        int pos = fileName.lastIndexOf('/');
        if (pos < 0) {
            return thumbNailTimeStamp.format(date) + '_' + fileName + thumbNailFileSuffix;
        }
        String fn = fileName.substring(pos + 1);
        return fileName.substring(0, pos + 1) + thumbNailTimeStamp.format(date) + '_' + fn + thumbNailFileSuffix;
    }

    /**
     * Create an object from some JSON
     *
     * MyBean myBean = (MyBean) JsonUtils.beanFromJson(MyBean.class, TEST_JSON);
     *
     * @param beanType The class of the object
     * @param jsonFile The string containing the JSON
     * @return The object (will need casting)
     */
    public static ConfigData createConfigFromJsonFile(String jsonFile) {
        File f = new File((new File(jsonFile)).getAbsolutePath());
        if (!f.exists()) {
            throw new ConfigDataException("JSONfile [" + f.getAbsolutePath() + "] not found");
        }
        try {
            String json = new String(Files.readAllBytes(f.toPath()));
            return jsonMapper.readValue(json, ConfigData.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new ConfigDataException("Failed to parse JSON to a Bean Object", ex);
        }
    }

    public enum OS {
        WIN, LINUX, MAC, UNKNOWN
    }

    public static OS resolveOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if ((osName.contains("win"))) {
            return OS.WIN;
        } else if (osName.contains("mac")) {
            return OS.MAC;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OS.LINUX;
        } else {
            return OS.UNKNOWN;
        }
    }

}
