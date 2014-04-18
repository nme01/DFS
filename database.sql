DROP DATABASE dfs_db;
DROP USER dfs_user;

CREATE USER dfs_user WITH PASSWORD 'dfs_pass';
CREATE DATABASE dfs_db OWNER dfs_user;
SET ROLE dfs_user;

CREATE TABLE Files (
	id BIGINT NOT NULL,
	name VARCHAR NOT NULL,
	size BIGINT,
	status CHAR(1),
	PRIMARY KEY (id)
);
CREATE INDEX pk_files ON Files (id);
CREATE UNIQUE INDEX uq_files ON Files (name);

CREATE TABLE Servers (
	ip INET NOT NULL,
	role CHAR(1),
	memory BIGINT NOT NULL,
	last_connection TIMESTAMP NOT NULL,
	PRIMARY KEY (ip)
);
CREATE INDEX pk_servers ON Servers (ip);

CREATE TABLE Files_on_servers (
	id BIGINT NOT NULL,
	ip INET NOT NULL,
	priority INT NOT NULL,
	PRIMARY KEY (id, ip),
	FOREIGN KEY (ip) REFERENCES Servers(ip),
	FOREIGN KEY (id) REFERENCES Files(id)
);

CREATE INDEX pk_filesonservers ON Files_on_servers (id,ip);
CREATE INDEX fk_filesonservers ON Files_on_servers (ip);

CREATE TABLE Version (
	log BIGINT
);

CREATE SEQUENCE files_seq START 1;
CREATE SEQUENCE log_seq START 1;

