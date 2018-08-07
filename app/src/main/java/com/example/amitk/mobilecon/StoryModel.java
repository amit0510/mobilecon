package com.example.amitk.mobilecon;

/**
 * Created by amitk on 07-Feb-18.
 */

public class StoryModel {
    public String time;
    public String filename;
    public String filenamewithNumber;
    public String extension;

    public StoryModel(String time,String filename,String filenamewithNumber,String extension){
        this.filename=filename;
        this.time=time;
        this.filenamewithNumber=filenamewithNumber;
        this.extension=extension;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilenamewithNumber() {
        return filenamewithNumber;
    }

    public void setFilenamewithNumber(String filenamewithNumber) {
        this.filenamewithNumber = filenamewithNumber;
    }
}
