# Entornos Android Donit

| Variant | Nombre | applicationId | API |
|---|---|---|---|
| `devDebug` / `devRelease` | Donit Dev | `dam.moviles.tareas_app_front.dev` | `https://donit-api-dev.marfern.dev/` |
| `prodDebug` / `prodRelease` | Donit | `dam.moviles.tareas_app_front` | `https://donit-api.marfern.dev/` |

Las dos aplicaciones se instalan simultáneamente y cada una compila su URL en `BuildConfig.API_BASE_URL`. No existe selección de entorno en runtime.

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

El valor debe terminar en `/`. Cleartext se habilita únicamente para ese build DEV cuando la URL empieza por `http://`; PROD siempre lo bloquea.

## CI y signing

Los PR compilan, prueban y ejecutan lint para DEV y PROD. `develop` adjunta un APK DEV debug. `master` genera un APK PROD release sin firma y no publica en Google Play.

Para signing futuro, almacenar el keystore como Base64 y sus credenciales en GitHub Secrets (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`, `ANDROID_STORE_PASSWORD`), reconstruirlo en `$RUNNER_TEMP` y nunca versionarlo.
