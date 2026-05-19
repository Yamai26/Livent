-- Livent Fase 2: bucket Storage para carteles
--
-- NOTA: Las políticas RLS sobre storage.objects NO se pueden crear aquí
-- en el SQL Editor estándar (error 42501: must be owner of relation objects).
-- storage.objects pertenece al rol interno supabase_storage_admin.
--
-- Pasos:
--   A) Ejecuta SOLO el bloque INSERT de abajo (crear bucket).
--   B) Crea las políticas en Dashboard → Storage → posters → Policies
--      O sigue docs/supabase/STORAGE_SETUP.md

INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'posters',
    'posters',
    true,
    5242880,
    ARRAY['image/jpeg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO UPDATE
SET
    public = EXCLUDED.public,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;
