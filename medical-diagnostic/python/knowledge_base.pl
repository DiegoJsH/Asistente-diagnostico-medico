% Base de Conocimiento - Sistema de Diagnóstico Médico
:- encoding(utf8).

% ============================
% Síntomas
% ============================
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
sintoma(dolor_abdominal).
sintoma(dolor_articular).
% Nuevos síntomas
sintoma(dificultad_respirar).
sintoma(dolor_pecho).
sintoma(dificultad_tragar).
sintoma(perdida_olfato).
sintoma(perdida_apetito).
sintoma(acidez).
sintoma(sudoracion_nocturna).
sintoma(mareo).
sintoma(perdida_peso).
sintoma(picazon).
sintoma(hinchazon).
sintoma(sensibilidad_luz).
sintoma(dolor_orinar).
sintoma(sangre_orina).

% ============================
% Enfermedades
% ============================
enfermedad(gripe).
enfermedad(resfriado_comun).
enfermedad(faringitis).
enfermedad(gastroenteritis).
enfermedad(varicela).
enfermedad(alergia).
enfermedad(migrana).
enfermedad(dengue).
enfermedad(apendicitis).
% Nuevas enfermedades
enfermedad(sinusitis).
enfermedad(neumonia).
enfermedad(bronquitis).
enfermedad(amigdalitis).
enfermedad(covid19).
enfermedad(infeccion_urinaria).

% Auxiliar: contar síntomas coincidentes
count_matches([], _Sintomas, 0).
count_matches([H|T], Sintomas, Count) :-
    member(H, Sintomas),
    !,
    count_matches(T, Sintomas, Count1),
    Count is Count1 + 1.
count_matches([_H|T], Sintomas, Count) :-
    count_matches(T, Sintomas, Count).

% ============================
% Reglas de diagnóstico
% ============================

% Gripe (fiebre, tos, fatiga, cuerpo_adolorido)
diagnostico(gripe, Sintomas) :-
    count_matches([fiebre, tos, fatiga, cuerpo_adolorido], Sintomas, Count),
    Count >= 3.

% Resfriado común (congestion_nasal, tos, estornudos)
diagnostico(resfriado_comun, Sintomas) :-
    count_matches([congestion_nasal, tos, estornudos], Sintomas, Count),
    Count >= 2.

% Faringitis (dolor_garganta, fiebre, dolor_cabeza)
diagnostico(faringitis, Sintomas) :-
    count_matches([dolor_garganta, fiebre, dolor_cabeza], Sintomas, Count),
    Count >= 2.

% Gastroenteritis (vomitos, diarrea, fatiga)
diagnostico(gastroenteritis, Sintomas) :-
    count_matches([vomitos, diarrea, fatiga], Sintomas, Count),
    Count >= 2.

% Varicela (erupciones_piel, fiebre, fatiga)
diagnostico(varicela, Sintomas) :-
    count_matches([erupciones_piel, fiebre, fatiga], Sintomas, Count),
    Count >= 2.

% Alergia (estornudos, congestion_nasal, erupciones_piel)
diagnostico(alergia, Sintomas) :-
    count_matches([estornudos, congestion_nasal, erupciones_piel], Sintomas, Count),
    Count >= 2.

% Migraña (dolor_cabeza, fatiga)
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

% Sinusitis (congestion_nasal, dolor_cabeza, dolor_garganta, fiebre)
diagnostico(sinusitis, Sintomas) :-
    count_matches([congestion_nasal, dolor_cabeza, dolor_garganta, fiebre], Sintomas, Count),
    Count >= 3.

% Neumonía (fiebre, tos, dificultad_respirar, dolor_pecho, fatiga)
diagnostico(neumonia, Sintomas) :-
    count_matches([fiebre, tos, dificultad_respirar, dolor_pecho, fatiga], Sintomas, Count),
    Count >= 3.

% Bronquitis (tos, fatiga, dificultad_respirar, dolor_pecho)
diagnostico(bronquitis, Sintomas) :-
    count_matches([tos, fatiga, dificultad_respirar, dolor_pecho], Sintomas, Count),
    Count >= 2.

