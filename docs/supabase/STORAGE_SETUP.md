# Storage `posters` — configuración en Dashboard

## Por qué falló el SQL original

El error:

```text
ERROR: 42501: must be owner of relation objects
```

significa que tu sesión del **SQL Editor** no es propietaria de la tabla `storage.objects`. En Supabase esa tabla la gestiona el rol interno `supabase_storage_admin`. Por eso:

| Operación | SQL Editor dashboard |
|-----------|----------------------|
| `INSERT` en `storage.buckets` | Suele funcionar |
| `CREATE POLICY` en `storage.objects` | Suele fallar |
| `COMMENT ON POLICY` | Suele fallar |

Las migraciones de tablas `public.*` (initial + rls) sí funcionan porque eres dueño de ese esquema.

---

## Paso A — Bucket (SQL o UI)

### Opción 1: SQL (solo el INSERT)

Ejecuta de nuevo el archivo actualizado:

`supabase/migrations/20260519100200_storage_posters.sql`

(solo crea el bucket, sin políticas).

### Opción 2: UI

**Storage → New bucket**

| Campo | Valor |
|-------|--------|
| Name | `posters` |
| Public bucket | **On** |
| File size limit | 5 MB (opcional) |
| Allowed MIME types | `image/jpeg`, `image/png`, `image/webp` |

---

## Paso B — Políticas (solo Dashboard)

**Storage → posters → Policies → New policy**

Puedes usar **“For full customization”** y pegar las expresiones.

### 1. `posters_public_read` (lectura pública)

| Campo | Valor |
|-------|--------|
| Policy name | `posters_public_read` |
| Allowed operation | `SELECT` |
| Target roles | `public` (o “all users” según UI) |
| USING expression | `bucket_id = 'posters'` |

### 2. `posters_insert_own_folder`

| Campo | Valor |
|-------|--------|
| Policy name | `posters_insert_own_folder` |
| Allowed operation | `INSERT` |
| Target roles | `authenticated` |
| WITH CHECK | ver abajo |

```sql
bucket_id = 'posters'
AND (storage.foldername(name))[1] = auth.uid()::text
```

### 3. `posters_update_own_folder`

| Campo | Valor |
|-------|--------|
| Policy name | `posters_update_own_folder` |
| Allowed operation | `UPDATE` |
| Target roles | `authenticated` |
| USING y WITH CHECK | misma expresión que insert |

```sql
bucket_id = 'posters'
AND (storage.foldername(name))[1] = auth.uid()::text
```

### 4. `posters_delete_own_folder`

| Campo | Valor |
|-------|--------|
| Policy name | `posters_delete_own_folder` |
| Allowed operation | `DELETE` |
| Target roles | `authenticated` |
| USING | misma expresión |

---

## Comprobar

1. En **Storage → posters** debe aparecer el bucket (público).
2. En **Policies** deben listarse las 4 políticas.
3. Sin políticas de lectura, las URLs públicas de carteles no funcionarán aunque el bucket sea “public”.

Ruta de subida prevista (Fase 4): `posters/{tu_user_id}/nombre.jpg`
