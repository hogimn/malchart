create table anime
(
    id              int not null,
    title           varchar(255),
    link            varchar(255),
    image           varchar(255),
    score           double,
    members         int,
    genre           varchar(255),
    studios         varchar(255),
    source          varchar(255),
    season          varchar(7),
    year            int,
    `rank`           int,
    popularity      int,
    scoring_count   int,
    episodes        int,
    air_status      varchar(255),
    type            varchar(50),
    start_date      datetime,
    end_date        datetime,
    english_title   varchar(1000),
    japanese_title  varchar(1000),
    synopsis        text,
    created_at      datetime,
    updated_at      datetime,
    large_image     varchar(255),
    rating          varchar(50),
    nsfw            varchar(50),
    primary key (id)
)
engine = innodb
default charset = utf8mb4;
