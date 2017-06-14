
create table user(
    id bigserial not null primary key,
    name varchar(255) not null,
    created_at timestamp with time zone not null
);

create unique index user_name_unique on user(name);

create table camera(
    id bigserial not null primary key,
    maker varchar(255) not null,
    name varchar(255) not null,
);

create unique index camera_maker_name_unique on camera(maker, name);

create table lens(
    id bigserial not null primary key,
    name varchar(255) not null
);

create unique index lens_name_unique on lens(name);

create table image(
    id bigserial not null primary key,
    user_id bigint not null references user(id),
    camera_id bigint not null references camera(id),
    lens_id bigint not null references lens(id),
    file_name varchar(255) not null,
    date_time timestamp not null,
    created_at timestamp with time zone not null
);

create unique index image_user_file_unique on image(user_id, file_name);

create table condition(
    id bigserial not null primary key,
    image_id bigint not null,
    iso integer not null,
    focal integer not null,
    focal35 integer not null,
    f_number float not null,
    exposure int not null
);

create unique condition_image_id_unique on condition(image_id);
