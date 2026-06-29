create extension if not exists pgcrypto;

create or replace function loop_set_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

create table if not exists loop_control (
  control_key text primary key,
  control_value jsonb not null default '{}'::jsonb,
  updated_at timestamptz not null default now()
);

create table if not exists incoming_events (
  id uuid primary key default gen_random_uuid(),
  source text not null,
  event_type text not null,
  repo text not null,
  external_event_id text not null,
  dedupe_key text not null unique,
  payload jsonb not null default '{}'::jsonb,
  received_at timestamptz not null default now(),
  processed_at timestamptz,
  status text not null default 'new' check (status in ('new', 'normalized', 'ignored', 'error')),
  error_message text
);

create table if not exists work_items (
  id uuid primary key default gen_random_uuid(),
  repo text not null,
  source text not null,
  source_ref text,
  dedupe_key text not null unique,
  title text not null,
  body text,
  priority int not null check (priority between 0 and 9),
  risk_level text not null check (risk_level in ('low', 'medium', 'high', 'critical')),
  status text not null check (status in (
    'new',
    'triaged',
    'in_progress',
    'verifying',
    'blocked',
    'retry_later',
    'waiting_human',
    'done',
    'dead_letter'
  )),
  recommended_action text,
  attempts int not null default 0,
  max_attempts int not null default 3,
  next_run_at timestamptz not null default now(),
  locked_by text,
  locked_at timestamptz,
  last_action text,
  last_error text,
  triage_reason text,
  payload jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  completed_at timestamptz
);

create table if not exists run_log (
  id uuid primary key default gen_random_uuid(),
  run_type text not null check (run_type in ('triage', 'action', 'verify', 'summary', 'maintenance')),
  repo text not null,
  work_item_id uuid references work_items(id),
  started_at timestamptz not null default now(),
  ended_at timestamptz,
  status text not null check (status in ('running', 'success', 'failed', 'partial', 'paused')),
  actor text not null,
  summary text,
  actions jsonb not null default '[]'::jsonb,
  tokens_in int default 0,
  tokens_out int default 0,
  estimated_cost numeric(12,4) default 0,
  error_message text
);

create table if not exists decisions (
  id uuid primary key default gen_random_uuid(),
  repo text not null,
  work_item_id uuid references work_items(id),
  decision_type text not null,
  decided_by text not null,
  reason text,
  payload jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create table if not exists budgets (
  budget_date date not null,
  repo text not null,
  metric text not null,
  limit_value numeric(12,2) not null,
  used_value numeric(12,2) not null default 0,
  unit text not null,
  primary key (budget_date, repo, metric)
);

create table if not exists locks (
  lock_name text primary key,
  owner text not null,
  acquired_at timestamptz not null default now(),
  expires_at timestamptz not null
);

create index if not exists idx_work_items_status_priority
  on work_items(status, priority, next_run_at);

create index if not exists idx_work_items_repo_status
  on work_items(repo, status);

create index if not exists idx_incoming_events_repo_status
  on incoming_events(repo, status);

create index if not exists idx_run_log_repo_started
  on run_log(repo, started_at desc);

drop trigger if exists trg_work_items_updated_at on work_items;
create trigger trg_work_items_updated_at
before update on work_items
for each row execute function loop_set_updated_at();

insert into loop_control(control_key, control_value)
values
  ('pause_all', '{"value": false}'::jsonb),
  ('current_phase', '{"value": "L1"}'::jsonb)
on conflict (control_key) do nothing;
