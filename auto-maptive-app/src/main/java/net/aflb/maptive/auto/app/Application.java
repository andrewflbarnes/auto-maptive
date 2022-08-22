package net.aflb.maptive.auto.app;

import io.quarkus.scheduler.Scheduled;
import net.aflb.maptive.auto.core.watcher.MaptiveWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final MaptiveWatcher maptiveWatcher;

    public Application(MaptiveWatcher maptiveWatcher) {
        this.maptiveWatcher = maptiveWatcher;
    }

    @Scheduled(every = "{schedule}")
    public void checkAndUpdate() {
        logger.debug("Checking for updates...");
        maptiveWatcher.checkAndUpdate();
    }
}
