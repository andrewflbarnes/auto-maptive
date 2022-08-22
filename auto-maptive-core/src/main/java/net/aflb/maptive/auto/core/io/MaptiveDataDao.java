package net.aflb.maptive.auto.core.io;

import net.aflb.maptive.auto.core.MaptiveData;
import net.aflb.maptive.auto.core.MaptiveId;

import java.util.Map;

public interface MaptiveDataDao {

    Map<MaptiveId, MaptiveData> getAll();

    long lastModified();
}
