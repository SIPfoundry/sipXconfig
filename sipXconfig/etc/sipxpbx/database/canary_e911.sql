create or replace function check_e911() returns void as $$
begin
    if not exists (select 0 from pg_class where relname = 'e911_seq' )
	then
		create sequence e911_seq;
	end if;

	if not exists(select * from information_schema.tables where 
        table_catalog = CURRENT_CATALOG and table_schema = CURRENT_SCHEMA
        and table_name = 'e911') 
	then
		create table e911 (
		  e911_id integer not null,
		  location character varying not null,
		  elin character varying not null,
		  address_info character varying,
		  description character varying,
		  constraint e911_id_primary_key primary key (e911_id),
		  constraint e911_elin_unique_key UNIQUE (elin)
		);
	end if;
end;
$$
language plpgsql;

select check_e911();
