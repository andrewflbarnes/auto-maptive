package net.aflb.maptive.auto.core.io.xlsx;

import com.sun.java.accessibility.util.AccessibilityListenerList;
import net.aflb.maptive.auto.client.retrofit.RetrofitMaptiveClient;
import net.aflb.maptive.auto.core.MaptiveId;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class SmokeTest {

    private static final String FILENAME = "/Users/andrew.barnes/development/repo/github.com/andrewflbarnes/auto-maptive/Adjusted.xlsx";
    private static final String APIKEY = "...";
    private static final String MAP_KEY = "...";

    @Test
    void test() throws Exception {
        final var client = RetrofitMaptiveClient.production();

//        System.out.println(client.deleteAll(APIKEY, MAP_KEY));

        final var maptiveData = XlsxMaptiveDataDao.forFile(FILENAME);

        System.out.println(client.addAll(APIKEY, MAP_KEY, maptiveData.getAll().values()));

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
//                    System.out.println(client.delete(APIKEY, MAP_KEY, List.of(id)));
                    System.out.println(client.add(APIKEY, MAP_KEY, md));
                    System.out.println(client.get(APIKEY, MAP_KEY, List.of(id)));
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            });
//        System.out.println(client.delete(APIKEY, MAP_KEY, List.of("999999")));
    }

    @Test
    void loop() throws Exception {
        final var f = new File(FILENAME);
        var trigger = f.lastModified();
        compare();
        while (true) {
            Thread.sleep(1000);
            final var lastMod = f.lastModified();
            if (lastMod > trigger) {
                trigger = lastMod;
                compare();
            }
        }
    }

    @Test
    void compare() throws Exception {
        final var client = RetrofitMaptiveClient.production();

        final var serverData = client.getAll(APIKEY, MAP_KEY).getResult();
        final var serverIds = serverData.stream()
            .map(md -> md.get(2))
            .collect(Collectors.toSet());

        final var dao = XlsxMaptiveDataDao.forFile(FILENAME);
        final var localData = dao.getAll();
        final var localIds = localData.keySet().stream()
            .map(MaptiveId::id)
            .collect(Collectors.toSet());


        final var toAddIds = localIds.stream()
            .filter(id -> !serverIds.contains(id))
            .collect(Collectors.toSet());

        final var toAdd = localData.entrySet().stream()
            .filter(e -> toAddIds.contains(e.getKey().id()))
            .map(Map.Entry::getValue)
            .toList();

        System.out.println("Add (" + toAddIds.size() + "): " + toAddIds);
        if (!toAddIds.isEmpty()) {
            System.out.println(client.addAll(APIKEY, MAP_KEY, toAdd));
        }

        final var toDeleteIds = serverIds.stream()
            .filter(id -> !localIds.contains(id))
            .collect(Collectors.toSet());

        System.out.println("Delete (" + toDeleteIds.size() + "): " + toDeleteIds);
        if (!toDeleteIds.isEmpty()) {
            System.out.println(client.delete(APIKEY, MAP_KEY, new ArrayList<>(toDeleteIds)));
        }

        final var checkToUpdateIds = localIds.stream()
            .filter(id -> !toAddIds.contains(id))
            .toList();

        final var simpleLocalData = localData.entrySet().stream()
            .filter(e -> checkToUpdateIds.contains(e.getKey().id()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new ArrayList<>(e.getValue().getData().values())
            ));

        final var toUpdateIds = simpleLocalData.entrySet().stream()
            .filter(e -> !serverData.contains(e.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        System.out.println("Update (" + toUpdateIds.size() + "): " + toUpdateIds.stream().map(MaptiveId::id).toList());
//        toUpdateIds.forEach(id -> {
//            System.out.println(id);
//            System.out.println(simpleLocalData.get(id));
//            System.out.println(serverData.stream().filter(d -> id.equals(d.get(2))).findFirst().get());
//        });
        for (final var id : toUpdateIds) {
            System.out.println(client.update(APIKEY, MAP_KEY, id, localData.get(id).getIdLessColumnData()));
        }
    }

    @Test
    void get() throws Exception {
        final var client = RetrofitMaptiveClient.production();

        System.out.println(client.get(APIKEY, MAP_KEY, List.of("8888")));
    }
}