USE cca;

-- ============================================================
-- QUESTIONS — VERBAL (10)
-- ============================================================
INSERT INTO t_question (str_uuid, str_domain_code, em_difficulty, em_question_type, em_content_type, str_question_text, str_explanation, str_hint, int_point_value, int_time_limit_ms, db_avg_correct_rate, db_avg_response_ms, b_active, b_verified, int_report_count, dt_created, dt_updated) VALUES
('v-001', 'VERBAL', 'EASY',   'WORD_ANALOGY',   'TEXT', 'BIRD is to SKY as FISH is to:', 'Birds live in the sky, fish live in water.', 'Think about the natural habitat.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('v-002', 'VERBAL', 'EASY',   'MULTIPLE_CHOICE','TEXT', 'Which word is closest in meaning to BENEVOLENT?', 'Benevolent means well-meaning and kindly.', 'Think of a synonym for kind.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('v-003', 'VERBAL', 'EASY',   'WORD_ANALOGY',   'TEXT', 'AUTHOR is to BOOK as COMPOSER is to:', 'An author writes a book, a composer writes music.', 'What does a composer create?', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('v-004', 'VERBAL', 'MEDIUM', 'MULTIPLE_CHOICE','TEXT', 'Which word is the ODD ONE OUT: Serene, Tranquil, Placid, Agitated?', 'Serene, Tranquil and Placid all mean calm. Agitated means disturbed.', 'Look for the word with the opposite meaning.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('v-005', 'VERBAL', 'MEDIUM', 'WORD_ANALOGY',   'TEXT', 'FRUGAL is to WASTEFUL as TIMID is to:', 'Frugal is the opposite of wasteful. Timid is the opposite of bold.', 'Find the antonym relationship.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('v-006', 'VERBAL', 'MEDIUM', 'MULTIPLE_CHOICE','TEXT', 'Choose the word most similar to EPHEMERAL:', 'Ephemeral means lasting for a very short time, similar to transient.', 'Think about duration.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('v-007', 'VERBAL', 'MEDIUM', 'WORD_ANALOGY',   'TEXT', 'PAINTING is to GALLERY as BOOK is to:', 'Paintings are displayed in galleries, books are stored in libraries.', 'Where are books kept?', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('v-008', 'VERBAL', 'HARD',   'MULTIPLE_CHOICE','TEXT', 'Which word is closest in meaning to LOQUACIOUS?', 'Loquacious means tending to talk a great deal.', 'This word relates to speech.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('v-009', 'VERBAL', 'HARD',   'WORD_ANALOGY',   'TEXT', 'PAUCITY is to ABUNDANCE as ENMITY is to:', 'Paucity (scarcity) is opposite to abundance. Enmity (hostility) is opposite to friendship.', 'Look for antonym pairs.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('v-010', 'VERBAL', 'HARD',   'MULTIPLE_CHOICE','TEXT', 'Which word is the ODD ONE OUT: Magnanimous, Petty, Generous, Charitable?', 'Magnanimous, Generous and Charitable all mean giving. Petty means small-minded.', 'One word has a negative connotation.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW());

-- ============================================================
-- QUESTIONS — NUMERICAL (10)
-- ============================================================
INSERT INTO t_question (str_uuid, str_domain_code, em_difficulty, em_question_type, em_content_type, str_question_text, str_explanation, str_hint, int_point_value, int_time_limit_ms, db_avg_correct_rate, db_avg_response_ms, b_active, b_verified, int_report_count, dt_created, dt_updated) VALUES
('n-001', 'NUMERICAL', 'EASY',   'NUMBER_SERIES',  'TEXT', 'What is the next number? 2, 4, 6, 8, __', 'Each number increases by 2.', 'Look at the difference between consecutive numbers.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('n-002', 'NUMERICAL', 'EASY',   'MULTIPLE_CHOICE','TEXT', 'A shirt costs $45. After a 20% discount, what is the new price?', '20% of $45 = $9. $45 - $9 = $36.', 'Calculate the discount amount first.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('n-003', 'NUMERICAL', 'EASY',   'NUMBER_SERIES',  'TEXT', 'What is the next number? 1, 4, 9, 16, __', 'These are perfect squares: 1²=1, 2²=4, 3²=9, 4²=16, 5²=25.', 'Think about square numbers.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('n-004', 'NUMERICAL', 'MEDIUM', 'NUMBER_SERIES',  'TEXT', 'What is the next number? 3, 6, 12, 24, __', 'Each number is multiplied by 2.', 'Look for a multiplication pattern.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('n-005', 'NUMERICAL', 'MEDIUM', 'MULTIPLE_CHOICE','TEXT', 'If a car travels 150 km in 2.5 hours, what is its average speed?', 'Speed = Distance ÷ Time = 150 ÷ 2.5 = 60 km/h.', 'Use the formula Speed = Distance / Time.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('n-006', 'NUMERICAL', 'MEDIUM', 'NUMBER_SERIES',  'TEXT', 'What is the next number? 1, 1, 2, 3, 5, 8, __', 'This is the Fibonacci sequence: each number = sum of the previous two.', 'Add the last two numbers.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('n-007', 'NUMERICAL', 'MEDIUM', 'MULTIPLE_CHOICE','TEXT', 'A store sells 3 items for $7.50. How much do 8 items cost?', '1 item = $7.50 ÷ 3 = $2.50. 8 items = 8 × $2.50 = $20.', 'Find the unit price first.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('n-008', 'NUMERICAL', 'HARD',   'NUMBER_SERIES',  'TEXT', 'What is the next number? 2, 5, 10, 17, 26, __', 'Differences: 3, 5, 7, 9, 11 — odd numbers increasing by 2. Next: 26+11=37.', 'Look at the differences between numbers.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('n-009', 'NUMERICAL', 'HARD',   'MULTIPLE_CHOICE','TEXT', 'If 40% of X is 60, what is 25% of X?', '40% of X = 60 → X = 150. 25% of 150 = 37.5.', 'Find X first, then calculate 25%.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('n-010', 'NUMERICAL', 'HARD',   'NUMBER_SERIES',  'TEXT', 'What is the next number? 81, 27, 9, 3, __', 'Each number is divided by 3.', 'Look for a division pattern.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW());

-- ============================================================
-- QUESTIONS — SPATIAL (10)
-- ============================================================
INSERT INTO t_question (str_uuid, str_domain_code, em_difficulty, em_question_type, em_content_type, str_question_text, str_explanation, str_hint, int_point_value, int_time_limit_ms, db_avg_correct_rate, db_avg_response_ms, b_active, b_verified, int_report_count, dt_created, dt_updated) VALUES
('s-001', 'SPATIAL', 'EASY',   'MULTIPLE_CHOICE','TEXT', 'How many sides does a hexagon have?', 'A hexagon has 6 sides. Hex = 6 in Greek.', 'Think of the prefix hex-.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('s-002', 'SPATIAL', 'EASY',   'MULTIPLE_CHOICE','TEXT', 'A square piece of paper is folded in half twice. How many layers are there?', 'First fold: 2 layers. Second fold: 4 layers.', 'Each fold doubles the number of layers.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('s-003', 'SPATIAL', 'EASY',   'MULTIPLE_CHOICE','TEXT', 'Which shape has exactly 3 sides and 3 angles?', 'A triangle has 3 sides and 3 angles.', 'Tri = 3.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('s-004', 'SPATIAL', 'MEDIUM', 'MULTIPLE_CHOICE','TEXT', 'A cube has how many edges?', 'A cube has 12 edges (4 on top face, 4 on bottom face, 4 vertical).', 'Count the edges of each face systematically.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('s-005', 'SPATIAL', 'MEDIUM', 'MULTIPLE_CHOICE','TEXT', 'If you rotate the letter "d" 180 degrees, what letter does it resemble?', 'Rotating "d" 180° gives "p".', 'Imagine physically rotating the letter.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('s-006', 'SPATIAL', 'MEDIUM', 'MULTIPLE_CHOICE','TEXT', 'How many faces does a triangular prism have?', 'A triangular prism has 2 triangular faces and 3 rectangular faces = 5 faces.', 'Count each type of face separately.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('s-007', 'SPATIAL', 'MEDIUM', 'MULTIPLE_CHOICE','TEXT', 'Which of these is the mirror image of the letter "b"?', 'The mirror image of "b" is "d".', 'Flip the letter horizontally.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('s-008', 'SPATIAL', 'HARD',   'MULTIPLE_CHOICE','TEXT', 'A 3x3x3 cube is painted on the outside and then cut into 27 small cubes. How many small cubes have exactly 2 painted faces?', 'Edge cubes (not corners) have 2 painted faces. A 3x3x3 cube has 12 edges × 1 middle cube each = 12.', 'Focus on the cubes on the edges but not the corners.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('s-009', 'SPATIAL', 'HARD',   'MULTIPLE_CHOICE','TEXT', 'If a clock shows 3:15, what is the angle between the hour and minute hands?', 'At 3:15: minute hand at 90°, hour hand at 97.5° (3h×30° + 15min×0.5°). Difference = 7.5°.', 'Remember the hour hand moves 0.5° per minute.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW()),
('s-010', 'SPATIAL', 'HARD',   'MULTIPLE_CHOICE','TEXT', 'How many cubes are needed to build a 4x4x4 solid cube?', '4 × 4 × 4 = 64 small cubes.', 'Multiply the dimensions together.', 1, 18000, 0, 0, 1, 1, 0, NOW(), NOW());

-- ============================================================
-- ANSWERS — VERBAL
-- ============================================================
-- v-001: BIRD/SKY → FISH/?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-001'), 'Water', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-001'), 'Land',  'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-001'), 'Sand',  'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-001'), 'River', 'D', 0, 4, 0, NOW(), NOW());

-- v-002: BENEVOLENT synonym
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-002'), 'Kind',       'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-002'), 'Hostile',    'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-002'), 'Indifferent','C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-002'), 'Selfish',    'D', 0, 4, 0, NOW(), NOW());

-- v-003: COMPOSER/?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-003'), 'Symphony', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-003'), 'Canvas',   'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-003'), 'Stage',    'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-003'), 'Novel',    'D', 0, 4, 0, NOW(), NOW());

-- v-004: ODD ONE OUT
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-004'), 'Agitated', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-004'), 'Serene',   'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-004'), 'Tranquil', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-004'), 'Placid',   'D', 0, 4, 0, NOW(), NOW());

-- v-005: TIMID/?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-005'), 'Bold',    'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-005'), 'Shy',     'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-005'), 'Anxious', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-005'), 'Weak',    'D', 0, 4, 0, NOW(), NOW());

-- v-006: EPHEMERAL synonym
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-006'), 'Transient', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-006'), 'Eternal',   'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-006'), 'Solid',     'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-006'), 'Ancient',   'D', 0, 4, 0, NOW(), NOW());

