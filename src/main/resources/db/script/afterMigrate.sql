INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Norsk', 0);
INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Matte', 0);
INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Naturfag', 0);
INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Engelsk', 0);
INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Fysikk', 0);
INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('KRLE', 0);
INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Eksamensnerver', 1);
INSERT INTO SUBJECTS (subject, is_mestring) VALUES ('Stress', 1);

INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id)
    VALUES ('', 'Hva er 2+2?', 1, 10, 1, 2);
INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id)
    VALUES ('Dikt er vanskelig', 'How to dikt?', 1, 11, 5, 1);
INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id)
    VALUES ('', 'Stresset for eksamen...', 0, 12, 3, 1);
INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id)
    VALUES ('Norsk greier', 'How to sakprosa?', 1, 12, 4, 1);
INSERT INTO QUESTIONS (title, question_text, is_public, student_grade, state, subject_id)
    VALUES ('Mattespørsmål', 'Hva er 2*2?', 1, 12, 5, 2);

INSERT INTO ANSWERS (answer_text, question_id)
    VALUES ('Dikt i vei', 2);
INSERT INTO ANSWERS (answer_text, question_id)
    VALUES ('Godt spørsmål.', 4)
    ;
INSERT INTO ANSWERS (answer_text, question_id)
    VALUES ('4', 5);

 INSERT INTO FEEDBACK (question_id, feedback_text)
    VALUES (4, 'Det der er ikke et ordentlig svar');

INSERT INTO THEMES (theme, subject_id) VALUES ('Dikt', 1);
INSERT INTO THEMES (theme, subject_id) VALUES ('Nynorsk', 1);
INSERT INTO THEMES (theme, subject_id) VALUES ('Sakprosa', 1);
INSERT INTO THEMES (theme, subject_id) VALUES ('Analyse', 1);
INSERT INTO THEMES (theme, subject_id) VALUES ('Algebra', 2);
INSERT INTO THEMES (theme, subject_id) VALUES ('Konstruksjon', 2);
INSERT INTO THEMES (theme, subject_id) VALUES ('Brøk', 2);
INSERT INTO THEMES (theme, subject_id) VALUES ('Sannsynlighet', 2);
INSERT INTO THEMES (theme, subject_id) VALUES ('Derivasjon', 2);
INSERT INTO THEMES (theme, subject_id) VALUES ('Integraler', 2);
INSERT INTO THEMES (theme, subject_id) VALUES ('Darwin', 3);
INSERT INTO THEMES (theme, subject_id) VALUES ('Klimaendringer', 3);
INSERT INTO THEMES (theme, subject_id) VALUES ('Grammatikk', 4);
INSERT INTO THEMES (theme, subject_id) VALUES ('Kultur', 4);
INSERT INTO THEMES (theme, subject_id) VALUES ('Tyngdekraft', 5);
INSERT INTO THEMES (theme, subject_id) VALUES ('Relativitetsteori', 5);
INSERT INTO THEMES (theme, subject_id) VALUES ('Mekanikk', 5);
INSERT INTO THEMES (theme, subject_id) VALUES ('Bibelen', 6);
INSERT INTO THEMES (theme, subject_id) VALUES ('Filosofi', 6);

INSERT INTO QUESTION_THEMES (question_id, theme_id) VALUES (2, 1);

INSERT INTO VOLUNTEERS (id, name, bio_text, img_url, email, role) VALUES('429644d7-3b30-46ac-998b-b5a6a425378a', 'Sandra Skarshaug', '', '', 'ssk.capra@outlook.com', 'ADMIN');
