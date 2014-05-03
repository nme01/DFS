create table files (
	id bigserial not null,
	name text not null,
	size bigint,
	status char(1),
	primary key (id)
);
create index pk_files on files (id);
create unique index uq_files on files (name);

create table servers (
	ip text not null,
	role char(1),
	memory bigint not null,
	last_connection timestamp not null,
	primary key (ip)
);
create index pk_servers on servers (ip);

create table files_on_servers (
	file_id bigint not null,
	server_ip text not null,
	priority int not null,
	primary key (id, ip),
	foreign key (ip) references servers(ip),
	foreign key (id) references files(id)
);

create index pk_filesonservers on files_on_servers (id,ip);
create index fk_filesonservers on files_on_servers (ip);

create table version (
	log bigserial not null
);