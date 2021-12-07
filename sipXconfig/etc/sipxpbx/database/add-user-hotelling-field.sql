alter table users add column is_hotelling boolean;
update users set is_hotelling = false;
alter table users alter column is_hotelling set not null;
alter table users alter column is_hotelling set default false;