
-- :name find-user :? :1
select * from users where identity = :identity

-- :name create-user :!
insert into users (identity, pass) values (:identity, :pass-hashed)
