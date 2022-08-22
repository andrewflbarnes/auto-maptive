package net.aflb.maptive.auto.core.watcher;

import net.aflb.maptive.auto.core.client.MaptiveClient;
import net.aflb.maptive.auto.core.io.MaptiveDataDao;

import java.io.IOException;
import java.util.TimerTask;

public class MaptiveWatcherTask extends TimerTask {

    private final MaptiveModifiedHandler handler;
    private final MaptiveClient client;
    private final MaptiveDataDao dao;
    private long trigger;

    public MaptiveWatcherTask(MaptiveModifiedHandler handler, MaptiveClient client, MaptiveDataDao dao) {
        this.handler = handler;
        this.client = client;
        this.dao = dao;
        trigger = dao.lastModified();
        update();
    }

    @Override
    public void run() {
        final var lastMod = dao.lastModified();
        if (lastMod > trigger) {
            trigger = lastMod;
            update();
        }
    }

    void update() {
        try {
            final var serverData = client.getAll().getResult();
            final var localData = dao.getAll();
            handler.onUpdate(serverData, localData, dao, client);
        } catch (IOException e) {
            System.err.println("Unable to get external data: ");
            e.printStackTrace();
        }
    }
}
