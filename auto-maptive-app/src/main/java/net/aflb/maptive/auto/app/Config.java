package net.aflb.maptive.auto.app;

import io.smallrye.config.ConfigMapping;

@ConfigMapping
interface Config {

    String key();

    String map();

    String file();
}
