package sviolet.turquoise.x.imageloader.server;

import java.io.File;

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>Load image from device local disk (for SourceType.LOCAL_DISK)</p>
 *
 * TODO developing
 *
 * @author S.Violet
 */
public class DiskLoadServer implements ComponentManager.Component, Server {

    private ComponentManager manager;
    private boolean assetsLoadingEnabled = true;

    private boolean initialized = false;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
    }

    private void initialize(){
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    //check assets cache path
                    if (!getComponentManager().getServerSettings().getAssetsCachePath().exists()) {
                        if (!getComponentManager().getServerSettings().getAssetsCachePath().mkdirs()) {
                            getComponentManager().getLogger().e("[TILoader:DiskLoadServer]Create assets cache path failed, disable assets loading");
                            assetsLoadingEnabled = false;
                        }
                    } else if (!getComponentManager().getServerSettings().getAssetsCachePath().isDirectory()) {
                        getComponentManager().getLogger().e("[TILoader:DiskLoadServer]Create assets cache path failed, find the same name file");
                        assetsLoadingEnabled = false;
                    }
                    getComponentManager().getLogger().i("[TILoader:DiskLoadServer]initialized");
                    initialized = true;
                }
            }
        }
    }

    @Override
    public Type getServerType() {
        return Type.DISK_LOAD;
    }

    /**
     * Read Image from device local disk
     * @param task task
     * @param decodeHandler used to decode file
     * @return ImageResource, might be null
     */
    public ImageResource readFromLocalDisk(Task task, DecodeHandler decodeHandler){
        initialize();
        //fetch cache file
        File targetFile = new File(task.getUrl());
        if (!targetFile.exists()){
            return null;
        }
        //decode
        return decodeHandler.decode(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                task, targetFile, getComponentManager().getLogger());
    }

    /**
     * Read Image from apk assets
     * @param task task
     * @param decodeHandler used to decode file
     * @return ImageResource, might be null
     */
    public ImageResource readFromAssets(Task task, DecodeHandler decodeHandler){
        initialize();

        //TODO developing

        return null;
    }

    private ComponentManager getComponentManager(){
        return manager;
    }

}
