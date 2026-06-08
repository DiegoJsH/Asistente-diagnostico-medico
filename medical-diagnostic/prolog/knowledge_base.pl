% Knowledge Base - Medical Diagnostic System
:- encoding(utf8).

% Symptoms
sintoma(fiebre).
sintoma(tos).
sintoma(dolor_cabeza).
sintoma(congestion_nasal).
sintoma(dolor_garganta).
sintoma(erupciones_piel).
sintoma(vomitos).
sintoma(diarrea).
sintoma(fatiga).
sintoma(escalofrios).
sintoma(estornudos).
sintoma(cuerpo_adolorido).
sintoma(dolor_abdominal).   % Nuevo síntoma para Apendicitis
sintoma(dolor_articular).   % Nuevo síntoma para Dengue

% Diseases
enfermedad(gripe).
enfermedad(resfriado_comun).
enfermedad(faringitis).
enfermedad(gastroenteritis).
enfermedad(varicela).
enfermedad(alergia).
enfermedad(migrana).
enfermedad(dengue).         % Nueva enfermedad
enfermedad(apendicitis).     % Nueva enfermedad

% Helper: Count matching symptoms
count_matches([], _Sintomas, 0).
count_matches([H|T], Sintomas, Count) :-
    member(H, Sintomas),
    !,
    count_matches(T, Sintomas, Count1),
    Count is Count1 + 1.
count_matches([_H|T], Sintomas, Count) :-
    count_matches(T, Sintomas, Count).

% Diagnostic rules - Flu (fiebre, tos, fatiga, cuerpo_adolorido)
diagnostico(gripe, Sintomas) :-
    count_matches([fiebre, tos, fatiga, cuerpo_adolorido], Sintomas, Count),
    Count >= 3.

% Common cold (congestion_nasal, tos, estornudos)
diagnostico(resfriado_comun, Sintomas) :-
    count_matches([congestion_nasal, tos, estornudos], Sintomas, Count),
    Count >= 2.

% Pharyngitis (dolor_garganta, fiebre, dolor_cabeza)
diagnostico(faringitis, Sintomas) :-
    count_matches([dolor_garganta, fiebre, dolor_cabeza], Sintomas, Count),
    Count >= 2.

% Gastroenteritis (vomitos, diarrea, fatiga)
diagnostico(gastroenteritis, Sintomas) :-
    count_matches([vomitos, diarrea, fatiga], Sintomas, Count),
    Count >= 2.

% Chickenpox (erupciones_piel, fiebre, fatiga)
diagnostico(varicela, Sintomas) :-
    count_matches([erupciones_piel, fiebre, fatiga], Sintomas, Count),
    Count >= 2.

% Allergy (estornudos, congestion_nasal, erupciones_piel)
diagnostico(alergia, Sintomas) :-
    count_matches([estornudos, congestion_nasal, erupciones_piel], Sintomas, Count),
    Count >= 2.

% Migraine (dolor_cabeza, fatiga)
diagnostico(migrana, Sintomas) :-
    count_matches([dolor_cabeza, fatiga], Sintomas, Count),
    Count >= 1.

% Dengue (fiebre, dolor_cabeza, dolor_articular, fatiga)
diagnostico(dengue, Sintomas) :-
    count_matches([fiebre, dolor_cabeza, dolor_articular, fatiga], Sintomas, Count),
    Count >= 3.

% Apendicitis (dolor_abdominal, vomitos, fiebre)
diagnostico(apendicitis, Sintomas) :-
    count_matches([dolor_abdominal, vomitos, fiebre], Sintomas, Count),
    Count >= 2.


% Disease information (Tratamientos)
info(gripe, 'Requiere descanso, abundantes líquidos y paracetamol para los síntomas.').
info(resfriado_comun, 'Infección viral leve. Descanso y medicamentos para aliviar síntomas.').
info(faringitis, 'Posible infección bacteriana. Requiere evaluación médica para antibióticos.').
info(gastroenteritis, 'Mantener hidratación constante con suero. Consultar si persiste más de 2 días.').
info(varicela, 'Enfermedad contagiosa. Se recomienda aislamiento y consultar al médico.').
info(alergia, 'Evitar exposición al alérgeno. Considerar antihistamínicos de venta libre.').
info(migrana, 'Descansar en un ambiente oscuro y silencioso. Evitar pantallas.').
info(dengue, '¡Atención! Reposo absoluto, hidratación oral extrema. NO tomar Aspirina ni Ibuprofeno bajo ninguna circunstancia.').
info(apendicitis, '¡PELIGRO! Requiere evaluación quirúrgica inmediata. No ingerir alimentos, analgésicos ni usar compresas calientes.').


% Triaje de Gravedad (verde, amarillo, rojo)
triaje(resfriado_comun, verde).
triaje(alergia, verde).
triaje(gripe, amarillo).
triaje(migrana, amarillo).
triaje(faringitis, amarillo).
triaje(gastroenteritis, amarillo).
triaje(varicela, amarillo).
triaje(dengue, rojo).
triaje(apendicitis, rojo).


% Especialistas médicos recomendados
especialista(resfriado_comun, 'Médico General').
especialista(gripe, 'Médico General').
especialista(faringitis, 'Otorrinolaringólogo').
especialista(gastroenteritis, 'Gastroenterólogo').
especialista(varicela, 'Pediatra / Médico General').
especialista(alergia, 'Alergólogo').
especialista(migrana, 'Neurólogo').
especialista(dengue, 'Infectólogo / Emergencista').
especialista(apendicitis, 'Cirujano General / Emergencista').


% Get possible diagnoses
obtener_diagnosticos(Sintomas, Diagnosticos) :-
    findall(Enfermedad, diagnostico(Enfermedad, Sintomas), Diagnosticos).

% Get disease information
obtener_informacion(Enfermedad, Informacion) :-
    info(Enfermedad, Informacion).

% Get triaje level
obtener_triaje(Enfermedad, Nivel) :-
    triaje(Enfermedad, Nivel).

% Get specialist
obtener_especialista(Enfermedad, Doc) :-
    especialista(Enfermedad, Doc).

% Validate symptom
sintoma_valido(Sintoma) :-
    sintoma(Sintoma).