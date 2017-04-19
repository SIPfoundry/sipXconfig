create or replace function set_canary_e911_contraints() returns void as $$
declare
erl record;
newseq integer := 0;
begin
    if exists(select * from information_schema.tables where 
            table_catalog = CURRENT_CATALOG and table_schema = CURRENT_SCHEMA
            and (table_name = 'e911_erl' or table_name = 'e911_erl_migrated')) then

      if exists(select * from information_schema.table_constraints where 
        table_name = 'users' and constraint_name = 'fk_e911_location_users_id') then

        alter table users drop constraint fk_e911_location_users_id;

      end if;

      if exists(select * from information_schema.table_constraints where 
        table_name = 'phone' and constraint_name = 'fk_e911_location_phone_id') then

        alter table phone drop constraint fk_e911_location_phone_id;

      end if;

    end if;

    if not exists(select * from information_schema.columns where 
      table_name = 'users' and column_name = 'e911_location_id') then

      alter table users add column e911_location_id integer;
      
    end if;

    if not exists(select * from information_schema.columns where 
      table_name = 'phone' and column_name = 'e911_location_id') then

      alter table phone add column e911_location_id integer;
      
    end if;

    if not exists(select * from information_schema.table_constraints where 
        table_name = 'users' and constraint_name = 'fk_e911_users_id') then

        alter table users add constraint fk_e911_users_id
          foreign key (e911_location_id)
          references e911(e911_id);

    end if;

    if not exists(select * from information_schema.table_constraints where 
        table_name = 'phone' and constraint_name = 'fk_e911_phone_id') then

        alter table phone add constraint fk_e911_phone_id 
          foreign key (e911_location_id) 
          references e911(e911_id);

    end if;
end;
$$
language plpgsql;

select set_canary_e911_contraints();  
