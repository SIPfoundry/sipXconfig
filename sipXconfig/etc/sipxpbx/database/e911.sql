create sequence e911_seq;

create table e911 (
  e911_id integer not null,
  location character varying not null,
  elin character varying not null,
  address_info character varying,
  description character varying,
  constraint e911_id_primary_key primary key (e911_id),
  constraint e911_elin_unique_key UNIQUE (elin)
);

alter table users
  add column e911_location_id integer;
alter table phone
  add column e911_location_id integer;
  
alter table users
  add constraint fk_e911_users_id
  foreign key (e911_location_id)
  references e911(e911_id);
  
alter table phone
  add constraint fk_e911_phone_id
  foreign key (e911_location_id)
  references e911(e911_id);