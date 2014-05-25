create table files (
	id bigserial not null,
	name text not null,
	size bigint,
	status char(1),
	primary key (id)
);
alter table files add constraint files_status_constraint check (status = 'U' or status = 'D' or status = 'H');

create index pk_files on files (id);
create unique index uq_files on files (name);

create table servers (
	id bigserial not null,
	ip text not null,
	role char(1),
	memory bigint not null,
	last_connection timestamp not null,
	primary key (id)
);
alter table servers add constraint servers_role_constraint check (role = 'M' or role = 'H' or  role='L');

create index pk_servers on servers (ip);

create table files_on_servers (
	file_id bigint not null,
	server_id bigint not null,
	priority int not null,
	primary key (file_id, server_id),
	foreign key (server_id) references servers(id),
	foreign key (file_id) references files(id)
);

create index pk_filesonservers on files_on_servers (file_id, server_id);
create index fk_filesonservers on files_on_servers (server_id);

create table log (
	version bigserial not null,
	sql text
);

create view servers_vw as
select s.id,s.ip,s.role,s.memory,s.last_connection, COALESCE (count(f.id),0) as filesNumber, COALESCE (s.memory-sum(f.size),s.memory) as freeMemory from servers s 
	left join files_on_servers fos on fos.server_id = s.id
	left join files f on fos.file_id = f.id
	group by s.id,s.ip,s.role,s.memory,s.last_connection;

GRANT ALL ON files, servers,files_on_servers, log, servers_vw TO rsodfs;
