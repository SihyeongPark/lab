/* introduction table */
CREATE TABLE introduction
(
    idx                bigint auto_increment    primary key,
    created_by         varchar(255)     null,
    created_date       datetime(6)      null,
    last_modified_by   varchar(255)     null,
    last_modified_date datetime(6)      null,
    active_status      varchar(255)     null,
    content            longtext         null,
    title              varchar(255)     null
);

ALTER TABLE introduction AUTO_INCREMENT=1;
DROP TABLE introduction;