/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import main.GLoaderException;
import main.Main;
import main.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author stuar
 */
public class CreateThumbNail {

    private static final Logger logger = LogManager.getLogger("Main");
    private static final String FORMAT = "jpg";
    private static Map<String, String> createOnce = new HashMap<String, String>();

    public static boolean create(String userBasePath, String pathToFileAndName, String user, String thumbNailBasePath, boolean dryRun) {
        File userBasePathFile = new File(userBasePath);
        userBasePathFile = new File(userBasePathFile.getAbsolutePath());
        if (!userBasePathFile.exists()) {
            throw new GLoaderException("User base path [" + userBasePathFile.getAbsolutePath() + "] not found");
        }

        File fromFile = new File(userBasePath + File.separator + pathToFileAndName);
        fromFile = new File(fromFile.getAbsolutePath());
        if (!fromFile.exists()) {
            throw new GLoaderException("Image file [" + fromFile.getAbsolutePath() + "] not found");
        }

        File thumbNailBasePathFile = new File(thumbNailBasePath);
        thumbNailBasePathFile = new File(thumbNailBasePathFile.getAbsolutePath());
        if (!thumbNailBasePathFile.exists()) {
            throw new GLoaderException("Thumbnail library [" + thumbNailBasePathFile.getAbsolutePath() + "] not found");
        }

        thumbNailBasePathFile = new File(thumbNailBasePathFile.getAbsolutePath() + File.separator + user);
        if (notAdded(thumbNailBasePathFile)) {
            if (!thumbNailBasePathFile.exists()) {
                Utils.instance().log("*** Create ThumbNail *** base path:" + thumbNailBasePathFile.getAbsolutePath(), true);
                if (!dryRun) {
                    if (!thumbNailBasePathFile.mkdir()) {
                        throw new GLoaderException("Thumbnail library [" + thumbNailBasePathFile.getAbsolutePath() + "] could not be created.");
                    }
                }
            }
        }

        thumbNailBasePathFile = new File(thumbNailBasePathFile.getAbsolutePath() + File.separator + pathToFileAndName);
        File thumbnailParent = thumbNailBasePathFile.getParentFile();
        if (notAdded(thumbnailParent)) {
            if (!thumbnailParent.exists()) {
                Utils.instance().log("*** Create ThumbNail *** path:" + thumbnailParent.getAbsolutePath(), true);
                if (!dryRun) {
                    if (!thumbnailParent.mkdirs()) {
                        throw new GLoaderException("Thumbnail library [" + thumbnailParent.getAbsolutePath() + "] could not be created.");
                    }
                }
            }
        }

        MetaData metaData = imageData(fromFile);
        if (metaData.hasError()) {
            throw new GLoaderException("Failed to read image metadata " + metaData.getErr());
        }
        
        String fullFileName = thumbnailParent.getAbsolutePath() + File.separator
                + Main.getConfigData().formatThumbNailFileTimeStamp(metaData.getDateTimeOriginal())
                + "_" + fromFile.getName() + '.' + FORMAT;

        File fullFileNameFile = new File(fullFileName);
        if (fullFileNameFile.exists()) {
            return false;
        }

        BufferedImage bufferedImage = ImageUtils.renderImage(metaData);
        try {
            if (!dryRun) {
                ImageIO.write(bufferedImage, FORMAT, fullFileNameFile);
            }
        } catch (IOException ex) {
            throw new GLoaderException("Failed to create thumbnail " + fullFileNameFile.getAbsolutePath(), ex);
        }
        return true;
    }

    private static MetaData imageData(final File file) {
        MetaData m = new MetaData(file);
        m.setTimeStamp(file.lastModified());
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (tag.getTagName().equals("Date/Time Original")) {
                        m.setDateTimeOriginal(tag.getDescription(), file.lastModified()); // 2016:06:02 12:18:20
                    }
                    if (tag.getTagName().equals("Orientation")) {
                        m.setOrientation(tag.getDescription());
                    }
                    if (m.getDateTimeOriginal() == null) {
                        if (tag.getTagName().equals("File Modified Date")) {
                            m.setDateTimeOriginal(tag.getDescription(), file.lastModified());
                        }
                    }
                }
            }
            if (m.hasDateInfo()) {
                return m;
            } else {
                logger.info("[NO DATE - FILE] " + file.getAbsolutePath());
            }
        } catch (ImageProcessingException | IOException ex) {
            return m.setErr(ex.getMessage());
        } catch (Exception ex) {
            String message = "Image File:"+(file==null?"":file.getAbsolutePath())+" threw Exception:"+ex.getClass().getSimpleName()+": Message:"+ex.getMessage();
            Utils.instance().log(message, false);
            Utils.instance().logErr(message, ex);
            return m.setErr(message);
        }
        return m.setErr("No Date Information found!");
    }

    private static boolean notAdded(File f) {
        if (!createOnce.containsKey(f.getAbsolutePath())) {
            createOnce.put(f.getAbsolutePath(), "");
            return true;
        }
        return false;
    }

}
