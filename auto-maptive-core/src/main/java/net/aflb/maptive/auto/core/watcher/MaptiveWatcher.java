package net.aflb.maptive.auto.core.watcher;

import net.aflb.maptive.auto.core.client.MaptiveClient;
import net.aflb.maptive.auto.core.io.MaptiveDataDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MaptiveWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaptiveWatcher.class);

    private final MaptiveModifiedHandler handler;
    private final MaptiveClient client;
    private final MaptiveDataDao dao;
    private long trigger;

    public MaptiveWatcher(MaptiveModifiedHandler handler, MaptiveClient client, MaptiveDataDao dao) {
        this.handler = handler;
        this.client = client;
        this.dao = dao;
        trigger = dao.lastModified();
    }

    public void checkAndUpdate() {
        final var lastMod = dao.lastModified();
        if (lastMod > trigger) {
            LOGGER.info("Local data has been modified, updating...");
            trigger = lastMod;
            doUpdate();
        }
    }

    public void forceUpdate() {
        LOGGER.warn("Force updating...");
        trigger = dao.lastModified();
        doUpdate();
    }

    private void doUpdate() {
        try {
            final var serverData = client.getAll().getResult();
            final var localData = dao.getAll();
            handler.onUpdate(serverData, localData, dao, client);
            LOGGER.info("Update complete.");
        } catch (IOException e) {
            LOGGER.error("Unable to get external data and process update", e);
        }
    }
}
