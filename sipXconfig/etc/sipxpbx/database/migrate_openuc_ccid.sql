create or replace function migrate_openuc_ccid() returns void as $$
declare
newseq integer := 0;
begin
    if exists(select * from information_schema.tables where 
            table_catalog = CURRENT_CATALOG and table_schema = CURRENT_SCHEMA
            and table_name = 'ouc_ccid') then
		
		newseq := (select last_value from ccid_seq);
		INSERT INTO mask_caller_id (mcid_id, mcid_from, mask_name, mask_extension) select ccid_id, ccid_from, ccid_to_name, ccid_to_extension from ouc_ccid;
		execute 'alter sequence mcid_seq restart with ' || newseq + 1;
                
        alter table ouc_ccid rename to ouc_ccid_migrated;
    end if;

end;
$$
language plpgsql;

select migrate_openuc_ccid();