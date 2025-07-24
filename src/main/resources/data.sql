INSERT INTO app_user (id, email, password, name, role)
VALUES (
           gen_random_uuid(),
           'Admin@gmail.com',
           '$2a$10$LmcJ3a8fsRhawYdXCtsXROvTN0.5prh0yYHsFEI9jK4OWwim1Tz4C',
           'Admin',
           'ADMIN'
       );
