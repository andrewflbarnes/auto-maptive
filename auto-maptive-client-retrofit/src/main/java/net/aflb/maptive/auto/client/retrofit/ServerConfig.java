package net.aflb.maptive.auto.client.retrofit;

class ServerConfig {

    public static final String DEFAULT_PROTO = "https";
    public static final String PRODUCTION_HOST = "fortress.maptive.com";
    public static final String DEFAULT_BASE_PATH = "ver4/classes/api/v1.0/";

    private final String proto;
    private final String host;
    private final String path;

    static ServerConfig production() {
        return forHost(PRODUCTION_HOST);
    }

    static ServerConfig forHost(String host) {
        return new ServerConfig(DEFAULT_PROTO, host, DEFAULT_BASE_PATH);
    }

    ServerConfig(String proto, String host, String path) {
        this.proto = proto;
        this.host = host;
        this.path = path;
    }

    String baseUrl() {
        return "%s://%s/%s".formatted(proto, host, path);
    }
}
