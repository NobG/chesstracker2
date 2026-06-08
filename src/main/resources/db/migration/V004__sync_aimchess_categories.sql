update training_categories
set category_key = 'advantage-capitalization',
    name = 'Advantage Capitalization',
    description = 'Turning better positions into wins.',
    sort_order = 10,
    active = true,
    updated_at = now()
where category_key = 'advantage-conversion';

update training_categories
set category_key = 'practice-visualization',
    name = 'Practice visualization',
    description = 'Board memory and visualization practice.',
    sort_order = 40,
    active = true,
    updated_at = now()
where category_key = 'visualization';

update training_categories
set category_key = 'endgame',
    name = 'Endgame',
    description = 'Technical endgame positions and conversion.',
    sort_order = 90,
    active = true,
    updated_at = now()
where category_key = 'endgames';

update training_categories
set category_key = 'defender',
    name = 'Defender',
    description = 'Saving worse positions and finding resilient resources.',
    sort_order = 100,
    active = true,
    updated_at = now()
where category_key = 'defense';

update training_categories
set category_key = 'blunder-preventer',
    name = 'Blunder Preventer',
    description = 'Checks before moving and tactical safety.',
    sort_order = 50,
    active = true,
    updated_at = now()
where category_key = 'blunder-prevention';

update training_categories
set category_key = 'time-trainer',
    name = 'Time Trainer',
    description = 'Clock usage and practical decision speed.',
    sort_order = 110,
    active = true,
    updated_at = now()
where category_key = 'time-management';

insert into training_categories (category_key, name, description, sort_order, active)
select 'advantage-capitalization', 'Advantage Capitalization', 'Turning better positions into wins.', 10, true
where not exists (select 1 from training_categories where category_key = 'advantage-capitalization');

insert into training_categories (category_key, name, description, sort_order, active)
select 'tactics', 'Tactics', 'Tactical pattern recognition and forcing moves.', 20, true
where not exists (select 1 from training_categories where category_key = 'tactics');

insert into training_categories (category_key, name, description, sort_order, active)
select 'opening-improver', 'Opening Improver', 'Improving opening understanding and early middlegame plans.', 30, true
where not exists (select 1 from training_categories where category_key = 'opening-improver');

insert into training_categories (category_key, name, description, sort_order, active)
select 'practice-visualization', 'Practice visualization', 'Board memory and visualization practice.', 40, true
where not exists (select 1 from training_categories where category_key = 'practice-visualization');

insert into training_categories (category_key, name, description, sort_order, active)
select 'blunder-preventer', 'Blunder Preventer', 'Checks before moving and tactical safety.', 50, true
where not exists (select 1 from training_categories where category_key = 'blunder-preventer');

insert into training_categories (category_key, name, description, sort_order, active)
select '360-trainer', '360 Trainer', 'Full-board tactical awareness from every angle.', 60, true
where not exists (select 1 from training_categories where category_key = '360-trainer');

insert into training_categories (category_key, name, description, sort_order, active)
select 'intuition-trainer', 'Intuition Trainer', 'Fast pattern judgment and move selection.', 70, true
where not exists (select 1 from training_categories where category_key = 'intuition-trainer');

insert into training_categories (category_key, name, description, sort_order, active)
select 'retry-mistakes', 'Retry Mistakes', 'Review and repeat previously missed positions.', 80, true
where not exists (select 1 from training_categories where category_key = 'retry-mistakes');

insert into training_categories (category_key, name, description, sort_order, active)
select 'endgame', 'Endgame', 'Technical endgame positions and conversion.', 90, true
where not exists (select 1 from training_categories where category_key = 'endgame');

insert into training_categories (category_key, name, description, sort_order, active)
select 'defender', 'Defender', 'Saving worse positions and finding resilient resources.', 100, true
where not exists (select 1 from training_categories where category_key = 'defender');

insert into training_categories (category_key, name, description, sort_order, active)
select 'time-trainer', 'Time Trainer', 'Clock usage and practical decision speed.', 110, true
where not exists (select 1 from training_categories where category_key = 'time-trainer');

