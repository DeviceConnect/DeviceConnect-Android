package org.allseen.LSF.helper;

import org.alljoyn.bus.BusAttachment;
import org.allseen.lsf.helper.facade.Group;
import org.allseen.lsf.helper.facade.Lamp;
import org.allseen.lsf.helper.facade.Scene;
import org.allseen.lsf.helper.listener.AllJoynListener;
import org.allseen.lsf.helper.manager.AllJoynManager;
import org.allseen.lsf.helper.manager.LightingSystemManager;
import org.allseen.lsf.helper.manager.LightingSystemQueue;

/*
 * org.allseen.lsf.helper.facade.LightingDirectoのLightingSystemManagerをアクセス可能に変更。
 */
public class LightingDirector {
    private final LightingSystemManager lightingManager;

    public LightingDirector(LightingSystemQueue queue) {
        this.lightingManager = new LightingSystemManager(queue);
    }

    public int getVersion() {
        return 1;
    }

    public void start(String applicationName) {
        this.lightingManager.init(applicationName, new AllJoynListener() {
            public void onAllJoynInitialized() {
                LightingDirector.this.lightingManager.start();
            }
        });
    }

    public void stop() {
        this.lightingManager.destroy();
    }

    public BusAttachment getBusAttachment() {
        return AllJoynManager.bus;
    }

    public Lamp[] getLamps() {
        return this.lightingManager.getLampCollectionManager().getLamps();
    }

    public Group[] getGroups() {
        return this.lightingManager.getGroupCollectionManager().getGroups();
    }

    public Scene[] getScenes() {
        return this.lightingManager.getSceneCollectionManager().getScenes();
    }

    public void postOnNextControllerConnection(Runnable task, int delay) {
        this.lightingManager.postOnNextControllerConnection(task, delay);
    }

    public LightingSystemManager getLightingManager() {
        return lightingManager;
    }
}
