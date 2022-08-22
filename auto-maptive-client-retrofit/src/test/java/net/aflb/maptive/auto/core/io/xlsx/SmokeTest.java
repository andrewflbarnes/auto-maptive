package net.aflb.maptive.auto.core.io.xlsx;

import net.aflb.maptive.auto.client.retrofit.RetrofitMaptiveClient;
import net.aflb.maptive.auto.core.client.MaptiveClient;
import net.aflb.maptive.auto.core.io.MaptiveDataDao;
import net.aflb.maptive.auto.core.watcher.MaptiveWatcherTask;
import net.aflb.maptive.auto.core.watcher.UpdateMaptiveModifiedHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

class SmokeTest {

    private static final String FILENAME = "/Users/andrew.barnes/development/repo/github.com/andrewflbarnes/auto-maptive/Adjusted.xlsx";
    private static final String APIKEY = "...";
    private static final String MAP_KEY = "...";

    private MaptiveClient client;
    private MaptiveDataDao dao;

    @BeforeEach
    void setUp() throws Exception {
        client = RetrofitMaptiveClient.production(APIKEY, MAP_KEY);
        dao = XlsxMaptiveDataDao.forFile(FILENAME);
    }

    @Test
    void test() throws Exception {
//        System.out.println(client.deleteAll());

        final var maptiveData = XlsxMaptiveDataDao.forFile(FILENAME);

        System.out.println(client.addAll(maptiveData.getAll().values()));

        if (true) {
            return;
        }


        maptiveData.getAll().entrySet()
            .stream()
//            .limit(1)
            .forEach(e -> {
                try {
                    final var md = e.getValue();
                    final var id = e.getKey().id();
                    System.out.printf("Adding %s: %s\n", id, md);
//                    System.out.println(client.delete(List.of(id)));
                    System.out.println(client.add(md));
                    System.out.println(client.get(List.of(id)));
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            });
//        System.out.println(client.delete(List.of("999999")));
    }

    @Test
    void smoke() throws Exception {
        final var watcher = new MaptiveWatcherTask(new UpdateMaptiveModifiedHandler(), client, dao);
        final var timer = new Timer();
        timer.schedule(watcher, 0, 1000);

        Thread.sleep(1000000);
    }

    @Test
    void get() throws Exception {
        System.out.println(client.get(List.of("8888")));
    }
}