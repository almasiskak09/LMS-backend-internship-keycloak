INSERT INTO user_entity (email,email_verified,enabled,first_name,last_name,realm_id,username)
VALUES
    ('almas@mail.ru',true,true,'Almas','Iskak','8525acbb-2401-45a9-975b-61df9924c3c1','almas06'),
    ('doni@mail.ru',true,true,'Daniyar','Tolik','8525acbb-2401-45a9-975b-61df9924c3c1','doni24');

INSERT INTO client (client_id,name)
VALUES
    ('internship-client','client for internship');

INSERT INTO keycloak_role(name)
VALUES
    ('USER','TEACHER','ADMIN');
