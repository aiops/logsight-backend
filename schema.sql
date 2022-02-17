create table applications (id uuid not null, application_key varchar(255) not null, name varchar(255), status varchar(255), user_id uuid, primary key (id))
create table logs_receipt (id uuid not null, logs_count int4 not null, order_num serial not null, source varchar(255), application_id uuid, primary key (id))
create table result_init (id uuid not null, status varchar(255), logs_receipt_id uuid, primary key (id))
create table time_selection (id uuid not null, date_time_type varchar(255), end_time varchar(255), name varchar(255), start_time varchar(255), user_id uuid, primary key (id))
create table token_entity (token uuid not null, expires_at timestamp, token_duration int8, token_type int4, user_id uuid, primary key (token))
create table users (id uuid not null, activated boolean not null, activation_date timestamp, approaching_limit boolean, available_data int8, date_created timestamp not null, email varchar(255) not null, has_paid boolean not null, key varchar(255) not null, password varchar(255) not null, used_data int8, user_type int4, primary key (id))
alter table logs_receipt add constraint UK_76m69gm9iebv2naycui87bfs5 unique (order_num)
alter table users add constraint UK_6dotkott2kjsp8vw4d0m25fb7 unique (email)
alter table applications add constraint FKfsfqljedcla632u568jl5qf3w foreign key (user_id) references users
alter table logs_receipt add constraint FKn3gdtcnu5g4v4oamvdgncff foreign key (application_id) references applications
alter table result_init add constraint FKj8hjp2c1lx3slfwv8hjg7ccly foreign key (logs_receipt_id) references logs_receipt
alter table time_selection add constraint FK9gox6stwqpk7rpip05soppvfy foreign key (user_id) references users
