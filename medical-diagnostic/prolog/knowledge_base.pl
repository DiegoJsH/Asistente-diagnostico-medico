% Knowledge Base - Medical Diagnostic System
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

% Diseases
enfermedad(gripe).
enfermedad(resfriado_comun).
enfermedad(faringitis).
enfermedad(gastroenteritis).
enfermedad(varicela).
enfermedad(alergia).
enfermedad(migrana).

% Helper: Count matching symptoms
count_matches([], _Sintomas, 0).
count_matches([H|T], Sintomas, Count) :-
    member(H, Sintomas),
    !,
    count_matches(T, Sintomas, Count1),
    Count is Count1 + 1.
count_matches([_H|T], Sintomas, Count) :-
    count_matches(T, Sintomas, Count).

% Diagnostic rules - Flu (fiebre, tos, fatiga, cuerpo_adolorido = 4 main symptoms)
diagnostico(gripe, Sintomas) :-
    count_matches([fiebre, tos, fatiga, cuerpo_adolorido], Sintomas, Count),
    Count >= 3.

% Common cold (congestion_nasal, tos, estornudos = 3 main symptoms)
diagnostico(resfriado_comun, Sintomas) :-
    count_matches([congestion_nasal, tos, estornudos], Sintomas, Count),
    Count >= 2.

% Pharyngitis (dolor_garganta, fiebre, dolor_cabeza = 3 main symptoms)
diagnostico(faringitis, Sintomas) :-
    count_matches([dolor_garganta, fiebre, dolor_cabeza], Sintomas, Count),
    Count >= 2.

% Gastroenteritis (vomitos, diarrea, fatiga = 3 main symptoms)
diagnostico(gastroenteritis, Sintomas) :-
    count_matches([vomitos, diarrea, fatiga], Sintomas, Count),
    Count >= 2.

% Chickenpox (erupciones_piel, fiebre, fatiga = 3 main symptoms)
diagnostico(varicela, Sintomas) :-
    count_matches([erupciones_piel, fiebre, fatiga], Sintomas, Count),
    Count >= 2.

% Allergy (estornudos, congestion_nasal, erupciones_piel = 3 main symptoms)
diagnostico(alergia, Sintomas) :-
    count_matches([estornudos, congestion_nasal, erupciones_piel], Sintomas, Count),
    Count >= 2.

% Migraine (dolor_cabeza, fatiga = 2 main symptoms)
diagnostico(migrana, Sintomas) :-
    count_matches([dolor_cabeza, fatiga], Sintomas, Count),
    Count >= 1.

% Disease information
info(gripe, 'Influenza. Requires rest, fluids and antivirals.').
info(resfriado_comun, 'Common cold. Rest and symptom medications.').
info(faringitis, 'Pharyngitis. Requires antibiotics. Consult a doctor.').
info(gastroenteritis, 'Gastroenteritis. Stay hydrated. Consult if persists.').
info(varicela, 'Chickenpox. Isolation recommended. Consult your doctor.').
info(alergia, 'Allergic reaction. Avoid allergens. Consider antihistamines.').
info(migrana, 'Migraine. Rest in dark and quiet environment recommended.').

% Get possible diagnoses
obtener_diagnosticos(Sintomas, Diagnosticos) :-
    findall(Enfermedad, diagnostico(Enfermedad, Sintomas), Diagnosticos).

% Get disease information
obtener_informacion(Enfermedad, Informacion) :-
    info(Enfermedad, Informacion).

% Validate symptom
sintoma_valido(Sintoma) :-
    sintoma(Sintoma).
