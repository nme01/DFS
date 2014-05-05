begin;
do $$
declare
	file_1_id int;
	file_2_id int;
	file_3_id int;
	file_4_id int;
	master_id int;
	slave_id int;
begin
insert into files (name, size, status) values ('file1.txt', 0, 'H') returning id into file_1_id;
insert into files (name, size, status) values ('file2.txt', 0, 'H') returning id into file_2_id;
insert into files (name, size, status) values ('file3.txt', 0, 'H') returning id into file_3_id;
insert into files (name, size, status) values ('file4.txt', 0, 'H') returning id into file_4_id;

insert into servers (ip, role, memory, last_connection) values ('127.0.0.1', 'M', 200000, now()) returning id into master_id;

insert into servers (ip, role, memory, last_connection) values ('127.0.0.1', 'L', 200000, now())  returning id into slave_id;

insert into files_on_servers(file_id, server_id, priority) values(file_1_id, slave_id, 0);
insert into files_on_servers(file_id, server_id, priority) values(file_2_id, slave_id, 0);
insert into files_on_servers(file_id, server_id, priority) values(file_3_id, slave_id, 0);
insert into files_on_servers(file_id, server_id, priority) values(file_4_id, slave_id, 0);

end $$;
end;