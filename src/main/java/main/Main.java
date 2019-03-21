package main;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.image.CreateThumbNail;
import main.config.ConfigData;
import main.config.ConfigDataException;
import main.config.UserData;

public class Main {

    private static final String SEP = "--------------------------------------------------------------------------------------------------------------------";
    private static ConfigData configData;
    private static Utils utils;
    private static final ObjectMapper jsonMapper;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static ConfigData getConfigData() {
        return configData;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String configArg = null;
        boolean dryRunOverride = false;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.equalsIgnoreCase("-dryrun")) {
                    dryRunOverride = true;
                }
            } else {
                configArg = arg;
            }
        }

        if (configArg != null) {
            configData = (ConfigData) utils.createConfigFromJsonFile(args[0]);
        } else {
            configData = (ConfigData) utils.createConfigFromJsonFile("configThumbNailGen.json");
        }
        if (dryRunOverride) {
            configData.setDryRun(true);
        }
        utils = Utils.instance(configData);

        try {
            doDiff();
        } catch (IOException ex) {
            utils.logErr(ex);
        }
    }

    private static void doDiff() throws IOException {
        int logLineCounter = 0;
        utils.log(SEP, false);
        long startTime1 = System.currentTimeMillis();
        File thumbNailsRoot = new File(configData.getThumbNailsRoot());
        if (!thumbNailsRoot.exists()) {
            throw new ConfigDataException("ThumbNailsRoot: [" + thumbNailsRoot + "] does not exist. Please create it");
        }
        /*
        For each user in the config data
         */
        for (String user : configData.getResources().getUsers().keySet()) {
            long prepTime = System.currentTimeMillis();
            UserData userData = configData.getResources().getUsers().get(user);
            if (userData == null) {
                throw new GLoaderException("User '" + user + "' data is null");
            }
            /*
            Thumb nails for a user are stored at thunb nail root + the users name
             */
            thumbNailsRoot = new File(thumbNailsRoot.getAbsolutePath() + File.separator + user);
            /*
                Create a map of ALL the the thumbNails for the user. 
                Path excludes root path.
                The names will have the date-time and .jpg stripped off them so they are original image names.
             */
            Map<String, ThumbNailStatus> thumbNailMap = new HashMap<>();
            mapThumbNailsForPath(thumbNailMap, thumbNailsRoot, thumbNailsRoot.getAbsolutePath().length());

            /*
                Each users image root directory
             */
            String imageRoot = userData.getImageRoot();
            File imageRootFile = new File((new File(imageRoot)).getAbsolutePath());

            int hits = 0;
            int countCreated = 0;
            int countErrors = 0;
            int countDeletes = 0;
            /*
            For each imageDirectory in the users image root directory
             */
            for (String imagePath : userData.getImagePaths()) {
                /*
                Path to the base of the image directory
                 */
                File imagePathFile;
                if (imagePath.length() == 0) {
                    imagePathFile = new File(imageRootFile.getAbsolutePath());
                } else {
                    imagePathFile = new File(imageRootFile.getAbsolutePath() + File.separator + imagePath);
                }

                imagePathFile = new File(imagePathFile.getAbsolutePath());
                List<String> list = new ArrayList<>();
                /*
                List all of the files in the users original images directory
                 */
                listImagesForPath(list, imagePathFile, imageRootFile.getAbsolutePath().length());

                for (String s : list) {
                    ThumbNailStatus thumbNailStatus = thumbNailMap.get(s);
                    if (thumbNailStatus != null) {
                        thumbNailStatus.setState(ThumbNailState.HIT);
                        hits++;
                    } else {
                        thumbNailMap.put(s, new ThumbNailStatus(s, ThumbNailState.MISS));
                    }
                }
            }

            List<String> missList = new ArrayList<>();
            List<String> delList = new ArrayList<>();

            for (Map.Entry<String, ThumbNailStatus> status : thumbNailMap.entrySet()) {
                switch (status.getValue().getState()) {
                    case MISS:
                        missList.add(status.getKey());
                        break;
                    case UNKNOWN:
                        delList.add(status.getKey());
                        break;
                }
            }

            utils.log("TOTAL for user [" + user + "] HIT(" + hits + ") MISS(" + missList.size() + ") DEL(" + delList.size() + ") PREPARATION(" + (System.currentTimeMillis() - prepTime) + "ms)", false);
            prepTime = System.currentTimeMillis();
            for (String originalFileName : missList) {
                try {
                    boolean created = CreateThumbNail.create(imageRootFile.getAbsolutePath(), originalFileName, user, configData.getThumbNailsRoot(), configData.isDryRun());
                    if (created) {
                        countCreated++;
                        utils.log("USER[" + user + "] IMAGE NEW [" + logLineCounter + "] : " + originalFileName, false);
                    } else {
                        utils.log("USER[" + user + "] IMAGE SKIPPED [" + logLineCounter + "] : " + originalFileName, ((logLineCounter % 50) != 0));
                    }
                } catch (Exception ex) {
                    countErrors++;
                    utils.logErr("USER[" + user + "] IMAGE ERR [" + logLineCounter + "] : " + originalFileName + " " + ex.getClass().getSimpleName() + ":" + ex.getMessage(), ex);
                }
                logLineCounter++;
            }

            for (String f : delList) {
                String thumbNailName = thumbNailsRoot + File.separator + thumbNailMap.get(f).getName();
                File delFile = new File(thumbNailName);
                delFile = new File(delFile.getAbsolutePath());
                boolean deletedOk;
                if (!configData.isDryRun()) {
                    deletedOk = delFile.delete();
                } else {
                    deletedOk = true;
                }
                if (deletedOk) {
                    utils.log("USER[" + user + "] DEL:" + user + ":" + delFile.getAbsolutePath(), false);
                    countDeletes++;
                } else {
                    utils.logErr("USER[" + user + "] IMAGE ERR:" + delFile.getAbsolutePath() + " could not be deleted!");
                    countErrors++;
                }
            }
            utils.log("TOTAL for user [" + user + "] CREATED(" + countCreated + ") DELETES(" + countDeletes + ") ERRORS(" + countErrors + ") PROCESS(" + (System.currentTimeMillis() - prepTime) + "ms)", false);
            if (countErrors > 0) {
                utils.logErr("TOTAL for user [" + user + "] CREATED(" + countCreated + ") DELETES(" + countDeletes + ") ERRORS(" + countErrors + ") PROCESS(" + (System.currentTimeMillis() - prepTime) + "ms)");
            }
            utils.log(SEP, false);
        }
        utils.log(time(startTime1) + ":TOTAL RUN  TIME", false);
        utils.log(SEP, false);
    }

    private static void listImagesForPath(List<String> list, File path, int rootPathLength) {
        if (path.isDirectory()) {
            File[] files = path.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.getAbsolutePath().contains(".thumbnails")) {
                        return false;
                    }
                    return (configData.isImageFile(file));
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
            utils.log("Scanned images:" + path.getAbsolutePath() + ". Found [" + list.size() + "] files", true);
        }
    }

    private static void mapThumbNailsForPath(Map<String, ThumbNailStatus> map, File path, int rootPathLength) {
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    mapThumbNailsForPath(map, f, rootPathLength);
                } else {
                    String name = f.getName();
                    String parent;
                    String relativeParent;
                    File parentFile = f.getParentFile();
                    if (parentFile == null) {
                        parent = "/";
                        relativeParent = "";
                    } else {
                        parent = parentFile.getAbsolutePath();
                        relativeParent = parent.substring(rootPathLength);
                        while (relativeParent.startsWith(File.separator)) {
                            relativeParent = relativeParent.substring(File.separator.length());
                        }
                    }
                    map.put(relativeParent + File.separator + utils.fileNameFromThumbNailName(name), new ThumbNailStatus(relativeParent + File.separator + name, ThumbNailState.UNKNOWN.UNKNOWN));
                }
            }
        }
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
