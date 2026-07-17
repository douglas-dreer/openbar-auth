INSERT INTO users (id, username, password_hash, role, active)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'admin@openbar.com',
    '$2b$12$rR6GRGBC0E1Hmve4F6JyQO74GUw6FRgkWLWAoVLSg47EzXl24cuSe',
    'ADMIN',
    true
) ON CONFLICT (username) DO NOTHING;