-- v-007: BOOK/?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-007'), 'Library',   'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-007'), 'Museum',    'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-007'), 'Bookstore', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-007'), 'School',    'D', 0, 4, 0, NOW(), NOW());

-- v-008: LOQUACIOUS
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-008'), 'Talkative', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-008'), 'Silent',    'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-008'), 'Clever',    'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-008'), 'Grumpy',    'D', 0, 4, 0, NOW(), NOW());

-- v-009: ENMITY/?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-009'), 'Friendship', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-009'), 'Hatred',     'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-009'), 'Conflict',   'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-009'), 'Rivalry',    'D', 0, 4, 0, NOW(), NOW());

-- v-010: ODD ONE OUT
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='v-010'), 'Petty',        'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-010'), 'Magnanimous',  'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-010'), 'Generous',     'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='v-010'), 'Charitable',   'D', 0, 4, 0, NOW(), NOW());

-- ============================================================
-- ANSWERS — NUMERICAL
-- ============================================================
-- n-001: 2,4,6,8,?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-001'), '10', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-001'), '9',  'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-001'), '12', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-001'), '11', 'D', 0, 4, 0, NOW(), NOW());

-- n-002: 20% discount on $45
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-002'), '$36', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-002'), '$35', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-002'), '$40', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-002'), '$38', 'D', 0, 4, 0, NOW(), NOW());

