# Integration guide

This article describes how to integrate the library in your project.

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

```groovy
implementation 'com.github.mapsforge.vtm:vtm:[CURRENT-VERSION]'
implementation 'com.github.mapsforge.vtm:vtm-themes:[CURRENT-VERSION]'
implementation 'org.slf4j:slf4j-api:1.7.28'
```

### Android

```groovy
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-armeabi-v7a'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-arm64-v8a'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-x86'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-x86_64'
implementation 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]'
implementation 'com.caverock:androidsvg:1.4'
```

### Android (libGDX)

```groovy
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-armeabi-v7a'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-arm64-v8a'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-x86'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-x86_64'
implementation 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]'
implementation 'com.github.mapsforge.vtm:vtm-gdx:[CURRENT-VERSION]'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android-gdx:[CURRENT-VERSION]:natives-armeabi-v7a'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android-gdx:[CURRENT-VERSION]:natives-arm64-v8a'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android-gdx:[CURRENT-VERSION]:natives-x86'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android-gdx:[CURRENT-VERSION]:natives-x86_64'
implementation 'com.github.mapsforge.vtm:vtm-android-gdx:[CURRENT-VERSION]'
implementation 'com.badlogicgames.gdx:gdx:1.11.0'
implementation 'com.badlogicgames.gdx:gdx-backend-android:1.11.0'
implementation 'com.caverock:androidsvg:1.4'
```

### iOS

Detailed iOS instructions can be found [here](ios.md).

### Desktop

```groovy
implementation 'com.github.mapsforge.vtm:vtm-gdx:[CURRENT-VERSION]'
implementation 'com.github.mapsforge.vtm:vtm-desktop:[CURRENT-VERSION]'
runtimeOnly 'com.github.mapsforge.vtm:vtm-desktop:[CURRENT-VERSION]:natives-linux'
runtimeOnly 'com.github.mapsforge.vtm:vtm-desktop:[CURRENT-VERSION]:natives-osx'
runtimeOnly 'com.github.mapsforge.vtm:vtm-desktop:[CURRENT-VERSION]:natives-windows'
implementation 'com.badlogicgames.gdx:gdx:1.11.0'
runtimeOnly 'com.badlogicgames.gdx:gdx-platform:1.11.0:natives-desktop'
implementation 'guru.nidi.com.kitfox:svgSalamander:1.1.3'
implementation 'net.sf.kxml:kxml2:2.3.0'
```

#### LWJGL 2

```groovy
implementation 'com.github.mapsforge.vtm:vtm-desktop-lwjgl:[CURRENT-VERSION]'
implementation 'com.badlogicgames.gdx:gdx-backend-lwjgl:1.11.0'
implementation 'org.lwjgl.lwjgl:lwjgl:2.9.3'
runtimeOnly 'org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-linux'
runtimeOnly 'org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-osx'
runtimeOnly 'org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-windows'
```

#### LWJGL 3

```groovy
implementation 'com.github.mapsforge.vtm:vtm-desktop-lwjgl3:[CURRENT-VERSION]'
implementation 'com.badlogicgames.gdx:gdx-backend-lwjgl3:1.11.0'
implementation 'org.lwjgl:lwjgl:3.3.1'
runtimeOnly 'org.lwjgl:lwjgl:3.3.1:natives-linux'
runtimeOnly 'org.lwjgl:lwjgl:3.3.1:natives-macos'
runtimeOnly 'org.lwjgl:lwjgl:3.3.1:natives-windows'
```

### JTS overlays

```groovy
implementation 'com.github.mapsforge.vtm:vtm-jts:[CURRENT-VERSION]'
// https://github.com/locationtech/jts/issues/145
implementation 'org.locationtech.jts:jts-core:1.15.1'
```

### Online tiles

```groovy
implementation 'com.github.mapsforge.vtm:vtm-http:[CURRENT-VERSION]'
// https://github.com/square/okhttp/issues/4481
implementation 'com.squareup.okhttp3:okhttp:3.12.13'
implementation 'com.squareup.okio:okio:1.15.0'
```

### MBTiles

```groovy
implementation 'com.github.mapsforge.vtm:vtm-android-mvt:[CURRENT-VERSION]'
implementation 'com.github.mapsforge.vtm:vtm-mvt:[CURRENT-VERSION]'
implementation 'com.google.protobuf:protobuf-java:3.6.1'
implementation 'com.wdtinc:mapbox-vector-tile:3.1.0'
// https://github.com/locationtech/jts/issues/145
implementation 'org.locationtech.jts:jts-core:1.15.1'
```

### Mapbox vector tiles

```groovy
implementation 'com.github.mapsforge.vtm:vtm-mvt:[CURRENT-VERSION]'
implementation 'com.google.protobuf:protobuf-java:3.6.1'
implementation 'com.wdtinc:mapbox-vector-tile:3.1.0'
// https://github.com/locationtech/jts/issues/145
implementation 'org.locationtech.jts:jts-core:1.15.1'
```

### GeoJSON vector tiles

```groovy
implementation 'com.github.mapsforge.vtm:vtm-json:[CURRENT-VERSION]'
implementation 'com.fasterxml.jackson.core:jackson-annotations:2.9.9'
implementation 'com.fasterxml.jackson.core:jackson-core:2.9.9'
implementation 'com.fasterxml.jackson.core:jackson-databind:2.9.9'
```

### jeo (indoor maps)

```groovy
implementation 'com.github.mapsforge.vtm:vtm-jeo:[CURRENT-VERSION]'
implementation('com.github.jeo.jeo:jeo-carto:master-SNAPSHOT') {
    exclude group: 'org.slf4j', module: 'slf4j-jdk14'
}
```
