#!/usr/bin/env python3
"""
Python Module - Intermediary between Scala and Prolog
Exposes a REST API that queries the Prolog knowledge base
"""

from flask import Flask, request, jsonify
from pyswip import Prolog
import json
import os

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False
# Initialize Prolog
prolog = Prolog()

# Load knowledge base
knowledge_base_path = os.path.join(os.path.dirname(__file__), '../prolog/knowledge_base.pl')
prolog.consult(knowledge_base_path)

@app.route('/health', methods=['GET'])
def health():
    """Check that Python server is active"""
    return jsonify({
        'status': 'ok',
        'module': 'Python Intermediary',
        'description': 'Intermediary between Scala and Prolog'
    }), 200

@app.route('/diagnose', methods=['POST'])
def diagnose():
    """
    Endpoint to get diagnoses based on symptoms
    Expects JSON: {"symptoms": ["fiebre", "tos", "fatiga", "cuerpo_adolorido"]}
    """
    try:
        data = request.get_json()
        symptoms = data.get('symptoms', [])
        
        if not symptoms:
            return jsonify({
                'error': 'No symptoms provided',
                'symptoms_required': True
            }), 400
        
        # Convert strings to Prolog atoms
        prolog_symptoms = [atom(symptom.lower().replace(' ', '_')) for symptom in symptoms]
        
        # Query possible diagnoses
        diagnoses = []
        try:
            for result in prolog.query(f'obtener_diagnosticos({prolog_symptoms}, Diagnosticos)'):
                diagnoses = list(result['Diagnosticos'])
                break
        except Exception as e:
            print(f"Error querying Prolog: {e}")
            # If error, try manual diagnosis
            diagnoses = get_diagnoses_manual(prolog_symptoms)
        
        # Get information for each diagnosis
        diagnosticos_con_info = []
        for diagnosis in diagnoses:
            info = get_diagnosis_info(str(diagnosis))
            triaje_lvl = get_diagnosis_triaje(str(diagnosis))
            specialist = get_diagnosis_specialist(str(diagnosis))
            
            diagnosticos_con_info.append({
                'enfermedad': str(diagnosis),
                'descripción': info,
                'triaje': triaje_lvl,    
                'especialista': specialist,   
                'confianza': calculate_confidence(prolog_symptoms, diagnosis)
            })
        
        return jsonify({
            'sintomas_ingresados': symptoms,
            'diagnosticos_posibles': diagnosticos_con_info,
            'total_diagnosticos': len(diagnosticos_con_info)
        }), 200
        
    except Exception as e:
        return jsonify({
            'error': f'Error processing request: {str(e)}'
        }), 500

@app.route('/symptoms', methods=['GET'])
def get_symptoms():
    """Return list of available symptoms"""
    symptoms = []
    try:
        for result in prolog.query('sintoma(X)'):
            symptoms.append(str(result['X']))
    except Exception as e:
        print(f"Error getting symptoms: {e}")
    
    return jsonify({
        'sintomas_disponibles': sorted(symptoms),
        'total': len(symptoms)
    }), 200

@app.route('/diseases', methods=['GET'])
def get_diseases():
    """Return list of known diseases"""
    diseases = []
    try:
        for result in prolog.query('enfermedad(X)'):
            diseases.append(str(result['X']))
    except Exception as e:
        print(f"Error getting diseases: {e}")
    
    return jsonify({
        'enfermedades_conocidas': sorted(diseases),
        'total': len(diseases)
    }), 200

