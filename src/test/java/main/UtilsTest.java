package main;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import main.config.ConfigData;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author stuart
 */
public class UtilsTest {

    private static SimpleDateFormat sdf;
    private static String ts;
    private static String EXT = ".jpg";

    ConfigData configData;
    Utils utils;

    @Before
    public void before() {
        configData = Utils.createConfigFromJsonFile("configThumbNailGen.json");
        sdf = new SimpleDateFormat(configData.getThumbNailTimeStamp());
        ts = sdf.format(new Date());
        utils = new Utils(configData);
    }

    @Test
    public void testThumbNailNameProcess() throws ParseException {
        String ds = "2019_03_25_13_48_30";
        Date d = sdf.parse("2019_03_25_13_48_30");
        
        String fn = "/my/pat/file.gif";    
        String tn = utils.thumbNailFileNameFromFileName(fn, d);
        assertEquals("/my/pat/" + ds + "_file.gif.jpg", tn);
        assertEquals(fn, utils.fileNameFromThumbNailName(tn));

        fn = "file.gif";
        tn = utils.thumbNailFileNameFromFileName(fn, d);
        assertEquals(ds + "_"+fn+".jpg", utils.thumbNailFileNameFromFileName(fn, d));
        assertEquals(fn, utils.fileNameFromThumbNailName(tn));
    }

}
