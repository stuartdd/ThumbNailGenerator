/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.image;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author stuart
 */
public class MetaData {

    private static final SimpleDateFormat SDF1 = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");//2016:06:02 12:18:20
    private static final SimpleDateFormat SDF2 = new SimpleDateFormat("MMM dd HH:mm:ss YYYY");//Thu Jun 02 12:18:21 +01:00 2016
    private static final SimpleDateFormat SDF_OUT = new SimpleDateFormat("yyyy:MM:dd'T'HH:mm:ss");//2016:06:02 12:18:20

    private boolean cw;
    private boolean rotate;
    private int degrees;
    private boolean hasDateInfo = false;
    private String err;

    public String getErr() {
        return err;
    }
    private File file;
    private Date dateTimeOriginal;
    private String orientation;
    private String fileId;
    private long timeStamp;

    public MetaData(File file) {
        this.file = file;
        this.err = null;
    }

    public boolean isAspectRotated() {
        return ((degrees == 90) || (degrees == 270));
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public MetaData setErr(String err) {
        this.err = err;
        return this;
    }

    public boolean isCw() {
        return cw;
    }

    public boolean isRotate() {
        return rotate;
    }

    public int getDegrees() {
        return degrees;
    }

    public String getSorter() {
        if (dateTimeOriginal != null) {
            return SDF_OUT.format(dateTimeOriginal);
        }
        return file.getName();
    }

    public String getName() {
        return file.getName();
    }

    public boolean hasDateInfo() {
        return hasDateInfo;
    }

    public boolean hasError() {
        return err != null;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Date getDateTimeOriginal() {
        return dateTimeOriginal;
    }

    /**
     *
     * @param dateTimeOriginal
     */
    public void setDateTimeOriginal(String dateTimeOriginal, long fileDateTime) {
        try {
            this.dateTimeOriginal = SDF1.parse(dateTimeOriginal);
            this.hasDateInfo = true;
        } catch (ParseException ex) {
            Date date = parse(dateTimeOriginal, fileDateTime);
            if (date == null) {
                this.hasDateInfo = false;
            }
            this.dateTimeOriginal = date;
            this.hasDateInfo = true;
        }
    }

    public String getOrientation() {
        return orientation == null ? "" : orientation;
    }

    public void setOrientation(String orientation) {
        cw = false;
        rotate = false;
        degrees = 0;
        int pos = orientation.indexOf("Rotate");
        if (pos > 0) {
            rotate = true;
            String[] bits = orientation.substring(pos + "Rotate".length()).split(" ");
            for (String s : bits) {
                s = s.trim();
                if (s.endsWith(")")) {
                    s = s.substring(0, s.length() - 1);
                }
                if (s.startsWith("(")) {
                    s = s.substring(1);
                }
                if (s.contains("CW")) {
                    cw = true;
                }
                if (degrees == 0) {
                    try {
                        degrees = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                        degrees = 0;
                    }
                }
            }
        }
        this.orientation = "Rotate [" + degrees + "] " + cw;
    }

    @Override
    public String toString() {
        if (hasError()) {
            return "Meta{" + "file=" + file.getAbsolutePath() + ", ERROR=" + err + '}';
        }
        return "Meta{" + "file=" + file.getAbsolutePath() + ", dateTimeOriginal=" + dateTimeOriginal + ", orientation=" + orientation + '}';
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    /**
     * Thu Jun 02 12:18:21 +01:00 2016
     *
     * 1 Mar 2008 14:44:18
     *
     * @param s
     * @return
     */
    private Date parse(String s, long fileDateTime) {
        if (s.trim().isEmpty()) {
            return new Date(fileDateTime);
        }
        String newS = s;
        String[] bits = s.split(" ");
        if (bits.length > 4) {
            newS = bits[1] + " " + bits[2] + " " + bits[3] + " " + bits[5];
        } else {
            if (bits.length == 4) {
                newS = bits[1] + " " + bits[0] + " " + bits[3] + " " + bits[2];
            }
        }
        try {
            Date d = SDF2.parse(newS);
            this.hasDateInfo = true;
            return d;
        } catch (ParseException ex) {
            this.err = (this.err==null?"":this.err+"|") + "Invalid Modified Date ["+s+"]:" + ex.getMessage() + ". ";
            return null;
        }
    }

}
