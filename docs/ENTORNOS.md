# Entornos Android Donit

| Variant | Nombre | applicationId | API |
|---|---|---|---|
| `devDebug` / `devRelease` | Donit Dev | `dam.moviles.tareas_app_front.dev` | `https://donit-api-dev.marfern.dev/` |
| `prodDebug` / `prodRelease` | Donit | `dam.moviles.tareas_app_front` | `https://donit-api.marfern.dev/` |

Los APK debug de DEV y PROD pueden instalarse simultáneamente. Cada variante compila su URL en `BuildConfig.API_BASE_URL`; no existe selección de entorno en runtime. El empaquetado release exige firma configurada y falla si faltan credenciales.

## Builds

```bash
./gradlew assembleDevDebug
./gradlew assembleProdDebug
./gradlew testDevDebugUnitTest testProdDebugUnitTest lintDevDebug lintProdDebug
```

## Probar DEV contra backend local

No se mantiene un tercer flavor. Se sobrescribe solo la URL de la variante DEV en el comando de build:

```bash
# Emulador Android
./gradlew assembleDevDebug -PdonitDevApiUrl=http://10.0.2.2:8080/

# Dispositivo físico (ejemplo; usar la IP LAN real del ordenador)
./gradlew assembleDevDebug -PdonitDevApiUrl=http://192.168.1.50:8080/
```

El valor debe terminar en `/`. Cuando la URL empieza por `http://`, cleartext se habilita para todo ese APK DEV; PROD siempre lo bloquea.

## CI y signing

Los PR compilan, prueban y ejecutan lint para DEV y PROD. `develop` adjunta un APK DEV debug. `master` genera un APK PROD release firmado si el entorno `production` tiene los secretos de firma; no publica en Google Play.

El empaquetado de `develop` usa GitHub Environment `development` y el grupo de concurrencia `android-development`, cancelable. El de `master` usa `production` y `android-production`: no cancela ejecuciones en curso y encola hasta 100 pendientes. Los PR solo ejecutan CI y no registran deployments.

El keystore Base64 y sus credenciales se guardan en los secretos de `production` (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`, `ANDROID_STORE_PASSWORD`). El workflow lo reconstruye en `$RUNNER_TEMP`. Para firma local y verificación con `apksigner`, consulta el [README](../README.md#firma-local-de-release). Nunca se versionan el keystore ni las contraseñas.
