create or replace function migrate_openuc_ccid() returns void as $$
begin
    if exists(select * from information_schema.tables where 
            table_catalog = CURRENT_CATALOG and table_schema = CURRENT_SCHEMA
            and table_name = 'ouc_ccid') then

        INSERT INTO mask_caller_id (mcid_id, mcid_from, mask_name, mask_extension) select ccid_id, ccid_from, ccid_to_name, ccid_to_extension from ouc_ccid;
                
    end if;

end;
$$
language plpgsql;

select migrate_openuc_ccid();
drop function migrate_openuc_ccid();