-- n-003: 1,4,9,16,?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-003'), '25', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-003'), '20', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-003'), '18', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-003'), '24', 'D', 0, 4, 0, NOW(), NOW());

-- n-004: 3,6,12,24,?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-004'), '48', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-004'), '36', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-004'), '40', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-004'), '30', 'D', 0, 4, 0, NOW(), NOW());

-- n-005: speed 150km/2.5h
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-005'), '60 km/h', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-005'), '50 km/h', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-005'), '75 km/h', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-005'), '55 km/h', 'D', 0, 4, 0, NOW(), NOW());

-- n-006: Fibonacci 1,1,2,3,5,8,?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-006'), '13', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-006'), '11', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-006'), '16', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-006'), '12', 'D', 0, 4, 0, NOW(), NOW());

-- n-007: 3 items for $7.50, 8 items?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-007'), '$20.00', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-007'), '$18.00', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-007'), '$22.50', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-007'), '$16.00', 'D', 0, 4, 0, NOW(), NOW());

-- n-008: 2,5,10,17,26,?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-008'), '37', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-008'), '35', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-008'), '38', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-008'), '33', 'D', 0, 4, 0, NOW(), NOW());

-- n-009: 40% of X = 60, find 25% of X
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-009'), '37.5', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-009'), '40',   'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-009'), '35',   'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-009'), '30',   'D', 0, 4, 0, NOW(), NOW());

