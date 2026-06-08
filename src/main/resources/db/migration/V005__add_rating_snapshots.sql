create table rating_snapshots (
    id bigserial primary key,
    snapshot_date date not null,
    lichess_blitz integer,
    lichess_rapid integer,
    lichess_classical integer,
    dwz integer,
    fide_elo integer,
    note text,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    constraint uq_rating_snapshots_snapshot_date unique (snapshot_date),
    constraint chk_rating_snapshots_lichess_blitz_non_negative check (lichess_blitz is null or lichess_blitz >= 0),
    constraint chk_rating_snapshots_lichess_rapid_non_negative check (lichess_rapid is null or lichess_rapid >= 0),
    constraint chk_rating_snapshots_lichess_classical_non_negative check (lichess_classical is null or lichess_classical >= 0),
    constraint chk_rating_snapshots_dwz_non_negative check (dwz is null or dwz >= 0),
    constraint chk_rating_snapshots_fide_elo_non_negative check (fide_elo is null or fide_elo >= 0)
);
