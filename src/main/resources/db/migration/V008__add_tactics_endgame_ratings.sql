alter table rating_snapshots add column tactics_rating integer;

alter table rating_snapshots add column endgame_rating integer;

alter table rating_snapshots
    add constraint chk_rating_snapshots_tactics_rating_non_negative check (tactics_rating is null or tactics_rating >= 0);

alter table rating_snapshots
    add constraint chk_rating_snapshots_endgame_rating_non_negative check (endgame_rating is null or endgame_rating >= 0);