-- n-010: 81,27,9,3,?
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='n-010'), '1',  'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-010'), '0',  'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-010'), '2',  'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='n-010'), '6',  'D', 0, 4, 0, NOW(), NOW());

-- ============================================================
-- ANSWERS — SPATIAL
-- ============================================================
-- s-001: hexagon sides
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-001'), '6', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-001'), '5', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-001'), '8', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-001'), '7', 'D', 0, 4, 0, NOW(), NOW());

-- s-002: paper folded twice
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-002'), '4', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-002'), '2', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-002'), '6', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-002'), '8', 'D', 0, 4, 0, NOW(), NOW());

-- s-003: triangle
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-003'), 'Triangle', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-003'), 'Square',   'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-003'), 'Pentagon', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-003'), 'Rhombus',  'D', 0, 4, 0, NOW(), NOW());

-- s-004: cube edges
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-004'), '12', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-004'), '8',  'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-004'), '6',  'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-004'), '10', 'D', 0, 4, 0, NOW(), NOW());

-- s-005: d rotated 180°
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-005'), 'p', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-005'), 'b', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-005'), 'q', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-005'), 'g', 'D', 0, 4, 0, NOW(), NOW());

-- s-006: triangular prism faces
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-006'), '5', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-006'), '4', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-006'), '6', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-006'), '3', 'D', 0, 4, 0, NOW(), NOW());

-- s-007: mirror of b
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-007'), 'd', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-007'), 'p', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-007'), 'q', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-007'), 'g', 'D', 0, 4, 0, NOW(), NOW());

-- s-008: 3x3x3 cube, 2 painted faces
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-008'), '12', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-008'), '8',  'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-008'), '6',  'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-008'), '24', 'D', 0, 4, 0, NOW(), NOW());

-- s-009: clock 3:15 angle
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-009'), '7.5°',  'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-009'), '0°',    'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-009'), '15°',   'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-009'), '90°',   'D', 0, 4, 0, NOW(), NOW());

-- s-010: 4x4x4 cube
INSERT INTO t_answer (lg_question_id, str_answer_text, str_answer_label, b_is_correct, int_sort_order, int_chosen_count, dt_created, dt_updated) VALUES
((SELECT lg_id FROM t_question WHERE str_uuid='s-010'), '64', 'A', 1, 1, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-010'), '48', 'B', 0, 2, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-010'), '32', 'C', 0, 3, 0, NOW(), NOW()),
((SELECT lg_id FROM t_question WHERE str_uuid='s-010'), '16', 'D', 0, 4, 0, NOW(), NOW());
