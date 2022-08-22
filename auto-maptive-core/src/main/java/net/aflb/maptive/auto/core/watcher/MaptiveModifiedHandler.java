package net.aflb.maptive.auto.core.watcher;

import net.aflb.maptive.auto.core.MaptiveData;
import net.aflb.maptive.auto.core.MaptiveId;
import net.aflb.maptive.auto.core.client.MaptiveClient;
import net.aflb.maptive.auto.core.io.MaptiveDataDao;

import java.util.List;
import java.util.Map;

public interface MaptiveModifiedHandler {
    void onUpdate(List<List<String>> serverData, Map<MaptiveId, MaptiveData> localData, MaptiveDataDao dao, MaptiveClient client);

    default MaptiveModifiedHandler andThen(MaptiveModifiedHandler next) {
        return (serverData, localData, dao, clent) -> {
            this.onUpdate(serverData, localData, dao, clent);
            next.onUpdate(serverData, localData, dao, clent);
        };
    }
}
