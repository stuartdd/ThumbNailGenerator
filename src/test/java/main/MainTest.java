/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import main.config.ConfigData;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author 802996013
 */
public class MainTest {

    ConfigData configData;
    Utils utils;
    String configFileName;
    String logContent;
    File tempFile;

    @Test
    public void testMain() {
        configFileName = "configThumbNailGenTest" + Utils.resolveOS().name().toUpperCase() + ".json";
        configData = (ConfigData) utils.createConfigFromJsonFile(configFileName);
        utils = Utils.instance(configData);
        logContent = readNewLogCotent("");

        File f = new File(configData.getThumbNailsRoot());
        if (f.exists()) {
            File thf = new File(f.getAbsolutePath() + File.separator + "shared/lg 002/2006_08_22_16_19_33_22082006016.jpg.jpg");
            if (thf.exists()) {
                if (!thf.delete()) {
                    fail("Could not delete file [" + thf.getAbsolutePath() + "]");
                }
            }
        }
        tempFile = new File(f.getAbsolutePath() + File.separator + "shared/lg 002/9999_08_22_16_19_33_22082009999.jpg.jpg");
        if (tempFile.exists()) {
            tempFile.delete();
        }
        try {
            Files.write(tempFile.toPath(), "HelloWorld".getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            fail("Could not create file [" + tempFile.getAbsolutePath() + "]");
        }

        Main.main(new String[]{configFileName});

        if (tempFile.exists()) {
            tempFile.delete();
        }

        logContent = readNewLogCotent(logContent);
        assertTrue(logContent.contains("TOTAL for user [shared] HIT(3) MISS(1) DEL(1)"));
        assertTrue(logContent.contains("USER[shared] IMAGE NEW [0] : lg 002\\22082006016.jpg"));
        assertTrue(logContent.contains("USER[shared] DEL:shared"));
        assertTrue(logContent.contains("\\shared\\lg 002\\9999_08_22_16_19_33_22082009999.jpg.jpg"));
        assertTrue(logContent.contains("TOTAL for user [shared] CREATED(1) DELETES(1) ERRORS(0)"));

        assertTrue(logContent.contains("TOTAL for user [stuart] HIT(0) MISS(5) DEL(0)"));
        assertTrue(logContent.contains("USER[stuart] IMAGE ERR [2] : lg 002\\someText.jpg"));
        assertTrue(logContent.contains("TOTAL for user [stuart] CREATED(0) DELETES(0) ERRORS(1)"));
        

    }

    private String readNewLogCotent(String content) {
        File log = new File(configData.getLogPath() + File.separator + configData.getLogName() + ".log");
        if (log.exists()) {
            try {
                String all = new String(Files.readAllBytes(log.toPath()));
                return all.substring(content.length()).trim();
            } catch (IOException ex) {
                fail("Could not read log file [" + log.getAbsolutePath() + "]");
            }
        }
        return "";
    }
}
