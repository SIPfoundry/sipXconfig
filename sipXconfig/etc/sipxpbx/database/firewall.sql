-- alter table firewall_rule drop constraint firewall_rule_fk1;
-- drop table firewall_server_group;
create table firewall_server_group (
   firewall_server_group_id int4 not null,      
   name varchar(255) not null unique,
   servers varchar(255) not null unique,
   primary key (firewall_server_group_id)
);

--drop table firewall_rule
create table firewall_rule (
   firewall_rule_id int4 not null,
   prioritize boolean default false,
   address_type character(64) not null,
   firewall_server_group_id int4,
   system_id char(16),
   primary key (firewall_rule_id)
);
 
create sequence firewall_rule_seq;
create sequence firewall_server_group_seq;
