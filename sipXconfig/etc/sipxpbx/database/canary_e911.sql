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