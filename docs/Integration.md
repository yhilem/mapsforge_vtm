[![](https://jitpack.io/v/mapsforge/vtm.svg)](https://jitpack.io/#mapsforge/vtm)

# Integration guide

This article describes how to integrate the library in your project with [JitPack](https://jitpack.io/#mapsforge/vtm).

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

## Map

```groovy
implementation 'com.github.mapsforge.vtm:vtm:[CURRENT-VERSION]@jar'
implementation 'com.github.mapsforge.vtm:vtm-themes:[CURRENT-VERSION]@jar'
```

## Android

```groovy
implementation 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]@jar'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-armeabi-v7a@jar'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-arm64-v8a@jar'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-x86@jar'
runtimeOnly 'com.github.mapsforge.vtm:vtm-android:[CURRENT-VERSION]:natives-x86_64@jar'
implementation 'com.caverock:androidsvg:1.4'
```

## Desktop

```groovy
implementation 'com.github.mapsforge.vtm:vtm-gdx:[CURRENT-VERSION]@jar'
implementation 'com.github.mapsforge.vtm:vtm-desktop:[CURRENT-VERSION]@jar'
runtimeOnly 'com.github.mapsforge.vtm:vtm-desktop:[CURRENT-VERSION]:natives-linux@jar'
runtimeOnly 'com.github.mapsforge.vtm:vtm-desktop:[CURRENT-VERSION]:natives-osx@jar'
runtimeOnly 'com.github.mapsforge.vtm:vtm-desktop:[CURRENT-VERSION]:natives-windows@jar'
implementation 'com.badlogicgames.gdx:gdx:1.11.0'
runtimeOnly 'com.badlogicgames.gdx:gdx-platform:1.11.0:natives-desktop'
implementation 'guru.nidi.com.kitfox:svgSalamander:1.1.3'
implementation 'net.sf.kxml:kxml2:2.3.0'
```

### LWJGL 2

```groovy
implementation 'com.github.mapsforge.vtm:vtm-desktop-lwjgl:[CURRENT-VERSION]@jar'
implementation 'com.badlogicgames.gdx:gdx-backend-lwjgl:1.11.0'
implementation 'org.lwjgl.lwjgl:lwjgl:2.9.3'
runtimeOnly 'org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-linux'
runtimeOnly 'org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-osx'
runtimeOnly 'org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-windows'
```

### LWJGL 3

```groovy
implementation 'com.github.mapsforge.vtm:vtm-desktop-lwjgl3:[CURRENT-VERSION]@jar'
implementation 'com.badlogicgames.gdx:gdx-backend-lwjgl3:1.11.0'
implementation 'org.lwjgl:lwjgl:3.3.1'
runtimeOnly 'org.lwjgl:lwjgl:3.3.1:natives-linux'
runtimeOnly 'org.lwjgl:lwjgl:3.3.1:natives-macos'
runtimeOnly 'org.lwjgl:lwjgl:3.3.1:natives-windows'
```

## Features

### Hillshading

```groovy
implementation 'com.github.mapsforge.vtm:vtm-hillshading:[CURRENT-VERSION]@jar'
implementation 'com.github.mapsforge.mapsforge:mapsforge-core:[CURRENT-VERSION]@jar'
implementation 'com.github.mapsforge.mapsforge:mapsforge-map:[CURRENT-VERSION]@jar'
implementation 'com.github.mapsforge.mapsforge:mapsforge-map-android:[CURRENT-VERSION]@jar'
```

### Overlays

```groovy
implementation 'com.github.mapsforge.vtm:vtm-jts:[CURRENT-VERSION]@jar'
implementation 'org.locationtech.jts:jts-core:1.20.0'
```

### Online tiles

```groovy
implementation 'com.github.mapsforge.vtm:vtm-http:[CURRENT-VERSION]@jar'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.squareup.okio:okio:3.6.0'
implementation 'com.squareup.okio:okio-jvm:3.6.0'
```

### MBTiles

```groovy
implementation 'com.github.mapsforge.vtm:vtm-mvt:[CURRENT-VERSION]@jar'
implementation 'com.github.mapsforge.vtm:vtm-android-mvt:[CURRENT-VERSION]@jar'
implementation 'com.google.protobuf:protobuf-java:3.24.2'
implementation 'io.github.ci-cmg:mapbox-vector-tile:4.0.6'
implementation 'org.locationtech.jts:jts-core:1.20.0'
```

### Mapbox vector tiles

```groovy
implementation 'com.github.mapsforge.vtm:vtm-mvt:[CURRENT-VERSION]@jar'
implementation 'com.google.protobuf:protobuf-java:3.24.2'
implementation 'io.github.ci-cmg:mapbox-vector-tile:4.0.6'
implementation 'org.locationtech.jts:jts-core:1.20.0'
```

### GeoJSON vector tiles

```groovy
implementation 'com.github.mapsforge.vtm:vtm-json:[CURRENT-VERSION]@jar'
implementation 'com.fasterxml.jackson.core:jackson-annotations:2.9.9'
implementation 'com.fasterxml.jackson.core:jackson-core:2.9.9'
implementation 'com.fasterxml.jackson.core:jackson-databind:2.9.9'
```

## Snapshots

See the instructions on  [JitPack](https://jitpack.io/#mapsforge/vtm).
