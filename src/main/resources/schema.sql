/* admins */
create table if not exists users(
  id integer PRIMARY KEY,
  name varchar(16),
  password varchar(32),
  salt varchar(16),
  accesstoken varchar(32),
  rand varchar(16),
  last_login_time datetime,
  last_login_ip varchar(20)
);

/* port mappings */
CREATE TABLE if not exists ports
(
  id integer PRIMARY KEY AUTOINCREMENT NOT NULL,
  user_id integer NOT NULL,
  server_port integer NOT NULL,
  client_port integer NOT NULL,
  state integer DEFAULT 0,
  create_time DATETIME,
  last_active_time DATETIME
);