create or replace function migrate_openuc_e911_db() returns void as $$
begin
    if exists(select * from information_schema.tables where 
            table_catalog = CURRENT_CATALOG and table_schema = CURRENT_SCHEMA
            and table_name = 'e911_erl') then

        alter table e911_erl rename column erl_id to e911_id;
        alter table e911_erl drop column ip_addr_start;
        alter table e911_erl drop column ip_addr_end;
        alter table e911_erl rename to e911;
        alter sequence erl_seq rename to e911_seq;

    end if;

end;
$$
language plpgsql;

select migrate_openuc_e911_db();
drop function migrate_openuc_e911_db();

