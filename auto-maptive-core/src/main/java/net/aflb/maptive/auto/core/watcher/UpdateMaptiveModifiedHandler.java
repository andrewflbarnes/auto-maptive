package net.aflb.maptive.auto.core.watcher;

import net.aflb.maptive.auto.core.MaptiveData;
import net.aflb.maptive.auto.core.MaptiveId;
import net.aflb.maptive.auto.core.client.MaptiveClient;
import net.aflb.maptive.auto.core.io.MaptiveDataDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateMaptiveModifiedHandler implements MaptiveModifiedHandler {

    @Override
    public void onUpdate(List<List<String>> serverData, Map<MaptiveId, MaptiveData> localData, MaptiveDataDao dao, MaptiveClient client) {
        final var serverIds = serverData.stream()
            .map(md -> md.get(2))
            .collect(Collectors.toSet());

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
            try {
                System.out.println(client.addAll(toAdd));
            } catch (Exception e) {
                System.err.println("Failed to add maptive data: " + toAddIds);
                e.printStackTrace();
            }
        }

        final var toDeleteIds = serverIds.stream()
            .filter(id -> !localIds.contains(id))
            .collect(Collectors.toSet());

        System.out.println("Delete (" + toDeleteIds.size() + "): " + toDeleteIds);
        if (!toDeleteIds.isEmpty()) {
            try {
                System.out.println(client.delete(new ArrayList<>(toDeleteIds)));
            } catch (Exception e) {
                System.err.println("Failed to add delete data: " + toDeleteIds);
                e.printStackTrace();
            }
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
            try {
                System.out.println(client.update(id, localData.get(id).getIdLessColumnData()));
            } catch (Exception e) {
                System.err.println("Failed to update maptive data: " + id);
                e.printStackTrace();
            }
        }
    }
}
