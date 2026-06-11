alter table rating_snapshots drop constraint chk_rating_snapshots_tactics_rating_non_negative;

alter table rating_snapshots drop constraint chk_rating_snapshots_endgame_rating_non_negative;

alter table rating_snapshots drop column tactics_rating;

alter table rating_snapshots drop column endgame_rating;
