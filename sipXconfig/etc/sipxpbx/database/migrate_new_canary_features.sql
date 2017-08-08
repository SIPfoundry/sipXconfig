create or replace function migrate_new_canary_features() returns void as $$
declare
    loc record;
begin
    -- Enable kamailio presence to primary server only if sipxproxy is enabled
    if exists(select * from feature_local where feature_id = 'proxy') then
        if not exists(select * from feature_local where feature_id = 'kamailiopresence') then
        	-- Locate primary server
            execute 'SELECT location_id, primary_location FROM location where primary_location = true' INTO loc;
            insert into feature_local (feature_id, location_id) values ('kamailiopresence', loc.location_id);
        end if;
    end if;

    -- Enable sbc and kamailiproxy for each instance of sipxproxy on each location
    for loc in select location_id from location loop
        if exists(select * from feature_local where feature_id = 'proxy' and location_id = loc.location_id) then
            if not exists(select * from feature_local where feature_id = 'kamailioproxy' and location_id = loc.location_id) then
                insert into feature_local (feature_id, location_id) values ('kamailioproxy', loc.location_id);
            end if;

            if not exists(select * from feature_local where feature_id = 'mysql' and location_id = loc.location_id) then
                insert into feature_local (feature_id, location_id) values ('mysql', loc.location_id);
            end if;

            if not exists(select * from feature_local where feature_id = 'sbc' and location_id = loc.location_id) then
                insert into feature_local (feature_id, location_id) values ('sbc', loc.location_id);
            end if;
        end if;    
	end loop;

    -- Remove deprecated services
    delete from feature_local where feature_id = 'rls';
    delete from feature_local where feature_id = 'hoteling';
    delete from feature_local where feature_id = 'sipxsss';
    delete from feature_local where feature_id = 'imbot';
    delete from feature_local where feature_id = 'instantMessage';
    delete from feature_local where feature_id = 'sipxsqa';
    delete from feature_local where feature_id = 'saa';
    delete from feature_local where feature_id = 'callqueue';
end;	
$$ language plpgsql;

select migrate_new_canary_features();
