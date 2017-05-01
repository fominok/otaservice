
-- :name find-user :? :1
select * from users where identity = :identity

-- :name create-user! :!
insert into users (identity, pass) values (:identity, :pass-hashed)

-- :name device-ping! :! :n
insert into devices (mac, developer, last_active, device_version)
values (:mac, :developer, :last-active, :device-version)
on conflict (mac)
do update set
developer = :developer,
last_active = :last-active,
device_version = :device-version

-- :name device-info :? :1
select * from devices where mac = :mac

-- :name devices-by-user :? :*
select * from devices where developer = :user
