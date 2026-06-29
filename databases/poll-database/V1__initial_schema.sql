create table if not exists poll
(
    content_id      int not null,
    topic_id        int not null,
    poll_option_id  int not null,
    title           varchar(255),
    episode         int,
    votes           int,
    created_at      datetime,
    updated_at      datetime,
    primary key (content_id, poll_option_id, topic_id)
)
engine = innodb
default charset = utf8mb4;