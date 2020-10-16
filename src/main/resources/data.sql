create or replace schema face_attendance collate utf8mb4_general_ci;

USE face_attendance;

create or replace table authority
(
	id int auto_increment
		primary key,
	role varchar(255) null
);

create or replace table detected_face
(
	id bigint auto_increment
		primary key,
	template longblob null,
	image_url varchar(255) null,
	scene_url varchar(255) null
);

create or replace table device
(
	id bigint auto_increment
		primary key,
	type varchar(255) null,
	enabled bit null,
	ip_address varchar(255) null,
	latitude float null,
	longitude float null,
	model_no varchar(50) null,
	phone_number varchar(255) null,
	serial_no varchar(50) null
);

create or replace table event
(
	id bigint auto_increment
		primary key,
	message varchar(255) null,
	time datetime(6) null,
	type varchar(255) null,
	device_id bigint null,
	constraint FK_EVENT_DEVICE_ID
		foreign key (device_id) references device (id)
);

create or replace table subject
(
	id bigint auto_increment
		primary key,
	created_by varchar(255) null,
	created_date datetime(6) not null,
	last_modified_by varchar(255) NULL,
	last_modified_date datetime(6) null,
	city varchar(50) null,
	country varchar(50) null,
	district varchar(50) null,
	province varchar(50) null,
	street varchar(100) null,
	zip varchar(10) null,
	birth_date date not null,
	email varchar(50) not null,
	gender varchar(10) not null,
	first_name varchar(50) not null,
	last_name varchar(50) not null,
	nid varchar(255) not null,
	phone_number varchar(20) null,
	constraint UK_SUBJECT_EMAIL
		unique (email),
	constraint UK_SUBJECT_NID
		unique (nid)
);

create or replace table account
(
	id bigint auto_increment
		primary key,
	created_by varchar(255) null,
	created_date datetime(6) not null,
	last_modified_by varchar(255) null,
	last_modified_date datetime(6) null,
	enabled tinyint(1) default 1 null,
	password varchar(255) not null,
	username varchar(255) not null,
	subject_id bigint not null,
	constraint UK_ACCOUNT_USERNAME
		unique (username),
	constraint UK_ACCOUNT_SUBJECT_ID
		unique (subject_id),
	constraint FK_ACCOUNT_SUBJECT_ID
		foreign key (subject_id) references subject (id)
);

create or replace table account_authorities
(
	account_id bigint not null,
	authority_id int not null,
	primary key (account_id, authority_id),
	constraint FK_ACCOUN_AUTHORITIES_AUTHROITY_ID
		foreign key (authority_id) references authority (id),
	constraint FK_ACCOUN_AUTHORITIES_ACCOUNT_ID
		foreign key (account_id) references account (id)
);

create or replace table bio_template
(
	id bigint auto_increment
		primary key,
	type int null,
	birth_year int null,
	country varchar(50) null,
	gender varchar(10) null,
	province varchar(50) null,
	template longblob null,
	subject_id bigint not null,
	constraint FK_BIO_TEMPLATE_SUBJECT_ID
		foreign key (subject_id) references subject (id)
);

create or replace table identify
(
	id bigint auto_increment
		primary key,
	created_by varchar(255) null,
	created_date datetime(6) not null,
	last_modified_by varchar(255) null,
	last_modified_date datetime(6) null,
	case_type varchar(20) null,
	prob_image_format varchar(255) null,
	prob_image_quality int null,
	prob_image longblob null,
	subject_id bigint null,
	constraint FK_IDENTITY_SUBJECT_ID
		foreign key (subject_id) references subject (id)
);

create or replace table candidate
(
	id bigint auto_increment
		primary key,
	created_by varchar(255) null,
	created_date datetime(6) not null,
	last_modified_by varchar(255) null,
	last_modified_date datetime(6) null,
	score int null,
	bio_template_id bigint not null,
	identity_id bigint not null,
	constraint FK_CANDIDATE_IDENITY_ID
		foreign key (identity_id) references identify (id),
	constraint FK_CANDIDATE_BIO_TEMPLATE_ID
		foreign key (bio_template_id) references bio_template (id)
);

create or replace table matching_result
(
	id bigint auto_increment
		primary key,
	score int not null,
	bio_template_id bigint null,
	detected_face_id bigint null,
	constraint FK_MATCHING_RESULT_BIO_TEMPLATE_ID
		foreign key (bio_template_id) references bio_template (id),
	constraint FK_MATCHING_RESULT_DETECTED_FACE_ID
		foreign key (detected_face_id) references detected_face (id)
);

create or replace table subject_image
(
	id bigint auto_increment
		primary key,
	created_by varchar(255) null,
	created_date datetime(6) not null,
	last_modified_by varchar(255) null,
	last_modified_date datetime(6) null,
	bio_type varchar(10) null,
	enabled tinyint(1) default 1 null,
	format varchar(10) null,
	image_quality int null,
	image_url varchar(255) null,
	pose varchar(50) null,
	subject_id bigint null,
	constraint FK_SUBJECT_IMAGE_SUBJECT_ID
		foreign key (subject_id) references subject (id)
);
