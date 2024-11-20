create database if not exists intelliedu;

use intelliedu;

create table if not exists `user`
(
    `id`          bigint auto_increment comment 'id' primary key,
    `username`    varchar(256)                           not null comment 'User Name',
    `password`    varchar(256)                           not null comment 'Password',
    `nickname`    varchar(256)                           null comment 'Nickname',
    `avatar`      varchar(1024)                          null comment 'User Avatar',
    `role`        varchar(256) default 'user'            not null comment 'User Role (user, admin)',
    `create_time` datetime     default CURRENT_TIMESTAMP not null comment 'Creation Time',
    `update_time` datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    `deleted`     tinyint      default 0                 not null comment 'Is Deleted'
) comment 'User' collate = utf8mb4_unicode_ci;

create table if not exists application
(
    `id`            bigint auto_increment comment 'ID' primary key,
    `app_name`      varchar(128)                       not null comment 'Application Name',
    `description`   varchar(2048)                      null comment 'Application Description',
    `image_url`     varchar(1024)                      null comment 'Application Image URL',
    `type`          tinyint  default 0                 not null comment 'Application Type (0 - Grade, 1 - Evaluation)',
    `strategy`      tinyint  default 0                 not null comment 'Scoring Strategy (0 - Custom, 1 - AI)',
    `user_id`       bigint                             not null comment 'Creator User ID',
    `audit_status`  int      default 0                 not null comment 'Audit Status: 0 - Pending, 1 - Approved, 2 - Rejected',
    `auditor_id`    bigint                             null comment 'Auditor User ID',
    `audit_message` varchar(512)                       null comment 'Audit Message',
    `audit_time`    datetime                           null comment 'Audit Time',
    `create_time`   datetime default CURRENT_TIMESTAMP not null comment 'Creation Time',
    `update_time`   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    `deleted`       tinyint  default 0                 not null comment 'Is Deleted',
    index idx_app_name (`app_name`)
) comment 'Application' collate = utf8mb4_unicode_ci;

create table if not exists question
(
    `id`          bigint auto_increment comment 'ID' primary key,
    `app_id`      bigint                             not null comment 'Application ID',
    `questions`   json                               null comment 'Question List (JSON)',
    `user_id`     bigint                             not null comment 'Creator User ID',
    `create_time` datetime default CURRENT_TIMESTAMP not null comment 'Creation Time',
    `update_time` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    `deleted`     tinyint  default 0                 not null comment 'Is Deleted',
    index idx_app_id (`app_id`)
) comment 'Question' collate = utf8mb4_unicode_ci;

create table if not exists scoring
(
    `id`                bigint auto_increment comment 'ID' primary key,
    `app_id`            bigint                             not null comment 'Application ID',
    `result_name`       varchar(128)                       not null comment 'Result name',
    `result_detail`     text                               null comment 'Result Detail',
    `result_image_url`  varchar(1024)                      null comment 'Result Image URL',
    `result_threshold`  int                                null comment 'Score Threshold For This Result, Intended For Grade-Type Applications',
    `result_attributes` json                               null comment 'Result Attribute Array (JSON), Intended For Evaluation-Type Applications',
    `user_id`           bigint                             not null comment 'Creator User ID',
    `create_time`       datetime default CURRENT_TIMESTAMP not null comment 'Creation Time',
    `update_time`       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    `deleted`           tinyint  default 0                 not null comment 'Is Deleted',
    index idx_app_id (`app_id`)
) comment 'Scoring' collate = utf8mb4_unicode_ci;

create table if not exists answer_record
(
    `id`               bigint auto_increment comment 'ID' primary key,
    `user_id`          bigint                             not null comment 'Creator User ID',
    `app_id`           bigint                             not null comment 'Application ID',
    `app_type`         tinyint  default 0                 not null comment 'Application Type (0 - Grade, 1 - Evaluation)',
    `strategy`         tinyint  default 0                 not null comment 'Scoring Strategy (0 - Custom, 1 - AI)',
    `answers`          json                               null comment 'User Answer List (JSON)',
    `result_id`        bigint                             null comment 'Result ID',
    `result_name`      varchar(128)                       null comment 'Result name',
    `result_detail`    text                               null comment 'Result Detail',
    `result_image_url` varchar(1024)                      null comment 'Result Image URL',
    `result_grade`     int                                null comment 'Result Grade, Intended For Grade-Type Applications',
    `create_time`      datetime default CURRENT_TIMESTAMP not null comment 'Creation Time',
    `update_time`      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    `deleted`          tinyint  default 0                 not null comment 'Is Deleted',
    index idx_user_id (`user_id`),
    index idx_app_id (`app_id`)
) comment 'Answer Record' collate = utf8mb4_unicode_ci;
