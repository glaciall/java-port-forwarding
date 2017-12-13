/* users 用户表 */
create table if not exists users(
  id integer PRIMARY KEY,
  name varchar(16) unique,
  password varchar(32),
  salt varchar(16),
  accesstoken varchar(32),
  rand varchar(16),
  last_login_time datetime,
  last_login_ip varchar(20)
);

/* clients 客户机信息表 */
create table if not exists clients
(
  id integer primary key AUTOINCREMENT not NULL,
  user_id integer not null,
  name varchar(50) not null,
  state integer not null,
  accesstoken varchar(64),
  ip varchar(100),
  last_active_time datetime
);

/* ports 端口映射表 */
create table if not exists ports
(
  id integer PRIMARY KEY AUTOINCREMENT NOT NULL,
  user_id integer NOT NULL,
  server_port integer NOT NULL,
  client_port integer NOT NULL,
  state integer DEFAULT 0,
  create_time DATETIME,
  last_active_time DATETIME
);