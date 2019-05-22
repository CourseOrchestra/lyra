create schema foo version '1.0';

create table foo (
  id int not null primary key,
  /**
  {"caption": "name field caption"}
   */
  name varchar (10),

  intField int,

  datetimeField datetime

);