% Amigdalitis (dolor_garganta, fiebre, dificultad_tragar)
diagnostico(amigdalitis, Sintomas) :-
    count_matches([dolor_garganta, fiebre, dificultad_tragar], Sintomas, Count),
    Count >= 2.

% COVID-19 (fiebre, tos, fatiga, perdida_olfato)
diagnostico(covid19, Sintomas) :-
    count_matches([fiebre, tos, fatiga, perdida_olfato], Sintomas, Count),
    Count >= 3.

% Infección urinaria (dolor_orinar, sangre_orina, fiebre)
diagnostico(infeccion_urinaria, Sintomas) :-
    count_matches([dolor_orinar, sangre_orina, fiebre], Sintomas, Count),
    Count >= 2.


% ============================
% Información de la enfermedad (recomendaciones y tratamiento)
% ============================
info(gripe, 'Se recomienda reposo absoluto, hidratación abundante (agua e infusiones) y paracetamol para bajar la fiebre y aliviar el malestar general. Evitar el contacto cercano con otras personas durante los primeros días para prevenir contagios.').
info(resfriado_comun, 'Infección viral leve de las vías respiratorias altas. Se recomienda descanso, líquidos tibios, lavados nasales con solución salina y analgésicos de venta libre si hay molestias. Suele resolverse en 7 a 10 días.').
info(faringitis, 'Inflamación de la garganta que puede ser viral o bacteriana. Se recomienda hacer gárgaras con agua tibia y sal, tomar líquidos abundantes y evitar irritantes como el humo. Si los síntomas persisten más de 3 días o hay fiebre alta, se requiere evaluación médica para descartar infección bacteriana.').
info(gastroenteritis, 'Mantener una hidratación constante con sales de rehidratación oral para reponer líquidos y electrolitos. Seguir una dieta blanda (arroz, plátano, manzana, tostadas) y evitar lácteos y alimentos grasos. Consultar a un médico si los síntomas persisten más de 48 horas.').
info(varicela, 'Enfermedad viral altamente contagiosa. Se recomienda aislamiento hasta que todas las lesiones formen costra, mantener las uñas cortas para evitar rascado e infecciones secundarias, y usar antihistamínicos para reducir la picazón. Evitar el uso de aspirina en niños.').
info(alergia, 'Evitar la exposición al alérgeno identificado (polvo, polen, ácaros, etc.). Los antihistamínicos de venta libre pueden aliviar los síntomas. Si las reacciones son frecuentes o severas, se recomienda realizar pruebas de alergia con un especialista.').
info(migrana, 'Descansar en un ambiente oscuro, silencioso y fresco, evitando pantallas y ruidos fuertes. Identificar y evitar los desencadenantes personales (estrés, ciertos alimentos, falta de sueño). Los analgésicos comunes pueden ayudar en episodios leves.').
info(dengue, '¡Atención! Se requiere reposo absoluto e hidratación oral extrema. NO tomar Aspirina ni Ibuprofeno bajo ninguna circunstancia, ya que aumentan el riesgo de sangrado; usar únicamente paracetamol para la fiebre. Acudir de inmediato a un centro médico ante sangrado, dolor abdominal intenso o vómitos persistentes.').
info(apendicitis, '¡PELIGRO! Requiere evaluación quirúrgica inmediata en un servicio de emergencias. No ingerir alimentos, analgésicos ni usar compresas calientes sobre el abdomen, ya que pueden enmascarar los síntomas o complicar el cuadro.').
info(sinusitis, 'Se recomienda realizar lavados nasales con solución salina, aplicar vapor de agua caliente para descongestionar y mantener buena hidratación. Si los síntomas persisten más de 10 días o empeoran, puede requerirse tratamiento con antibióticos recetados por un médico.').
info(neumonia, '¡Atención! Requiere evaluación médica urgente, ya que puede necesitar tratamiento con antibióticos y, en casos severos, hospitalización. Se recomienda reposo, hidratación y vigilar la dificultad para respirar. Acudir de inmediato a emergencias si hay labios morados o falta de aire severa.').
info(bronquitis, 'Se recomienda reposo, hidratación abundante para fluidificar las secreciones y evitar el humo del cigarrillo o contaminantes. Consultar a un médico si la tos persiste más de 3 semanas o hay dificultad para respirar.').
info(amigdalitis, 'Se recomienda hacer gárgaras con agua tibia y sal, tomar líquidos fríos para aliviar el dolor y evitar alimentos irritantes. Si hay placas de pus, fiebre alta o dificultad para tragar, es necesaria una evaluación médica para descartar infección bacteriana.').
info(covid19, 'Se recomienda aislamiento inmediato, monitoreo de la respiración y reposo. Realizar una prueba diagnóstica (PCR o antígeno) para confirmar el contagio. Acudir a emergencias si aparece dificultad respiratoria marcada, dolor en el pecho o confusión.').
info(infeccion_urinaria, 'Se recomienda aumentar el consumo de agua para ayudar a eliminar la bacteria, evitar retener la orina y mantener buena higiene íntima. Es necesaria una evaluación médica para confirmar el diagnóstico con un examen de orina y, de ser necesario, iniciar tratamiento antibiótico.').


