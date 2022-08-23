package net.aflb.maptive.auto.simple.app;

import net.aflb.maptive.auto.client.retrofit.RetrofitMaptiveClient;
import net.aflb.maptive.auto.core.client.MaptiveClient;
import net.aflb.maptive.auto.core.io.MaptiveDataDao;
import net.aflb.maptive.auto.core.io.xlsx.NotXlsxFileException;
import net.aflb.maptive.auto.core.io.xlsx.XlsxMaptiveDataDao;
import net.aflb.maptive.auto.core.watcher.MaptiveModifiedHandler;
import net.aflb.maptive.auto.core.watcher.MaptiveWatcher;
import net.aflb.maptive.auto.core.watcher.UpdateMaptiveModifiedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            final var config = AppConfig.from("application.properties");
            final var app = new Application(config);
            registerShutdownHook(app::destroy);
            app.start();
        } catch (AppException e) {
            LOGGER.error(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            LOGGER.error("Error while starting application :(", e);
            System.exit(1);
        }
    }

    private static void registerShutdownHook(Runnable onShutdown) {
        final var shutdownThread = new Thread(onShutdown, "shutdown-hook");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    private final AppConfig config;
    private MaptiveWatcher watcher = null;
    private Timer timer;

    public Application(AppConfig config) {
        this.config = config;
    }

    public void start() throws IOException {
        this.watcher = new MaptiveWatcher(
            buildHandler(),
            buildClient(),
            buildDao());

        watcher.forceUpdate();

        final var delay = config.getSchedule();

        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                watcher.checkAndUpdate();
            }
        }, delay, delay);
    }

    public void destroy() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private MaptiveModifiedHandler buildHandler() {
        return new UpdateMaptiveModifiedHandler();
    }

    private MaptiveClient buildClient() {
        return RetrofitMaptiveClient.production(config.getApiKey(), config.getMapId());
    }

    private MaptiveDataDao buildDao() throws IOException {
        try {
            return XlsxMaptiveDataDao.forFile(config.getFile());
        } catch (NotXlsxFileException e) {
            throw new AppException(e.getMessage());
        }
    }
}
