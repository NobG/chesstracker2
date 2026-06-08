create table if not exists motivation_quotes (
    id bigserial primary key,
    quote_text text not null,
    author varchar(120),
    active boolean not null default true,
    sort_order integer not null default 0,
    created_at timestamp with time zone not null default now()
);

insert into motivation_quotes (quote_text, author, sort_order)
values
    ('Jeder starke Zug beginnt mit einem klaren Gedanken.', 'chesstracker2', 1),
    ('Wer seine Fehler notiert, trainiert doppelt.', 'chesstracker2', 2),
    ('Heute zaehlt nicht Perfektion, sondern ein bewusster Zug mehr.', 'chesstracker2', 3),
    ('Taktik gewinnt Partien, Gewohnheit gewinnt Fortschritt.', 'chesstracker2', 4),
    ('Ein guter Trainingsplan ist wie eine gute Stellung: ruhig, stabil und voller Moeglichkeiten.', 'chesstracker2', 5),
    ('Spiele nicht nur schneller - denke klarer.', 'chesstracker2', 6),
    ('Jede geloeste Aufgabe ist ein kleiner Baustein deiner naechsten starken Partie.', 'chesstracker2', 7),
    ('Wer Endspiele versteht, spielt das Mittelspiel mit mehr Vertrauen.', 'chesstracker2', 8),
    ('Fortschritt entsteht, wenn du deine Muster erkennst und deine Fehler nicht wiederholst.', 'chesstracker2', 9),
    ('Trainiere wie ein Herausforderer, spiele wie ein ruhiger Meister.', 'chesstracker2', 10);
