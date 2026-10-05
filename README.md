# Donit — Android

Donit es una aplicación de gestión de tareas formada por una API Spring Boot, un panel de administración Angular y esta aplicación Android. Este repositorio es el cliente de usuario: registro, login, persistencia opcional de sesión, refresh, logout, perfil y gestión de tareas y tipos de tarea (crear, editar, completar, reabrir y eliminar).

## Stack y arquitectura

Kotlin, Jetpack Compose, Material 3, Retrofit y OkHttp. La app consume la API Donit de su flavor; la sesión se guarda localmente mediante Android Keystore y cifrado AES-GCM. La autenticación de administrador pertenece al panel web y al backend.

## Entornos y variantes

| Entorno | Nombre | applicationId | API | Panel Admin del entorno |
|---|---|---|---|---|
| DEV | Donit Dev | `dam.moviles.tareas_app_front.dev` | `https://donit-api-dev.marfern.dev/` | `https://admin-dev.marfern.dev` |
| PROD | Donit | `dam.moviles.tareas_app_front` | `https://donit-api.marfern.dev/` | `https://admin-donit.marfern.dev` |

Las variantes son `devDebug`, `devRelease`, `prodDebug` y `prodRelease`. DEV y PROD tienen IDs distintos y se pueden instalar simultáneamente. LOCAL es una API local usada por un build DEV; no existe un tercer flavor. La URL se fija al compilar cada variante. Más detalles en [docs/ENTORNOS.md](docs/ENTORNOS.md).

## Ejecución local

Se necesita Android SDK y el JDK requerido por Gradle. Para compilar e instalar DEV debug contra la API DEV:

```bash
./gradlew assembleDevDebug
```

Para usar una API local desde el emulador:

```bash
./gradlew assembleDevDebug -PdonitDevApiUrl=http://10.0.2.2:8080/
```

El override debe terminar en `/`, solo afecta a DEV y no cambia PROD.

## Tests

```bash
./gradlew testDevDebugUnitTest testProdDebugUnitTest lintDevDebug lintProdDebug
```

## CI y artefactos

En PR, CI prueba, ejecuta lint y compila DEV/PROD. Tras integrar `feature/*` en `develop`, `package-development` genera un APK DEV debug en GitHub Environment `development`, con concurrencia cancelable. Al promover `develop` a `master`, `package-production` genera un APK PROD release **sin firma** en `production`, con concurrencia no cancelable. Los artefactos se adjuntan al workflow; no hay publicación en Play Store ni signing release configurado.

## Seguridad

Access y refresh tokens se cifran con AES-GCM usando una clave de Android Keystore. La migración desde el almacenamiento legacy y el logout limpian los datos de sesión anteriores. Los ficheros con tokens están excluidos de backup y device transfer; el logging no incluye tokens ni la cabecera `Authorization`. El nombre de usuario se valida entre 3 y 20 caracteres.

## Estado

Smoke de Android DEV y PROD superado. En DEV se validaron refresh real y almacenamiento cifrado; también se comprobó el aislamiento DEV/PROD.
