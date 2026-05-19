# Livent — Esquema Supabase (Fase 2)

Base de datos PostgreSQL en Supabase con **RLS activo**. Sin políticas explícitas, todo queda denegado.

## Diagrama ER

```mermaid
erDiagram
    auth_users ||--|| profiles : "1:1"
    profiles ||--o{ events : "publica"
    profiles ||--o{ favorites : "guarda"
    events ||--o{ favorites : "en"

    auth_users {
        uuid id PK
    }

    profiles {
        uuid id PK FK
        user_role role
        text display_name
        text email
        text avatar_url
        subscription_tier subscription_tier
        timestamptz created_at
        timestamptz updated_at
    }

    events {
        uuid id PK
        uuid publisher_id FK
        text title
        text artist
        text description
        text location
        timestamptz starts_at
        text poster_url
        event_status status
        boolean is_featured
        timestamptz created_at
        timestamptz updated_at
    }

    favorites {
        uuid user_id PK FK
        uuid event_id PK FK
        timestamptz created_at
    }
```

## Enums

| Tipo PostgreSQL | Valores |
|-----------------|---------|
| `user_role` | `user`, `publisher` |
| `subscription_tier` | `free`, `premium` |
| `event_status` | `active`, `past`, `draft` |

## Reglas de negocio

| Regla | Dónde |
|-------|--------|
| Invitado = sin sesión; lee eventos vía rol PostgREST `anon` | RLS `events_select_active_anon` |
| Solo eventos `status = active` visibles en exploración | Políticas SELECT en `events` |
| Publisher CRUD solo sus filas (`publisher_id = auth.uid()`) | Políticas INSERT/UPDATE/DELETE |
| Plan **free**: máx. **1** evento `active` por publisher | Trigger `events_enforce_free_active_limit` + validación app Fase 4 |
| Registro Auth → fila `profiles` | Trigger `on_auth_user_created` → `handle_new_user()` |
| Fase 3 signup: pasar `role` en `raw_user_meta_data` (`user` \| `publisher`) | Función `handle_new_user` |

## Políticas RLS (resumen)

| Tabla | `anon` | `authenticated` |
|-------|--------|-----------------|
| **events** | `SELECT` si `status = active` | `SELECT` activos + **propios** (cualquier status); `INSERT/UPDATE/DELETE` si `publisher_id = auth.uid()` |
| **favorites** | — | `SELECT/INSERT/DELETE` si `user_id = auth.uid()` |
| **profiles** | — | `SELECT/UPDATE/INSERT` solo `id = auth.uid()` |

## Storage: bucket `posters`

| Operación | Quién | Condición |
|-----------|-------|-----------|
| `SELECT` | `public` | `bucket_id = posters` |
| `INSERT/UPDATE/DELETE` | `authenticated` | Ruta `posters/{auth.uid}/...` |

URLs públicas: `{SUPABASE_URL}/storage/v1/object/public/posters/{path}` (Fase 4).

> **Importante:** En el SQL Editor del dashboard, `CREATE POLICY` sobre `storage.objects` suele fallar con `42501: must be owner of relation objects`. Crea el bucket con SQL o UI; las políticas, en **Storage → Policies**. Guía: [`STORAGE_SETUP.md`](STORAGE_SETUP.md).

---

## Aplicar en el dashboard Supabase

Orden recomendado (SQL Editor, pegar cada archivo completo):

1. `supabase/migrations/20260519100000_initial_schema.sql`
2. `supabase/migrations/20260519100100_rls_policies.sql`
3. `supabase/migrations/20260519100200_storage_posters.sql` (solo bucket)
4. Políticas Storage en dashboard → [`STORAGE_SETUP.md`](STORAGE_SETUP.md)
5. Crear usuarios de prueba en **Authentication → Users** (ver seed).
6. `supabase/seed.sql`

Alternativa CLI: `supabase link` + `supabase db push` (si usas Supabase CLI local).

---

## Seed (datos de prueba)

1. Crear en Auth (email + contraseña):
   - `publisher1@test.livent`
   - `publisher2@test.livent`
   - `user1@test.livent`
2. Ejecutar `supabase/seed.sql`.
3. Comprobar en **Table Editor**: `profiles`, `events`, `favorites`.

---

## Verificación (PostgREST / curl)

Sustituye `PROJECT_REF` y `ANON_KEY` (Settings → API → **anon public**, nunca `service_role`).

### Invitado: leer eventos activos (debe funcionar)

```bash
curl -s "https://PROJECT_REF.supabase.co/rest/v1/events?select=id,title,status&status=eq.active" \
  -H "apikey: ANON_KEY" \
  -H "Authorization: Bearer ANON_KEY"
```

### Invitado: favorites (debe fallar / vacío denegado)

```bash
curl -s -o /dev/null -w "%{http_code}" "https://PROJECT_REF.supabase.co/rest/v1/favorites?select=*" \
  -H "apikey: ANON_KEY" \
  -H "Authorization: Bearer ANON_KEY"
```

Esperado: `401` o `403` (sin filas accesibles).

### Autenticado publisher: solo edita sus eventos

Tras login (Fase 3), usar JWT de sesión en `Authorization: Bearer <access_token>`.

- `PATCH /rest/v1/events?id=eq.<id_propio>` → OK  
- `PATCH` sobre evento de otro publisher → 0 filas / error RLS  

### SQL Editor (como postgres, solo desarrollo)

```sql
SET ROLE anon;
SELECT count(*) FROM public.events WHERE status = 'active';
RESET ROLE;
```

---

## Android (Fase 2 opcional)

En `local.properties` (no commitear):

```properties
supabase.url=https://PROJECT_REF.supabase.co
supabase.anon.key=TU_ANON_KEY
```

Plantilla: `local.properties.example`.

Módulo Hilt: `di/SupabaseModule.kt` — cliente vacío si faltan claves; compila `assembleDebug` sin configurar.

DTOs: `data/remote/dto/` alineados con columnas snake_case.

---

## Notas para fases siguientes

| Fase | Uso de este esquema |
|------|---------------------|
| **3 Auth** | `signUp` con `data = buildJsonObject { put("role", "publisher") }`; trigger crea `profiles`; leer rol con `profiles` + JWT |
| **4 Publisher CRUD** | PostgREST sobre `events`; Storage `posters/{uid}/`; respetar trigger plan free |
| **5 Usuario** | `favorites` con sesión; feed igual que anon pero con favoritos |
| **6 Stripe** | Actualizar `subscription_tier` y `is_featured` |