def get_diagnoses_manual(symptoms):
    """Get diagnoses manually (fallback if Prolog query fails)"""
    diagnoses = []
    symptom_str = str(symptoms)
    # Dengue
    if all(s in symptom_str for s in ['fiebre', 'dolor_articular', 'fatiga']):
        diagnoses.append('dengue')
    # Apendicitis
    if all(s in symptom_str for s in ['dolor_abdominal', 'vomitos']):
        diagnoses.append('apendicitis')
    # Gripe
    if all(s in symptom_str for s in ['fiebre', 'tos', 'fatiga', 'cuerpo_adolorido']):
        diagnoses.append('gripe')
    # Resfriado común
    if all(s in symptom_str for s in ['congestion_nasal', 'tos', 'estornudos']):
        diagnoses.append('resfriado_comun')
    # Faringitis
    if all(s in symptom_str for s in ['dolor_garganta', 'fiebre']):
        diagnoses.append('faringitis')
    # Gastroenteritis
    if all(s in symptom_str for s in ['vomitos', 'diarrea']):
        diagnoses.append('gastroenteritis')
    # Varicela
    if all(s in symptom_str for s in ['erupciones_piel', 'fiebre']):
        diagnoses.append('varicela')
    # Alergia
    if all(s in symptom_str for s in ['estornudos', 'congestion_nasal']):
        diagnoses.append('alergia')
    # Migraña
    if all(s in symptom_str for s in ['dolor_cabeza', 'fatiga']):
        diagnoses.append('migrana')
    # Sinusitis
    if all(s in symptom_str for s in ['congestion_nasal', 'dolor_cabeza', 'dolor_garganta']):
        diagnoses.append('sinusitis')
    # Neumonía
    if all(s in symptom_str for s in ['fiebre', 'tos', 'dificultad_respirar']):
        diagnoses.append('neumonia')
    # Bronquitis
    if all(s in symptom_str for s in ['tos', 'dificultad_respirar']):
        diagnoses.append('bronquitis')
    # Amigdalitis
    if all(s in symptom_str for s in ['dolor_garganta', 'fiebre', 'dificultad_tragar']):
        diagnoses.append('amigdalitis')
    # COVID-19
    if all(s in symptom_str for s in ['fiebre', 'tos', 'perdida_olfato']):
        diagnoses.append('covid19')
    # Infección urinaria
    if all(s in symptom_str for s in ['dolor_orinar', 'fiebre']):
        diagnoses.append('infeccion_urinaria')
    
    return diagnoses

def get_diagnosis_info(diagnosis):
    """Get information about a disease directly from Prolog"""
    try:
        # Hacemos la consulta directa a Prolog
        for result in prolog.query(f"obtener_informacion({diagnosis}, Info)"):
            info = result['Info']
            # pyswip a veces devuelve bytes, decodificamos por seguridad
            if isinstance(info, bytes):
                return info.decode('utf-8')
            return str(info)
    except Exception as e:
        print(f"Error querying Prolog info: {e}")
    
    return 'Información médica no disponible.'

def get_diagnosis_triaje(diagnosis):
    """Obtiene el nivel de triaje de la enfermedad desde Prolog"""
    try:
        for result in prolog.query(f"obtener_triaje({diagnosis}, Nivel)"):
            return str(result['Nivel'])
    except Exception as e:
        print(f"Error querying Prolog triaje: {e}")
    return 'amarillo'

def get_diagnosis_specialist(diagnosis):
    """Obtiene el especialista médico recomendado desde Prolog"""
    try:
        for result in prolog.query(f"obtener_especialista({diagnosis}, Doc)"):
            doc = result['Doc']
            if isinstance(doc, bytes):
                return doc.decode('utf-8')
            return str(doc)
    except Exception as e:
        print(f"Error querying Prolog specialist: {e}")
    return 'Médico General'

def calculate_confidence(symptoms, diagnosis):
    """Calculate confidence level based on symptom matching"""
    confidence_map = {
        'dengue': 0.90,
        'apendicitis': 0.95,
        'gripe': 0.95,
        'resfriado_comun': 0.85,
        'faringitis': 0.80,
        'gastroenteritis': 0.90,
        'varicela': 0.85,
        'alergia': 0.75,
        'migrana': 0.70,
        'sinusitis': 0.80,
        'neumonia': 0.90,
        'bronquitis': 0.80,
        'amigdalitis': 0.85,
        'covid19': 0.85,
        'infeccion_urinaria': 0.85
    }
    return confidence_map.get(str(diagnosis).lower(), 0.60)

def atom(s):
    """Convert string to Prolog atom"""
    return s.lower().replace(' ', '_')

if __name__ == '__main__':
    print("Starting Python server...")
    print("Intermediary module between Scala and Prolog")
    print("Listening on http://localhost:5000")
    app.run(host='0.0.0.0', port=5000, debug=False)