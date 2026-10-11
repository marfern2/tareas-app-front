# Donit Android v1.0.0

Donit es una aplicación de gestión de tareas formada por una API Spring Boot, un panel de administración Angular y esta aplicación Android. Este repositorio es el cliente de usuario: registro, login, persistencia opcional de sesión, refresh, logout, perfil y gestión de tareas y tipos de tarea (crear, editar, completar, reabrir y eliminar).

## Stack y arquitectura

Kotlin, Jetpack Compose, Material 3, Retrofit y OkHttp. La app consume la API Donit de su flavor; la sesión se guarda localmente mediante Android Keystore y cifrado AES-GCM. La autenticación de administrador pertenece al panel web y al backend.

## Entornos y variantes

| Entorno | Nombre | applicationId | API | Panel Admin del entorno |
|---|---|---|---|---|
| DEV | Donit Dev | `dam.moviles.tareas_app_front.dev` | `https://donit-api-dev.marfern.dev/` | `https://admin-dev.marfern.dev` |
| PROD | Donit | `dam.moviles.tareas_app_front` | `https://donit-api.marfern.dev/` | `https://admin-donit.marfern.dev` |

Las variantes son `devDebug`, `devRelease`, `prodDebug` y `prodRelease`. DEV y PROD tienen IDs distintos y se pueden instalar simultáneamente. LOCAL es una API local usada por un build DEV; no existe un tercer flavor. La URL se fija al compilar cada variante. Más detalles en [docs/ENTORNOS.md](docs/ENTORNOS.md).

La release PROD usa `versionCode = 1`, `versionName = "1.0.0"` y la API `https://donit-api.marfern.dev/`. DEV conserva el sufijo `-dev` en `versionName`.

## Ejecución local

Se necesita Android SDK y el JDK requerido por Gradle. Para compilar e instalar DEV debug contra la API DEV:

```bash
./gradlew assembleDevDebug
```

Para compilar ambas variantes debug (firmadas con la clave debug habitual de Android):

```bash
./gradlew assembleDebug
```

Para usar una API local desde el emulador:

```bash
./gradlew assembleDevDebug -PdonitDevApiUrl=http://10.0.2.2:8080/
```

El override debe terminar en `/`, solo afecta a DEV y no cambia PROD.

## Firma local de release

Configura estas cuatro propiedades en `~/.gradle/gradle.properties` (fuera del repositorio), con la ruta absoluta a un keystore existente y sus credenciales privadas:

| Propiedad Gradle | Variable de entorno equivalente |
|---|---|
| `donitReleaseStoreFile` | `DONIT_RELEASE_STORE_FILE` |
| `donitReleaseStorePassword` | `DONIT_RELEASE_STORE_PASSWORD` |
| `donitReleaseKeyAlias` | `DONIT_RELEASE_KEY_ALIAS` |
| `donitReleaseKeyPassword` | `DONIT_RELEASE_KEY_PASSWORD` |

Las propiedades tienen prioridad sobre las variables de entorno. El keystore y las contraseñas permanecen fuera del repositorio; no se versionan ni se muestran en el comando. No se genera un keystore automáticamente. Con las cuatro credenciales configuradas:

```bash
./gradlew assembleProdRelease
apksigner verify --verbose --print-certs app/build/outputs/apk/prod/release/app-prod-release.apk
```

Usa el `apksigner` del Android SDK en `PATH`. Comprueba que la verificación termine correctamente y que el certificado mostrado sea el esperado antes de distribuir el APK. Si falta una credencial o el keystore no existe, `assembleProdRelease` falla con un mensaje claro; nunca firma release con la clave debug. Las tareas de tests y lint no necesitan credenciales.

## Tests

```bash
./gradlew test lint lintProdRelease
```

## CI y artefactos

En PR, CI prueba, ejecuta lint y compila DEV/PROD debug. Tras integrar `feature/*` en `develop`, `package-development` genera un APK DEV debug en GitHub Environment `development`, con concurrencia cancelable. `package-production` genera un APK PROD firmado al promover a `master` solo si el GitHub Environment `production` tiene los secretos `ANDROID_KEYSTORE_BASE64`, `ANDROID_STORE_PASSWORD`, `ANDROID_KEY_ALIAS` y `ANDROID_KEY_PASSWORD`; reconstruye el keystore en el directorio temporal del runner. Sin ellos, falla. Este repositorio no publica en Play Store.

## Seguridad

Access y refresh tokens se cifran con AES-GCM usando una clave de Android Keystore. La migración desde el almacenamiento legacy y el logout limpian los datos de sesión anteriores. Los ficheros con tokens y las preferencias locales del perfil (nombre, correo y URI de foto) están excluidos de backup y device transfer; el logging no incluye tokens ni la cabecera `Authorization`. El nombre de usuario se valida entre 3 y 20 caracteres.

## Estado

Smoke de Android DEV y PROD superado. En DEV se validaron refresh real y almacenamiento cifrado; también se comprobó el aislamiento DEV/PROD.
