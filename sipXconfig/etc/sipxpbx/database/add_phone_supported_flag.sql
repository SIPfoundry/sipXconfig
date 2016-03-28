-- add flag is_supported for phones
alter table phone add column is_supported boolean not null default true;