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
3. Rellena `sdk.dir`, `supabase.url` y `supabase.anon.key` (clave **anon public** del dashboard).
4. Aplica migraciones y seed según `docs/supabase/SCHEMA.md`.
5. Sincroniza Gradle y ejecuta en emulador o dispositivo.

## Estructura del proyecto

```text
app/src/main/java/com/example/livent/
├── data/          # Repositorios, DTOs, fuentes remotas
├── domain/        # Modelos, contratos, casos de uso
├── presentation/  # UI Compose, ViewModels, navegación
└── di/            # Módulos Hilt

supabase/migrations/   # Esquema SQL y RLS
docs/                  # Documentación técnica
```

## Documentación

| Documento | Contenido |
|-----------|-----------|
| [docs/supabase/SCHEMA.md](docs/supabase/SCHEMA.md) | Esquema, RLS, seed |
| [docs/supabase/STORAGE_SETUP.md](docs/supabase/STORAGE_SETUP.md) | Bucket `posters` |
| [docs/fase3/AUTH_FLOW.md](docs/fase3/AUTH_FLOW.md) | Autenticación y sesión |
| [plan2.md](plan2.md) | Arquitectura y fases del producto |

## Estado del desarrollo

- [x] Fase 1 — Infraestructura Android
- [x] Fase 2 — Base de datos Supabase
- [x] Fase 3 — Autenticación y modo invitado
- [ ] Fase 4 — Flujo Publisher (CRUD eventos)
- [ ] Fase 5 — Feed y favoritos
- [ ] Fase 6 — Monetización (Stripe Test)
- [ ] Fase 7 — UI final (mockups de referencia)

## Licencia

Proyecto final DAM2 (TFG).
