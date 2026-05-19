-- Livent Fase 2: esquema inicial (profiles, events, favorites)
-- Aplicar en: Supabase Dashboard → SQL Editor (en orden) o `supabase db push`

-- ---------------------------------------------------------------------------
-- Tipos enumerados
-- ---------------------------------------------------------------------------
CREATE TYPE public.user_role AS ENUM ('user', 'publisher');

CREATE TYPE public.subscription_tier AS ENUM ('free', 'premium');

CREATE TYPE public.event_status AS ENUM ('active', 'past', 'draft');

-- ---------------------------------------------------------------------------
-- profiles (1:1 con auth.users)
-- ---------------------------------------------------------------------------
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users (id) ON DELETE CASCADE,
    role public.user_role NOT NULL DEFAULT 'user',
    display_name TEXT,
    email TEXT,
    avatar_url TEXT,
    -- Solo relevante para publishers; users lo ignoran en la app
    subscription_tier public.subscription_tier NOT NULL DEFAULT 'free',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE public.profiles IS 'Perfil de app; 1:1 con auth.users. Rol user|publisher.';
COMMENT ON COLUMN public.profiles.subscription_tier IS 'Plan publisher: free (máx. 1 evento active) | premium (ilimitado).';

-- ---------------------------------------------------------------------------
-- events
-- ---------------------------------------------------------------------------
CREATE TABLE public.events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    publisher_id UUID NOT NULL REFERENCES public.profiles (id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    artist TEXT NOT NULL DEFAULT '',
    description TEXT NOT NULL DEFAULT '',
    location TEXT NOT NULL DEFAULT '',
    starts_at TIMESTAMPTZ NOT NULL,
    poster_url TEXT,
    status public.event_status NOT NULL DEFAULT 'draft',
    is_featured BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT events_title_not_empty CHECK (char_length(trim(title)) > 0)
);

COMMENT ON TABLE public.events IS 'Eventos publicados. Invitados (anon) solo leen status=active.';
COMMENT ON COLUMN public.events.is_featured IS 'Boost / carrusel destacados en Home (Fase 6 Stripe).';
-- Regla de negocio Free: máx. 1 evento active por publisher con subscription_tier=free.
-- Validación principal en app (Fase 4); refuerzo opcional vía trigger check_free_publisher_active_limit.

-- ---------------------------------------------------------------------------
-- favorites
-- ---------------------------------------------------------------------------
CREATE TABLE public.favorites (
    user_id UUID NOT NULL REFERENCES public.profiles (id) ON DELETE CASCADE,
    event_id UUID NOT NULL REFERENCES public.events (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, event_id)
);

COMMENT ON TABLE public.favorites IS 'Favoritos usuario↔evento; solo authenticated (RLS).';

-- ---------------------------------------------------------------------------
-- Índices
-- ---------------------------------------------------------------------------
CREATE INDEX events_status_starts_at_idx ON public.events (status, starts_at);

CREATE INDEX events_featured_active_idx ON public.events (is_featured)
    WHERE status = 'active' AND is_featured = true;

CREATE INDEX favorites_user_id_idx ON public.favorites (user_id);

CREATE INDEX events_publisher_id_idx ON public.events (publisher_id);

-- ---------------------------------------------------------------------------
-- updated_at automático
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE TRIGGER profiles_set_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER events_set_updated_at
    BEFORE UPDATE ON public.events
    FOR EACH ROW
    EXECUTE FUNCTION public.set_updated_at();

-- ---------------------------------------------------------------------------
-- Límite plan Free: 1 evento active por publisher free
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.check_free_publisher_active_event_limit()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    publisher_tier public.subscription_tier;
    active_count INTEGER;
BEGIN
    IF NEW.status IS DISTINCT FROM 'active' THEN
        RETURN NEW;
    END IF;

    SELECT p.subscription_tier
    INTO publisher_tier
    FROM public.profiles AS p
    WHERE p.id = NEW.publisher_id;

    IF publisher_tier IS NULL OR publisher_tier <> 'free' THEN
        RETURN NEW;
    END IF;

    SELECT count(*)::INTEGER
    INTO active_count
    FROM public.events AS e
    WHERE e.publisher_id = NEW.publisher_id
      AND e.status = 'active'
      AND e.id IS DISTINCT FROM NEW.id;

    IF active_count >= 1 THEN
        RAISE EXCEPTION 'Free plan allows at most one active event per publisher'
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER events_enforce_free_active_limit
    BEFORE INSERT OR UPDATE OF status, publisher_id ON public.events
    FOR EACH ROW
    EXECUTE FUNCTION public.check_free_publisher_active_event_limit();

-- ---------------------------------------------------------------------------
-- Perfil al registrarse (Auth) — Fase 3 puede pasar role en raw_user_meta_data
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    meta_role TEXT;
    resolved_role public.user_role;
BEGIN
    meta_role := NEW.raw_user_meta_data ->> 'role';

    resolved_role := CASE
        WHEN meta_role = 'publisher' THEN 'publisher'::public.user_role
        ELSE 'user'::public.user_role
    END;

    INSERT INTO public.profiles (id, email, display_name, role)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(
            NEW.raw_user_meta_data ->> 'display_name',
            split_part(COALESCE(NEW.email, ''), '@', 1)
        ),
        resolved_role
    )
    ON CONFLICT (id) DO NOTHING;

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION public.handle_new_user IS
    'Crea fila en profiles tras signup. Fase 3: enviar raw_user_meta_data.role = user|publisher.';

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- ---------------------------------------------------------------------------
-- Permisos base (Supabase roles)
-- ---------------------------------------------------------------------------
GRANT USAGE ON SCHEMA public TO anon, authenticated;

GRANT SELECT ON public.events TO anon, authenticated;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.favorites TO authenticated;

GRANT SELECT, INSERT, UPDATE ON public.profiles TO authenticated;

GRANT USAGE ON TYPE public.user_role TO anon, authenticated;
GRANT USAGE ON TYPE public.subscription_tier TO anon, authenticated;
GRANT USAGE ON TYPE public.event_status TO anon, authenticated;
