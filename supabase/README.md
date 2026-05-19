# Migraciones Supabase — Livent

Ejecuta en orden en **SQL Editor** del dashboard:

1. `migrations/20260519100000_initial_schema.sql`
2. `migrations/20260519100100_rls_policies.sql`
3. `migrations/20260519100200_storage_posters.sql` (bucket; políticas vía UI)
4. Políticas Storage: `docs/supabase/STORAGE_SETUP.md`
5. Crea usuarios Auth y ejecuta `seed.sql`

Documentación completa: [`docs/supabase/SCHEMA.md`](../docs/supabase/SCHEMA.md)
