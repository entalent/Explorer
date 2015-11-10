package cn.edu.bit.cs.explorer.util;


import android.os.StatFs;

/**
 * This class describes the state of a storage volume.
 */
public class StorageVolumeInfo {

    StatFs fs;
    /** the path to the storage volume */
    public String path;
    /** the available size */
    public long availableBytes;
    /** the total size */
    public long totalBytes;

    public StorageVolumeInfo(String path){
        fs = new StatFs(path);
        this.path = path;
        this.availableBytes = updateAvailableBytes();
        this.totalBytes = updateTotalBytes();
    }

    /**
     * judge a volume is available by the available size
     * @return whether the storage volume is mounted
     */
    public boolean isAvailableVolume(){
        return totalBytes > 0;
    }

    long updateAvailableBytes() {
        this.availableBytes = ((long)fs.getAvailableBlocks()) * fs.getBlockSize();
        return this.availableBytes;
    }

    long updateTotalBytes() {
        this.totalBytes = ((long)fs.getBlockCount() * fs.getBlockSize());
        return this.totalBytes;
    }
}