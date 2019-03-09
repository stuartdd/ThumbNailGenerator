package main;

import config.Config;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.image.CreateThumbNail;
import main.config.ConfigData;
import main.config.UserData;

public class Main {

    private static FileOutputStream logOutputStream;
    private static FileOutputStream logErrorOutputStream;
    private static final String NL = System.getProperty("line.separator");
    private static final String SEP = "--------------------------------------------------------------------------------------------------------------------";
    private static ConfigData configData;
    private static String logLineSeperator;
    private static int timeStampOffset;

        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String configArg = null;
        boolean diffArg = true;
        boolean fullScanArg = false;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.equalsIgnoreCase("-diff")) {
                    diffArg = true;
                    fullScanArg = false;
                }
                if (arg.equalsIgnoreCase("-full")) {
                    diffArg = false;
                    fullScanArg = true;
                }
            } else {
                configArg = arg;
            }
        }

        if (configArg != null) {
            configData = (ConfigData) Config.configFromJsonFile(ConfigData.class, args[0]);
        } else {
            configData = (ConfigData) Config.configFromJsonFile(ConfigData.class, "configThumbNailGen.json");
        }
        timeStampOffset = configData.formatFileTimeStamp(new Date()).length() + 1;
        initLog();

        if (diffArg) {
            doDiff();
        } else {
            if (fullScanArg) {
                doFullScan();
            }
        }

    }
    
    private static void doFullScan() {
        log(SEP);
        log("RUNNING FULL SCAN mode");
        int counterTotal = 0;
        int counterCreatedTotal = 0;
        long startTime1 = System.currentTimeMillis();
        for (String user : configData.getResources().getUsers().keySet()) {
            UserData userData = configData.getResources().getUsers().get(user);
            if (userData == null) {
                throw new GLoaderException("User '" + user + "' could not be located");
            }
            String imageRoot = userData.getImageRoot();
            File imageRootFile = new File(imageRoot);
            imageRootFile = new File(imageRootFile.getAbsolutePath());
            int counterNofN = 0;
            int counterCreated = 0;
            int counterSkippedNofN = 0;
            for (String imagePath : userData.getImagePaths()) {
                File imagePathFile;
                if (imagePath.length() == 0) {
                    imagePathFile = new File(imageRootFile.getAbsolutePath());
                } else {
                    imagePathFile = new File(imageRootFile.getAbsolutePath() + File.separator + imagePath);
                }
                imagePathFile = new File(imagePathFile.getAbsolutePath());
                log(SEP);
                log(time(startTime1) + ":SCANNING: User:" + user + ". Path:" + imagePathFile.getAbsolutePath());

                List<String> list = new ArrayList<>();
                listImagesForPath(list, imagePathFile, imageRootFile.getAbsolutePath().length());
                counterNofN = 0;
                counterSkippedNofN = 0;
                for (String s : list) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    }
                    long createTime = System.currentTimeMillis();
                    boolean created;
                    counterNofN++;
                    try {
                        created = CreateThumbNail.create(imageRootFile.getAbsolutePath(), s, user, configData.getThumbNailsRoot());
                        if (created) {
                            log(time(createTime) + ":IMAGE NEW : (" + counterNofN + " of " + list.size() + ") " + s + " ");
                            counterCreated++;
                        } else {
                            counterSkippedNofN++;
                            if ((counterSkippedNofN % 500) == 0) {
                                log(time(startTime1) + ":IMAGES SKIPPED: (" + counterSkippedNofN + " of " + list.size() + ')');
                            }
                        }
                    } catch (Exception ex) {
                        log(time(startTime1) + ":IMAGE ERR :  (" + counterNofN + " of " + list.size() + ") " + s + " Failed " + ex.getClass().getSimpleName() + ":" + ex.getMessage(), ex);
                    }
                }
                if ((counterSkippedNofN % 500) > 0) {
                    log(time(startTime1) + ":IMAGES SKIPPED: (" + counterSkippedNofN + " of " + list.size() + ")");
                }
                counterTotal = counterTotal + list.size();
            }
            counterCreatedTotal = counterCreatedTotal + counterCreated;
        }
        log(SEP);
        log(time(startTime1) + ":TOTAL: PROCESSED:" + counterTotal + " CREATED:" + counterCreatedTotal);
        log(SEP);

    }

    private static void doDiff() {
        log(SEP);
        log("RUNNING in DIFF mode");
        long startTime1 = System.currentTimeMillis();
        for (String user : configData.getResources().getUsers().keySet()) {
            UserData userData = configData.getResources().getUsers().get(user);
            if (userData == null) {
                throw new GLoaderException("User '" + user + "' could not be located");
            }
            File thumbNails = new File(configData.getThumbNailsRoot() + File.separator + user);
            Map<String, String> map = new HashMap<>();
            mapImagesForPath(map, thumbNails, thumbNails.getAbsolutePath().length());

            String imageRoot = userData.getImageRoot();
            File imageRootFile = new File(imageRoot);
            imageRootFile = new File(imageRootFile.getAbsolutePath());

            int hits = 0;
            int countCreated = 0;
            int countErrors = 0;
            int countDeletes = 0;
            for (String imagePath : userData.getImagePaths()) {

                File imagePathFile;
                if (imagePath.length() == 0) {
                    imagePathFile = new File(imageRootFile.getAbsolutePath());
                } else {
                    imagePathFile = new File(imageRootFile.getAbsolutePath() + File.separator + imagePath);
                }
                imagePathFile = new File(imagePathFile.getAbsolutePath());
                List<String> list = new ArrayList<>();
                listImagesForPath(list, imagePathFile, imageRootFile.getAbsolutePath().length());
                for (String s : list) {
                    if (map.containsKey(s)) {
                        map.put(s, "H");
                        hits++;
                    } else {
                        map.put(s, "M");
                    }
                }
            }
            List<String> missList = new ArrayList<>();
            List<String> delList = new ArrayList<>();

            for (Map.Entry<String, String> tag : map.entrySet()) {
                if (tag.getValue().equals("?")) {
                    delList.add(tag.getKey());
                }
                if (tag.getValue().equals("M")) {
                    missList.add(tag.getKey());
                }
            }
            log(SEP);
            log("TOTAL for user [" + user + "] HIT(" + hits + ") MISS(" + missList.size() + ") DEL(" + delList.size() + ")");
            for (String f : missList) {
                try {
                    boolean created = CreateThumbNail.create(imageRootFile.getAbsolutePath(), f, user, configData.getThumbNailsRoot());
                    if (created) {
                        countCreated++;
                        log("USER[" + user + "] IMAGE NEW : " + f);
                    } else {
                        log("USER[" + user + "] IMAGE SKIPPED : " + f);
                    }
                } catch (Exception ex) {
                    countErrors++;
                    log("USER[" + user + "] IMAGE ERR : " + f + " " + ex.getClass().getSimpleName() + ":" + ex.getMessage());
                    log("USER[" + user + "] IMAGE ERR : " + f + " " + ex.getClass().getSimpleName() + ":" + ex.getMessage(), ex);
                }

            }
            for (String f : delList) {
                File delFile = new File(thumbNails + File.separator + f);
                delFile = new File(delFile.getAbsolutePath());
                File[] list = delFile.getParentFile().listFiles();
                boolean hasBeenFound = false;
                for (File toDelete : list) {
                    if (toDelete.getName().contains(delFile.getName())) {
                        hasBeenFound = true;
                        if (toDelete.delete()) {
                            log("USER[" + user + "] DEL:" + user + ":" + toDelete.getAbsolutePath());
                            countDeletes++;
                        } else {
                            log("USER[" + user + "] IMAGE ERR:" + toDelete.getAbsolutePath() + " could not be deleted!");
                            logError("USER[" + user + "] IMAGE ERR:" + toDelete.getAbsolutePath() + " could not be deleted!");
                            countErrors++;
                        }
                    }
                }
                if (!hasBeenFound) {
                    log("USER[" + user + "] IMAGE ERR:" + delFile.getAbsolutePath() + " not recognised!");
                    logError("USER[" + user + "] IMAGE ERR:" + delFile.getAbsolutePath() + " not recognised!");
                }
            }
            log("TOTAL for user [" + user + "] CREATED(" + countCreated + ") DELETES(" + countDeletes + ") ERRORS(" + countErrors + ")");
            log(SEP);
        }
        log(time(startTime1) + ":TOTAL RUN  TIME");
        log(SEP);
    }


    public static ConfigData getConfigData() {
        return configData;
    }

    /**
     * media/Photos/2007-09-21_Lyns_Wedding_Photos/Gareth's
     * Pics/2007_09_21_21_27_15_Picture 142.jpg.jpg
     *
     * to
     *
     * media/Photos/2007-09-21_Lyns_Wedding_Photos/Gareth's Pics/Picture 142.jpg
     *
     * @param thName
     * @return
     */
    private static String thumbnailNameToFileName(String thName) {
        int pos = thName.lastIndexOf('/');
        return thName.substring(0, pos + 1) + thName.substring(pos + timeStampOffset + 1, thName.length() - 4);
    }

    private static void listImagesForPath(List<String> list, File path, int rootPathLength) {
        if (path.isDirectory()) {
            File[] files = path.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.getAbsolutePath().contains(".thumbnails")) {
                        return false;
                    }
                    return (Main.getConfigData().isImageFile(file));
                }
            });
            for (File f : files) {
                if (f.isDirectory()) {
                    listImagesForPath(list, f, rootPathLength);
                } else {
                    String relativeFilePath = f.getAbsolutePath().substring(rootPathLength);
                    while (relativeFilePath.startsWith(File.separator)) {
                        relativeFilePath = relativeFilePath.substring(File.separator.length());
                    }
                    list.add(relativeFilePath);
                }
            }
        }
    }

    private static void mapImagesForPath(Map<String, String> map, File path, int rootPathLength) {
        if (path.isDirectory()) {
            File[] files = path.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return (Main.getConfigData().isImageFile(file));
                }
            });
            for (File f : files) {
                if (f.isDirectory()) {
                    mapImagesForPath(map, f, rootPathLength);
                } else {
                    String relativeFilePath = f.getAbsolutePath().substring(rootPathLength);
                    while (relativeFilePath.startsWith(File.separator)) {
                        relativeFilePath = relativeFilePath.substring(File.separator.length());
                    }
                    map.put(thumbnailNameToFileName(relativeFilePath), "?");
                }
            }
        }
    }

    public static void initLog() {
        logLineSeperator = configData.getLogLineSeparator();
        if (logLineSeperator.equals("NL")) {
            logLineSeperator = System.getProperty("line.separator");
        }
        String fileName = createFileNameTimeStamp(configData.getLogFileName(), configData.formatLogFileTimeStamp());
        System.out.println("Log File Name is :" + fileName);
        File f = new File(fileName);
        f = new File(f.getAbsolutePath());
        System.out.println("Log File Name FINAL is :" + fileName);
        try {
            logOutputStream = new FileOutputStream(f, true);
        } catch (Exception ex) {
            throw new GLoaderException("Could not create logFile " + f.getAbsolutePath(), ex);
        }
        writeLog("Log created!", logOutputStream);

        fileName = createFileNameTimeStamp(configData.getLogErrorFileName(), configData.formatLogFileTimeStamp());
        System.out.println("Error Log File Name is :" + fileName);
        f = new File(fileName);
        f = new File(f.getAbsolutePath());
        System.out.println("Error Log File Name FINAL is :" + fileName);
        try {
            logErrorOutputStream = new FileOutputStream(f, true);
        } catch (Exception ex) {
            throw new GLoaderException("Could not create logFile " + f.getAbsolutePath(), ex);
        }
        writeLog("Error Log created!", logErrorOutputStream);
        writeLog("Error Log created! " + fileName, logOutputStream);
    }

    public static String createFileNameTimeStamp(String template, String timestamp) {
        int pos = template.indexOf("%{ts}");
        if (pos >= 0) {
            return template.substring(0, pos) + timestamp + template.substring(pos + "%{ts}".length());
        }
        return template;
    }

    public static String createFileName(String template, String fileName) {
        int pos = template.indexOf("%{fn}");
        if (pos >= 0) {
            return template.substring(0, pos) + fileName + template.substring(pos + "%{fn}".length());
        }
        return template;
    }

    public static void log(String message) {
        log(message, null);
    }

    public static void logError(String message) {
        if (configData == null) {
            writeLog(message, logErrorOutputStream);
        } else {
            if (configData.isVerbose()) {
                writeLog(message, logErrorOutputStream);
            }
        }
    }

    public static void log(String message, Throwable ex) {
        if (ex == null) {
            if (configData == null) {
                writeLog(message, logOutputStream);
            } else {
                if (configData.isVerbose()) {
                    writeLog(message, logOutputStream);
                }
            }
        } else {
            writeLog(message + NL + toStringException(ex), logErrorOutputStream);
        }
    }

    private static void writeLog(String message, FileOutputStream fos) {
        if (message == null) {
            message = null;
        }
        System.out.println(message);
        if (fos != null) {
            try {
                if (configData == null) {
                    fos.write(("NO_CONFIG:" + message + logLineSeperator).getBytes(Charset.defaultCharset()));
                } else {
                    fos.write((configData.formatLogLineTimeStamp() + ":" + message + logLineSeperator).getBytes(Charset.defaultCharset()));
                }
            } catch (Exception io) {
                System.err.println("Failed to write to log file:" + message + ":EX:" + io.getMessage());
            }
        }
    }

    private static String toStringException(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    private static long MS_SEC = 1000;
    private static long MS_M1N = MS_SEC * 60;
    private static long MS_HOU = MS_M1N * 60;

    private static String time(long startTime) {
        long msDiff = System.currentTimeMillis() - startTime;
        long h = msDiff / MS_HOU;
        msDiff = msDiff - (MS_HOU * h);
        long m = msDiff / MS_M1N;
        msDiff = msDiff - (MS_M1N * m);
        long s = msDiff / MS_SEC;
        msDiff = msDiff - (MS_SEC * s);
        return pad2(h) + ":" + pad2(m) + ":" + pad2(s) + "." + pad3(msDiff);
    }

    private static String pad3(long n) {
        if (n < 10) {
            return "00" + n;
        } else {
            if (n < 100) {
                return "0" + n;
            } else {
                return "" + n;
            }
        }
    }

    private static String pad2(long n) {
        if (n < 10) {
            return "0" + n;
        } else {
            return "" + n;
        }
    }
}
