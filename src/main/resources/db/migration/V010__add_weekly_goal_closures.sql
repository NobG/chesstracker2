create table weekly_goal_closures (
    id bigserial primary key,
    iso_year integer not null,
    iso_week integer not null,
    status varchar(20) not null,
    note text,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    constraint uq_weekly_goal_closures_iso_week unique (iso_year, iso_week),
    constraint chk_weekly_goal_closures_status check (status in ('NOT_RECORDED', 'ACHIEVED', 'MISSED')),
    constraint chk_weekly_goal_closures_iso_week_range check (iso_week >= 1 and iso_week <= 53)
);
