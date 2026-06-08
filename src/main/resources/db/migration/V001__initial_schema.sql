create table training_categories (
    id bigserial primary key,
    category_key varchar(80) not null unique,
    name varchar(120) not null,
    description text,
    sort_order integer not null default 0,
    active boolean not null default true,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table daily_training_entries (
    id bigserial primary key,
    training_date date not null,
    category_id bigint not null references training_categories(id),
    trained boolean not null default false,
    success_count integer not null default 0,
    total_count integer not null default 0,
    score integer,
    duration_minutes integer,
    note text,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    constraint uq_daily_training_entries_date_category unique (training_date, category_id),
    constraint chk_daily_training_entries_success_total check (success_count <= total_count),
    constraint chk_daily_training_entries_total_non_negative check (total_count >= 0),
    constraint chk_daily_training_entries_success_non_negative check (success_count >= 0),
    constraint chk_daily_training_entries_duration_non_negative check (duration_minutes is null or duration_minutes >= 0)
);

create table daily_notes (
    id bigserial primary key,
    training_date date not null unique,
    note text,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);
