/* users 用户表 */
create table if not exists users(
  id integer PRIMARY KEY,
  name varchar(16) unique,
  password varchar(32),
  salt varchar(16),
  accesstoken varchar(32),
  rand varchar(16),
  last_login_time integer,
  last_login_ip varchar(20)
);

/* hosts 主机信息表 */
create table if not exists hosts
(
  id integer primary key AUTOINCREMENT not NULL,
  user_id integer not null,
  name varchar(50) not null,
  state integer not null,
  accesstoken varchar(64),
  ip varchar(100),
  last_active_time integer
);

/* ports 端口映射表 */
create table if not exists ports
(
  id integer PRIMARY KEY AUTOINCREMENT NOT NULL,
  user_id integer NOT NULL,
  host_id integer NOT NULL,
  listen_port integer NOT NULL,
  host_port integer NOT NULL,
  so_timeout integer NOT NULL,
  concurrent_connections integer NOT NULL,
  state integer DEFAULT 0,
  create_time integer,
  last_active_time integer
);
CREATE UNIQUE INDEX ports_listen_port_uindex ON ports(listen_port);