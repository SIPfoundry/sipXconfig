alter table users add column is_mwi boolean;
update users set is_mwi = true;
alter table users alter column is_mwi set not null;
alter table users alter column is_mwi set default true;