insert into training_categories (category_key, name, description, sort_order, active)
select 'blindfold-tactics', 'Blindfold Tactics', 'Tactics with limited board visibility.', 120, true
where not exists (select 1 from training_categories where category_key = 'blindfold-tactics');

insert into training_categories (category_key, name, description, sort_order, active)
select 'checkmate-patterns', 'Checkmate Patterns', 'Recognizing mating nets and final tactics.', 130, true
where not exists (select 1 from training_categories where category_key = 'checkmate-patterns');

insert into training_categories (category_key, name, description, sort_order, active)
select 'opening-trainer', 'Opening Trainer', 'Opening repetition and repertoire practice.', 140, true
where not exists (select 1 from training_categories where category_key = 'opening-trainer');

insert into training_categories (category_key, name, description, sort_order, active)
select 'tactics-challenge', 'Tactics Challenge', 'Timed tactical challenge mode.', 150, true
where not exists (select 1 from training_categories where category_key = 'tactics-challenge');

update training_categories
set name = 'Advantage Capitalization',
    description = 'Turning better positions into wins.',
    sort_order = 10,
    active = true,
    updated_at = now()
where category_key = 'advantage-capitalization';

update training_categories
set name = 'Tactics',
    description = 'Tactical pattern recognition and forcing moves.',
    sort_order = 20,
    active = true,
    updated_at = now()
where category_key = 'tactics';

update training_categories
set name = 'Opening Improver',
    description = 'Improving opening understanding and early middlegame plans.',
    sort_order = 30,
    active = true,
    updated_at = now()
where category_key = 'opening-improver';

update training_categories
set name = 'Practice visualization',
    description = 'Board memory and visualization practice.',
    sort_order = 40,
    active = true,
    updated_at = now()
where category_key = 'practice-visualization';

update training_categories
set name = 'Blunder Preventer',
    description = 'Checks before moving and tactical safety.',
    sort_order = 50,
    active = true,
    updated_at = now()
where category_key = 'blunder-preventer';

update training_categories
set name = '360 Trainer',
    description = 'Full-board tactical awareness from every angle.',
    sort_order = 60,
    active = true,
    updated_at = now()
where category_key = '360-trainer';

update training_categories
set name = 'Intuition Trainer',
    description = 'Fast pattern judgment and move selection.',
    sort_order = 70,
    active = true,
    updated_at = now()
where category_key = 'intuition-trainer';

update training_categories
set name = 'Retry Mistakes',
    description = 'Review and repeat previously missed positions.',
    sort_order = 80,
    active = true,
    updated_at = now()
where category_key = 'retry-mistakes';

update training_categories
set name = 'Endgame',
    description = 'Technical endgame positions and conversion.',
    sort_order = 90,
    active = true,
    updated_at = now()
where category_key = 'endgame';

update training_categories
set name = 'Defender',
    description = 'Saving worse positions and finding resilient resources.',
    sort_order = 100,
    active = true,
    updated_at = now()
where category_key = 'defender';

update training_categories
set name = 'Time Trainer',
    description = 'Clock usage and practical decision speed.',
    sort_order = 110,
    active = true,
    updated_at = now()
where category_key = 'time-trainer';

update training_categories
set name = 'Blindfold Tactics',
    description = 'Tactics with limited board visibility.',
    sort_order = 120,
    active = true,
    updated_at = now()
where category_key = 'blindfold-tactics';

update training_categories
set name = 'Checkmate Patterns',
    description = 'Recognizing mating nets and final tactics.',
    sort_order = 130,
    active = true,
    updated_at = now()
where category_key = 'checkmate-patterns';

update training_categories
set name = 'Opening Trainer',
    description = 'Opening repetition and repertoire practice.',
    sort_order = 140,
    active = true,
    updated_at = now()
where category_key = 'opening-trainer';

update training_categories
set name = 'Tactics Challenge',
    description = 'Timed tactical challenge mode.',
    sort_order = 150,
    active = true,
    updated_at = now()
where category_key = 'tactics-challenge';

update training_categories
set active = false,
    updated_at = now()
where category_key in ('calculation', 'openings', 'strategy');
