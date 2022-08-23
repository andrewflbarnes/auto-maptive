# auto-maptive
Automatic xlsx uploads to maptive API

## Build

```bash
./gradlew build
```

## Native image build (Graal)

```bash
native-image \
  --no-fallback \
  -H:+AddAllCharsets \
  --no-server \
  -H:DynamicProxyConfigurationResources=src/main/native/dynamic-proxy.json \
  -jar auto-maptive-simple-app/build/libs/auto-maptive-simple-app-*.jar
```

## Run

Create an `application.properties` file in the running dirrectory with the below settings
```properties
# The API key provided by Maptive
key=123456789
# The ID of the map to update in maptive (taken from the URL)
map=123
# The full filepath to the xlsx file which will be watched for updates
file=/path/to/MyFile.xlsx
# How often, in seconds, to check the xlsx file for updates
schedule=5
```

Then run the native image or jar as normal e.g.
```bash
java -jar app.jar
# or
./app
```