% ============================
% Triaje de gravedad (verde, amarillo, rojo)
% ============================
triaje(resfriado_comun, verde).
triaje(alergia, verde).
triaje(gripe, amarillo).
triaje(migrana, amarillo).
triaje(faringitis, amarillo).
triaje(gastroenteritis, amarillo).
triaje(varicela, amarillo).
triaje(sinusitis, amarillo).
triaje(bronquitis, amarillo).
triaje(amigdalitis, amarillo).
triaje(covid19, amarillo).
triaje(infeccion_urinaria, amarillo).
triaje(dengue, rojo).
triaje(apendicitis, rojo).
triaje(neumonia, rojo).


% ============================
% Especialistas medicos recomendados
% ============================
especialista(resfriado_comun, 'Médico General').
especialista(gripe, 'Médico General').
especialista(faringitis, 'Otorrinolaringólogo (especialista en oído, nariz y garganta)').
especialista(gastroenteritis, 'Gastroenterólogo (especialista en sistema digestivo)').
especialista(varicela, 'Pediatra / Médico General').
especialista(alergia, 'Alergólogo (especialista en alergias e inmunología)').
especialista(migrana, 'Neurólogo (especialista en sistema nervioso)').
especialista(dengue, 'Infectólogo / Médico de Emergencias').
especialista(apendicitis, 'Cirujano General / Médico de Emergencias').
especialista(sinusitis, 'Otorrinolaringólogo (especialista en oído, nariz y garganta)').
especialista(neumonia, 'Neumólogo (especialista en pulmones) / Médico de Emergencias').
especialista(bronquitis, 'Neumólogo (especialista en pulmones)').
especialista(amigdalitis, 'Otorrinolaringólogo (especialista en oído, nariz y garganta)').
especialista(covid19, 'Infectólogo (especialista en enfermedades infecciosas)').
especialista(infeccion_urinaria, 'Urólogo (especialista en sistema urinario)').


% ============================
% Consultas expuestas al modulo Python
% ============================

% Obtener diagnósticos posibles
obtener_diagnosticos(Sintomas, Diagnosticos) :-
    findall(Enfermedad, diagnostico(Enfermedad, Sintomas), Diagnosticos).

% Obtener información de la enfermedad
obtener_informacion(Enfermedad, Informacion) :-
    info(Enfermedad, Informacion).

% Obtener nivel de triaje
obtener_triaje(Enfermedad, Nivel) :-
    triaje(Enfermedad, Nivel).

% Obtener especialista
obtener_especialista(Enfermedad, Doc) :-
    especialista(Enfermedad, Doc).

% Validar síntoma
sintoma_valido(Sintoma) :-
    sintoma(Sintoma).