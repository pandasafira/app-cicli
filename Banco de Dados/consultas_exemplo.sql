-- PARTE 1: consultas que funcionam com os dados iniciais (pilulas e medicamentos)
SELECT 
med_name, 
active_days, 
placebo_days, 
pause_days, 
miss_hours 
FROM birth_pill 
ORDER BY med_name LIMIT 20;

SELECT 
id, 
med_name, 
miss_hours 
FROM birth_pill 
WHERE med_name_search LIKE '%adoless%';

SELECT 
ai.name AS principio_ativo 
FROM birth_pill bp 
JOIN birth_pill_ingredient bpi ON bpi.birth_pill_id = bp.id 
JOIN active_ingredient ai ON ai.id = bpi.ingredient_id 
WHERE bp.med_name_search LIKE '%adoless%';

SELECT 
mta.name AS medicamento, 
mta.why AS motivo 
FROM birth_pill bp 
JOIN birth_pill_medicine_to_avoid bpm ON bpm.birth_pill_id = bp.id 
JOIN medicine_to_avoid mta ON mta.id = bpm.medicine_to_avoid_id 
WHERE bp.med_name_search LIKE '%adoless%' ORDER BY mta.name;

SELECT 
active_days, 
placebo_days, 
pause_days, 
COUNT(*) AS total 
FROM birth_pill 
GROUP BY active_days, placebo_days, pause_days 
ORDER BY total DESC;

SELECT 
ai.name AS principio_ativo, 
COUNT(*) AS total_pilulas 
FROM active_ingredient ai 
JOIN birth_pill_ingredient bpi ON bpi.ingredient_id = ai.id 
GROUP BY ai.id 
ORDER BY total_pilulas DESC;

SELECT 
med_name 
FROM birth_pill 
WHERE miss_hours IS NULL 
ORDER BY med_name;

-- PARTE 2: consultas das tabelas do usuario (retornam vazio ate o app ser usado ou os dados de teste serem inseridos)
SELECT 
u.name, 
bp.med_name, 
pp.start_date 
FROM pill_pack pp 
JOIN users u ON u.id = pp.user_id 
LEFT JOIN birth_pill bp ON bp.id = pp.birth_pill_id 
WHERE pp.is_current = 1;

SELECT 
id, 
printf('%02d:%02d', hour, minute) AS horario, 
interval_minutes, 
repeat_limit 
FROM alarm WHERE active = 1 
ORDER BY hour, minute;

SELECT 
h.scheduled_at, 
h.confirmed_at, 
h.status, 
COALESCE(bp.med_name, 'Anticoncepcional') AS pilula 
FROM medication_history h 
JOIN pill_pack pp ON pp.id = h.pack_id 
LEFT JOIN birth_pill bp ON bp.id = pp.birth_pill_id 
ORDER BY h.scheduled_at DESC;

SELECT 
status, 
COUNT(*) AS total 
FROM medication_history 
GROUP BY status;

SELECT 
COUNT(*) AS registros_antigos 
FROM medication_history 
WHERE scheduled_at < date('now', '-3 months');

-- PARTE 3: dados de teste (rodar em uma copia do banco, nunca no arquivo cicli.db do projeto)
INSERT INTO users (name, birth_date, created_at) VALUES ('Teste', '01/01/2000', date('now'));
INSERT INTO pill_pack (user_id, birth_pill_id, active_days, placebo_days, pause_days, start_date, is_current, created_at) VALUES (1, (SELECT id FROM birth_pill WHERE med_name_search LIKE '%adoless%' LIMIT 1), 24, 4, 0, date('now', '-10 days'), 1, date('now'));
INSERT INTO alarm (user_id, hour, minute, interval_minutes, repeat_limit, active, created_at) VALUES (1, 8, 0, 5, 3, 1, date('now'));
INSERT INTO medication_history (user_id, pack_id, alarm_id, scheduled_at, confirmed_at, status, created_at) VALUES (1, 1, 1, datetime('now', '-2 days', 'start of day', '+8 hours'), datetime('now', '-2 days', 'start of day', '+8 hours', '+3 minutes'), 'TAKEN', date('now'));
INSERT INTO medication_history (user_id, pack_id, alarm_id, scheduled_at, confirmed_at, status, created_at) VALUES (1, 1, 1, datetime('now', '-1 days', 'start of day', '+8 hours'), NULL, 'NOT_TAKEN', date('now'));
