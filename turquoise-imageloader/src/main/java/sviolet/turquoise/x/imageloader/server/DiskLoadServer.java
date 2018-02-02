package sviolet.turquoise.x.imageloader.server;

import java.io.File;

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>Load image from device local disk (for SourceType.LOCAL_DISK)</p>
 *
 * @author S.Violet
 */
public class DiskLoadServer implements ComponentManager.Component, Server {

    private ComponentManager manager;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
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
    public ImageResource read(Task task, DecodeHandler decodeHandler){
        //fetch cache file
        File targetFile = getFile(task);
        if (!targetFile.exists()){
            return null;
        }
        //decode
        ImageResource imageResource = null;
        try {
            imageResource = decodeHandler.decode(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                    task, targetFile, getComponentManager().getLogger());
            if (imageResource == null){
                getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader:DiskLoadServer]decoding failed, return null ImageResource"), getComponentManager().getLogger());
            }
        }catch(Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
        }
        return imageResource;
    }

    private File getFile(Task task){
        return new File(task.getUrl());
    }

    private ComponentManager getComponentManager(){
        return manager;
    }

}
