begin;
do $$
declare
	file_1_id int;
	file_2_id int;
begin
insert into files (name, size, status) values ('file1', 128, 'U') returning id into file_1_id;
insert into files (name, size, status) values ('file2', 256, 'D') returning id into file_2_id;

insert into servers (ip, role, memory, last_connection) values ('192.168.0.1', 'M', 200000, now());

insert into servers (ip, role, memory, last_connection) values ('192.168.0.2', 'H', 200000, now());
insert into servers (ip, role, memory, last_connection) values ('192.168.0.3', 'L', 200000, now());

insert into files_on_servers(file_id, server_ip, priority) values(file_1_id, '192.168.0.3', 0);

insert into files_on_servers(file_id, server_ip, priority) values(file_2_id, '192.168.0.3', 0);

end $$;
end;