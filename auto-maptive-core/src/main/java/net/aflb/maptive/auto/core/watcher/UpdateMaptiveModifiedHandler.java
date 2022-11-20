package net.aflb.maptive.auto.core.watcher;

import net.aflb.maptive.auto.core.MaptiveData;
import net.aflb.maptive.auto.core.MaptiveId;
import net.aflb.maptive.auto.core.client.MaptiveClient;
import net.aflb.maptive.auto.core.io.MaptiveDataDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateMaptiveModifiedHandler implements MaptiveModifiedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateMaptiveModifiedHandler.class);

    private final int idColumnIndex;

    public UpdateMaptiveModifiedHandler(int idColumnIndex) {
        this.idColumnIndex = idColumnIndex;
    }

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

        LOGGER.info("Add ({}): {}", toAddIds.size(), toAddIds);
        if (!toAddIds.isEmpty()) {
            try {
                LOGGER.debug("{}", client.addAll(toAdd));
            } catch (Exception e) {
                LOGGER.error("Failed to add maptive data: {}", toAddIds, e);
            }
        }

        final var toDeleteIds = serverIds.stream()
            .filter(id -> !localIds.contains(id))
            .collect(Collectors.toSet());

        LOGGER.info("Delete ({}): {}", toDeleteIds.size(), toDeleteIds);
        if (!toDeleteIds.isEmpty()) {
            try {
                LOGGER.debug("{}", client.delete(new ArrayList<>(toDeleteIds)));
            } catch (Exception e) {
                LOGGER.error("Failed to delete maptive data: {}", toDeleteIds, e);
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
            .filter(e -> {
                final var localId = e.getKey().id();
                final var match = serverData.stream()
                    .filter(sd -> {
                        if (sd.size() <= idColumnIndex) {
                            return false;
                        }
                        return sd.get(idColumnIndex).equals(localId);
                    }).toList();

                if (match.isEmpty()) {
                    LOGGER.warn("Discarding possible update for {} which has no corresponding server data", localId);
                    return false;
                }

                if (match.size() > 1) {
                    LOGGER.warn("Multiple matching server entries for {} found (DELETE DUPLICATE IDs!) - using first only", localId);
                    for (var i = 1; i < match.size(); i++) {
                        LOGGER.debug("Discarding duplicate entry for {}: {}", localId, match.get(i));
                    }
                }

                final var localValue = e.getValue();
                final var serverValue = match.get(0);
                final var different = !localValue.equals(serverValue);
                if (different) {
                    LOGGER.debug("local {} server {}", localValue.get(1).getClass().getName(),serverValue.get(1).getClass().getName());
                    LOGGER.debug("Changes detected for {}", localId);
                    LOGGER.debug("{} server: {}", localId, serverValue);
                    LOGGER.debug("{} local : {}", localId, localValue);
                }

                return different;
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        LOGGER.info("Update ({}): {}", toUpdateIds.size(), toUpdateIds.stream().map(MaptiveId::id).toList());

        for (final var id : toUpdateIds) {
            try {
                LOGGER.debug("{}", client.update(id, localData.get(id).getIdLessColumnData()));
            } catch (Exception e) {
                LOGGER.error("Failed to update maptive data: {}", id, e);
            }
        }
    }
}
