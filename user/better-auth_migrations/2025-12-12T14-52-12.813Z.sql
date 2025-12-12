alter table "users" add column "emailVerified" boolean not null;

alter table "users" add column "productsId" text[]

alter table "users" add column "createdAt" timestamptz default CURRENT_TIMESTAMP not null;

alter table "users" add column "updatedAt" timestamptz default CURRENT_TIMESTAMP not null;

alter table "sessions" add column "expiresAt" timestamptz not null;

alter table "sessions" add column "createdAt" timestamptz default CURRENT_TIMESTAMP not null;

alter table "sessions" add column "updatedAt" timestamptz not null;

alter table "sessions" add column "ipAddress" text;

alter table "sessions" add column "userAgent" text;

alter table "sessions" add column "userId" uuid not null references "users" ("id") on delete cascade;

create index "sessions_userId_idx" on "sessions" ("userId");

alter table "verification" add column "expiresAt" timestamptz not null;

alter table "verification" add column "createdAt" timestamptz default CURRENT_TIMESTAMP not null;

alter table "verification" add column "updatedAt" timestamptz default CURRENT_TIMESTAMP not null;