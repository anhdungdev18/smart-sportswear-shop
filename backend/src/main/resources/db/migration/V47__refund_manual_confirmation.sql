alter table refunds add column if not exists manual_reference varchar(100);
alter table refunds add column if not exists manual_note text;
