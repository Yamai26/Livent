-- Livent Fase 2: Row Level Security (RLS obligatorio con automatic RLS activo)

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;

-- Grants de escritura en events (RLS restringe filas)
GRANT INSERT, UPDATE, DELETE ON public.events TO authenticated;

-- =============================================================================
-- events
-- =============================================================================

-- Invitado (anon): solo lectura de eventos activos (feed / detalle)
CREATE POLICY events_select_active_anon
    ON public.events
    FOR SELECT
    TO anon
    USING (status = 'active');

-- Usuario autenticado: lectura de eventos activos (exploración compartida con invitado)
CREATE POLICY events_select_active_authenticated
    ON public.events
    FOR SELECT
    TO authenticated
    USING (status = 'active');

-- Publisher: leer todos sus eventos (draft, past, active) — necesario Fase 4 dashboard
CREATE POLICY events_select_own_publisher
    ON public.events
    FOR SELECT
    TO authenticated
    USING (publisher_id = auth.uid());

CREATE POLICY events_insert_own_publisher
    ON public.events
    FOR INSERT
    TO authenticated
    WITH CHECK (publisher_id = auth.uid());

CREATE POLICY events_update_own_publisher
    ON public.events
    FOR UPDATE
    TO authenticated
    USING (publisher_id = auth.uid())
    WITH CHECK (publisher_id = auth.uid());

CREATE POLICY events_delete_own_publisher
    ON public.events
    FOR DELETE
    TO authenticated
    USING (publisher_id = auth.uid());

-- =============================================================================
-- favorites (sin acceso anon)
-- =============================================================================

CREATE POLICY favorites_select_own
    ON public.favorites
    FOR SELECT
    TO authenticated
    USING (user_id = auth.uid());

CREATE POLICY favorites_insert_own
    ON public.favorites
    FOR INSERT
    TO authenticated
    WITH CHECK (user_id = auth.uid());

CREATE POLICY favorites_delete_own
    ON public.favorites
    FOR DELETE
    TO authenticated
    USING (user_id = auth.uid());

-- =============================================================================
-- profiles (sin acceso anon)
-- =============================================================================

CREATE POLICY profiles_select_own
    ON public.profiles
    FOR SELECT
    TO authenticated
    USING (id = auth.uid());

CREATE POLICY profiles_update_own
    ON public.profiles
    FOR UPDATE
    TO authenticated
    USING (id = auth.uid())
    WITH CHECK (id = auth.uid());

-- Permite INSERT del propio perfil (p. ej. si el trigger de signup fallara o migración)
CREATE POLICY profiles_insert_own
    ON public.profiles
    FOR INSERT
    TO authenticated
    WITH CHECK (id = auth.uid());
