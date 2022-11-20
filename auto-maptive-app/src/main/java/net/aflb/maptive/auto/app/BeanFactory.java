package net.aflb.maptive.auto.app;

import net.aflb.maptive.auto.core.client.MaptiveClient;
import net.aflb.maptive.auto.core.io.MaptiveDataDao;
import net.aflb.maptive.auto.core.io.xlsx.XlsxMaptiveDataDao;
import net.aflb.maptive.auto.core.watcher.MaptiveModifiedHandler;
import net.aflb.maptive.auto.core.watcher.MaptiveWatcher;
import net.aflb.maptive.auto.core.watcher.UpdateMaptiveModifiedHandler;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import java.io.IOException;

@Dependent
public class BeanFactory {

    private Config config;

    public BeanFactory(Config config) {
        this.config = config;
    }

    @Produces
    public MaptiveWatcher watcher(MaptiveModifiedHandler handler, MaptiveClient client, MaptiveDataDao dao) {
        return new MaptiveWatcher(handler, client, dao);
    }

    @Produces
    public MaptiveModifiedHandler handler() {
        return new UpdateMaptiveModifiedHandler();
    }

    @Produces
    public MaptiveClient client() {
//        return RetrofitMaptiveClient.production(config.key(), config.map());
        return ResteasyMaptiveClient.production(config.key(), config.map());
//        return SimpleHttpClient.production(config.key(), config.map());
    }

    @Produces
    public MaptiveDataDao dao() throws IOException {
        return XlsxMaptiveDataDao.forFile(config.file());
    }
}
