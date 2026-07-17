INSERT INTO categories (id, name, description) VALUES
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Bebidas', 'Bebidas em geral - sucos, refrigerantes, água'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Alcoólicas', 'Drinks, cervejas e destilados'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Pratos Principais', 'Refeições completas - carnes, massas, peixes'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Porções', 'Porções para compartilhar - batata, Coxinha, etc'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Sobremesas', 'Doces e sobremesas')
ON CONFLICT (name) DO NOTHING;
