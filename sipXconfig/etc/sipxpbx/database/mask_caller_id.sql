create table mask_caller_id (
  mcid_id integer not null,
  mcid_from character varying not null,
  mask_extension character varying,
  mask_name character varying,
  constraint mcid_pkey primary key (mcid_id )
);
create sequence mcid_seq;
