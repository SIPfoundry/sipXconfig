create or replace function migrate_openuc_e911_db() returns void as $$
declare
erl record;
newseq integer := 0;
begin
    if exists(select * from information_schema.tables where 
            table_catalog = CURRENT_CATALOG and table_schema = CURRENT_SCHEMA
            and table_name = 'e911_erl') then

		newseq := (select last_value from erl_seq);
		INSERT INTO e911 (e911_id, location, elin, address_info, description) select erl_id, location, elin, address_info, description from e911_erl;
		execute 'alter sequence e911_seq restart with ' || newseq + 1;
		
		alter table e911_erl rename to e911_erl_migrated;
    end if;

end;
$$
language plpgsql;

select migrate_openuc_e911_db();