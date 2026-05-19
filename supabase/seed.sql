-- Livent: datos de prueba (ejecutar DESPUÉS de migraciones 001–003)
--
-- Prerrequisito: crear usuarios en Supabase Dashboard → Authentication → Users
--   publisher1@test.livent  (contraseña de prueba)
--   publisher2@test.livent
--   user1@test.livent
-- El trigger handle_new_user crea filas en profiles automáticamente (rol user por defecto).

-- Ajustar roles y planes publisher
UPDATE public.profiles
SET
    role = 'publisher',
    subscription_tier = 'free',
    display_name = 'Editor Free'
WHERE email = 'publisher1@test.livent';

UPDATE public.profiles
SET
    role = 'publisher',
    subscription_tier = 'premium',
    display_name = 'Editor Premium'
WHERE email = 'publisher2@test.livent';

UPDATE public.profiles
SET
    role = 'user',
    display_name = 'María Espectadora'
WHERE email = 'user1@test.livent';

-- Eventos (publisher1: 1 active free; publisher2: varios + destacado)
INSERT INTO public.events (
    publisher_id,
    title,
    artist,
    description,
    location,
    starts_at,
    poster_url,
    status,
    is_featured
)
SELECT
    p.id,
  v.title,
  v.artist,
  v.description,
  v.location,
  v.starts_at,
  v.poster_url,
  v.status::public.event_status,
  v.is_featured
FROM (
    VALUES
        (
            'publisher1@test.livent',
            'Noche Electrónica Valencia',
            'DJ Nova',
            'Sesión de house y techno en la marina.',
            'Valencia, España',
            (now() + interval '14 days'),
            NULL,
            'active',
            false
        ),
        (
            'publisher1@test.livent',
            'Borrador sin publicar',
            'Artista X',
            'Evento en preparación.',
            'Madrid',
            (now() + interval '30 days'),
            NULL,
            'draft',
            false
        ),
        (
            'publisher2@test.livent',
            'Festival Indie Costa',
            'The Waves',
            'Tres días de indie y rock alternativo.',
            'Barcelona',
            (now() + interval '21 days'),
            NULL,
            'active',
            true
        ),
        (
            'publisher2@test.livent',
            'Jazz en el Jardín',
            'Blue Note Collective',
            'Concierto acústico al aire libre.',
            'Sevilla',
            (now() + interval '7 days'),
            NULL,
            'active',
            false
        ),
        (
            'publisher2@test.livent',
            'Concierto pasado demo',
            'Legacy Band',
            'Evento ya celebrado.',
            'Bilbao',
            (now() - interval '30 days'),
            NULL,
            'past',
            false
        )
) AS v (
    publisher_email,
    title,
    artist,
    description,
    location,
    starts_at,
    poster_url,
    status,
    is_featured
)
JOIN public.profiles AS p ON p.email = v.publisher_email;

-- Favoritos de user1 sobre eventos activos
INSERT INTO public.favorites (user_id, event_id)
SELECT u.id, e.id
FROM public.profiles AS u
CROSS JOIN public.events AS e
WHERE u.email = 'user1@test.livent'
  AND e.status = 'active'
  AND e.title IN ('Noche Electrónica Valencia', 'Festival Indie Costa')
ON CONFLICT DO NOTHING;
