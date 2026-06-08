alter table daily_notes
    add column if not exists completed_at timestamp with time zone null;

update daily_notes
set completed_at = coalesce(updated_at, now())
where completion_status = 'COMPLETED'
  and completed_at is null;
