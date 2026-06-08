alter table daily_notes
  add column if not exists completion_status varchar(20) not null default 'OPEN';

alter table daily_notes
  add constraint daily_notes_completion_status_check
  check (completion_status in ('OPEN', 'PARTIAL', 'COMPLETED'));
