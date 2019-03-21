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
    File deletedThumbNail1;
    File deletedThumbNail2;

    @Test
    public void testMain() {
        /*
        read the config so we can find where stuff is to check!
        */
        configFileName = "configThumbNailGenTest" + Utils.resolveOS().name().toUpperCase() + ".json";
        configData = (ConfigData) utils.createConfigFromJsonFile(configFileName);
        utils = Utils.instance(configData);
        /*
        Read the logs to get the current content length
        */
        logContent = readNewLogCotent(0);

        /*
        Delete a thumbnail to test it being re-created
        */
        File f = new File(configData.getThumbNailsRoot());
        if (f.exists()) {
            deletedThumbNail1 = new File(f.getAbsolutePath() + File.separator + "shared/lg 002/2006_08_22_16_19_33_22082006016.jpg.jpg");
            if (deletedThumbNail1.exists()) {
                if (!deletedThumbNail1.delete()) {
                    fail("Could not delete file [" + deletedThumbNail1.getAbsolutePath() + "]");
                }
            }
            deletedThumbNail2 = new File(f.getAbsolutePath() + File.separator + "stuart/lg 001/2006_08_22_15_53_15_22082006010.jpg.jpg");
            if (deletedThumbNail2.exists()) {
                if (!deletedThumbNail2.delete()) {
                    fail("Could not delete file [" + deletedThumbNail2.getAbsolutePath() + "]");
                }
            }
        } else {
            fail("Could not find:"+f.getAbsolutePath());
        }
        /*
        Create a temp (mock) thumbnail file. This should be deleted by the process as it has no original image
        */
        tempFile = new File(f.getAbsolutePath() + File.separator + "shared/lg 002/9999_08_22_16_19_33_22082009999.jpg.jpg");
        try {
            Files.write(tempFile.toPath(), "HelloWorld".getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            fail("Could not create file [" + tempFile.getAbsolutePath() + "]");
        }
        if (!tempFile.exists()) {
            fail("Temp file: "+tempFile.getAbsolutePath()+" was not created");
        }
        /*
        Run the process.
        */
        Main.main(new String[]{configFileName});

        /*
        Clean up temp file
        */
        if (tempFile.exists()) {
            tempFile.delete();
            fail("Thumbnail :"+tempFile.getAbsolutePath()+" should have been deleted");
        }
        /*
        Check the TN was created
        */
        if (!deletedThumbNail1.exists()) {
            fail("Thumbnail :"+deletedThumbNail1.getAbsolutePath()+" was not re-created");
        }
        if (!deletedThumbNail2.exists()) {
            fail("Thumbnail :"+deletedThumbNail2.getAbsolutePath()+" was not re-created");
        }

        /*
        Get the content of the logs (for this run only)
        */
        logContent = readNewLogCotent(logContent.length());
        assertTrue(logContent.length() > 1400);
        assertTrue(logContent.length() < 1900);
        assertTrue(logContent.contains("Config file name: "+configFileName));
        assertTrue(logContent.contains("INFO  TN-Scan: TOTAL for user [shared] HIT(3) MISS(1) DEL(1) PREPARATION("));
        assertTrue(logContent.contains("INFO  TN-Scan: USER[shared] NEW IMAGE: lg 002\\22082006016.jpg"));
        assertTrue(logContent.contains("INFO  TN-Scan: USER[shared] DEL IMAGE: lg 002\\9999_08_22_16_19_33_22082009999.jpg.jpg"));
        assertTrue(logContent.contains("TOTAL for user [shared] CREATED(1) DELETES(1) ERRORS(0) PROCESS("));
        assertTrue(logContent.contains("INFO  TN-Scan: TOTAL for user [stuart] HIT(3) MISS(2) DEL(0) PREPARATION("));
        assertTrue(logContent.contains("ERROR TN-Scan: USER[stuart] IMAGE ERR: lg 002\\someText.jpg"));
        assertTrue(logContent.contains("INFO  TN-Scan: USER[stuart] NEW IMAGE: lg 001\\22082006010.jpg"));
        assertTrue(logContent.contains("INFO  TN-Scan: TOTAL for user [stuart] CREATED(1) DELETES(0) ERRORS(1) PROCESS("));
    }

    private String readNewLogCotent(int contentLength) {
        File log = new File(configData.getLogPath() + File.separator + configData.getLogName() + ".log");
        if (log.exists()) {
            try {
                String all = new String(Files.readAllBytes(log.toPath()));
                return all.substring(contentLength).trim();
            } catch (IOException ex) {
                fail("Could not read log file [" + log.getAbsolutePath() + "]");
            }
        }
        return "";
    }
}
