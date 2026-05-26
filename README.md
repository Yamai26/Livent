# Livent

Aplicación Android para descubrimiento y gestión de eventos locales. Proyecto de TFG.

## Stack

- **UI:** Jetpack Compose, Material 3
- **Arquitectura:** MVVM, Clean Architecture, UDF
- **DI:** Dagger Hilt (KSP)
- **Backend:** Supabase (Auth, PostgreSQL, Storage)
- **Navegación:** Navigation Compose

## Requisitos

- Android Studio (reciente, compatible con AGP 9.x)
- JDK 11+
- Cuenta y proyecto en [Supabase](https://supabase.com)

## Configuración

1. Clona el repositorio.
2. Copia `local.properties.example` a `local.properties` (no se versiona).
3. Rellena `sdk.dir`, `supabase.url`, `supabase.anon.key` (clave **anon public**) y, para Fase 6, las claves Stripe Test en `local.properties` (ver `local.properties.example`).
4. Configura el proyecto Supabase (migraciones, seed y Edge Functions) en tu instancia; el esquema y la documentación detallada se mantienen en local y no forman parte de este repositorio.
5. Sincroniza Gradle y ejecuta en emulador o dispositivo.

## Estructura del proyecto (repositorio público)

```text
app/src/main/java/com/example/livent/
├── data/          # Repositorios, DTOs, fuentes remotas
├── domain/        # Modelos, contratos, casos de uso
├── presentation/  # UI Compose, ViewModels, navegación
└── di/            # Módulos Hilt
```

## Estado del desarrollo

- [x] Fase 1 — Infraestructura Android
- [x] Fase 2 — Base de datos Supabase
- [x] Fase 3 — Autenticación y modo invitado
- [x] Fase 4 — Flujo Publisher (CRUD eventos)
- [x] Fase 5 — Feed y favoritos
- [x] Fase 6 — Monetización (Stripe Test)
- [x] Fase 7 — UI final (mockups de referencia)

## Licencia

Proyecto final DAM2 (TFG).
