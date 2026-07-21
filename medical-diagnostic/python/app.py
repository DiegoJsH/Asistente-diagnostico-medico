#!/usr/bin/env python3
"""
Python Module - Intermediary between Spring Boot / Scala and Prolog
Exposes a REST API that queries the Prolog knowledge base
"""

from flask import Flask, request, jsonify
from pyswip import Prolog
import os

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False

# Initialize Prolog
prolog = Prolog()

# Load knowledge base ONCE at startup using relative path
knowledge_base_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'knowledge_base.pl')

try:
    if os.path.exists(knowledge_base_path):
        prolog.consult(knowledge_base_path)
        print(f"[Python] Base de conocimientos Prolog cargada exitosamente desde: {knowledge_base_path}")
    else:
        print(f"[ERROR] No se encontró el archivo: {knowledge_base_path}")
except Exception as e:
    print(f"[ERROR] No se pudo cargar la base de conocimientos: {e}")

@app.route('/', methods=['GET'])
def index():
    """Root endpoint to check service status"""
    return jsonify({
        'status': 'ok',
        'message': 'API de Diagnóstico Médico (Python - Prolog) lista'
    }), 200

@app.route('/health', methods=['GET'])
def health():
    """Check that Python server is active"""
    return jsonify({
        'status': 'ok',
        'module': 'Python Intermediary',
        'description': 'Intermediary between Spring Boot and Prolog'
    }), 200

@app.route('/diagnose', methods=['POST'])
def diagnose():
    """
    Endpoint to get diagnoses based on symptoms
    Expects JSON: {"symptoms": ["fiebre", "tos", "fatiga", "cuerpo_adolorido"]}
    """
    try:
        data = request.get_json() or {}
        symptoms = data.get('symptoms', [])
        
        if not symptoms:
            return jsonify({
                'error': 'No symptoms provided',
                'symptoms_required': True
            }), 400
        
        # Convert strings to Prolog atoms
        prolog_symptoms = [atom(symptom) for symptom in symptoms]
        
        # Query possible diagnoses using list() to close the C-query immediately
        diagnoses = []
        try:
            query_str = f'obtener_diagnosticos({prolog_symptoms}, Diagnosticos)'
            results = list(prolog.query(query_str))
            if results and 'Diagnosticos' in results[0]:
                diagnoses = [str(d) for d in results[0]['Diagnosticos']]
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
        # Usamos list() para evaluar la consulta y cerrar el puntero C
        results = list(prolog.query('sintoma(X)'))
        for result in results:
            symptoms.append(str(result['X']))
    except Exception as e:
        print(f"Error getting symptoms from Prolog: {e}")
    
    sorted_symptoms = sorted(symptoms)
    return jsonify({
        'symptoms': sorted_symptoms,
        'sintomas_disponibles': sorted_symptoms,
        'total': len(sorted_symptoms)
    }), 200

@app.route('/diseases', methods=['GET'])
def get_diseases():
    """Return list of known diseases"""
    diseases = []
    try:
        results = list(prolog.query('enfermedad(X)'))
        for result in results:
            diseases.append(str(result['X']))
    except Exception as e:
        print(f"Error getting diseases from Prolog: {e}")
    
    sorted_diseases = sorted(diseases)
    return jsonify({
        'diseases': sorted_diseases,
        'enfermedades_conocidas': sorted_diseases,
        'total': len(sorted_diseases)
    }), 200

def get_diagnoses_manual(symptoms):
    """Get diagnoses manually (fallback if Prolog query fails)"""
    diagnoses = []
    symptom_str = str(symptoms)
    
    if all(s in symptom_str for s in ['fiebre', 'dolor_articular', 'fatiga']):
        diagnoses.append('dengue')
    if all(s in symptom_str for s in ['dolor_abdominal', 'vomitos']):
        diagnoses.append('apendicitis')
    if all(s in symptom_str for s in ['fiebre', 'tos', 'fatiga', 'cuerpo_adolorido']):
        diagnoses.append('gripe')
    if all(s in symptom_str for s in ['congestion_nasal', 'tos', 'estornudos']):
        diagnoses.append('resfriado_comun')
    if all(s in symptom_str for s in ['dolor_garganta', 'fiebre']):
        diagnoses.append('faringitis')
    if all(s in symptom_str for s in ['vomitos', 'diarrea']):
        diagnoses.append('gastroenteritis')
    if all(s in symptom_str for s in ['erupciones_piel', 'fiebre']):
        diagnoses.append('varicela')
    if all(s in symptom_str for s in ['estornudos', 'congestion_nasal']):
        diagnoses.append('alergia')
    if all(s in symptom_str for s in ['dolor_cabeza', 'fatiga']):
        diagnoses.append('migrana')
    if all(s in symptom_str for s in ['congestion_nasal', 'dolor_cabeza', 'dolor_garganta']):
        diagnoses.append('sinusitis')
    if all(s in symptom_str for s in ['fiebre', 'tos', 'dificultad_respirar']):
        diagnoses.append('neumonia')
    if all(s in symptom_str for s in ['tos', 'dificultad_respirar']):
        diagnoses.append('bronquitis')
    if all(s in symptom_str for s in ['dolor_garganta', 'fiebre', 'dificultad_tragar']):
        diagnoses.append('amigdalitis')
    if all(s in symptom_str for s in ['fiebre', 'tos', 'perdida_olfato']):
        diagnoses.append('covid19')
    if all(s in symptom_str for s in ['dolor_orinar', 'fiebre']):
        diagnoses.append('infeccion_urinaria')
    
    return diagnoses

def get_diagnosis_info(diagnosis):
    """Get information about a disease directly from Prolog"""
    try:
        results = list(prolog.query(f"obtener_informacion({diagnosis}, Info)"))
        if results:
            info = results[0]['Info']
            if isinstance(info, bytes):
                return info.decode('utf-8')
            return str(info)
    except Exception as e:
        print(f"Error querying Prolog info: {e}")
    
    return 'Información médica no disponible.'

def get_diagnosis_triaje(diagnosis):
    """Obtiene el nivel de triaje de la enfermedad desde Prolog"""
    try:
        results = list(prolog.query(f"obtener_triaje({diagnosis}, Nivel)"))
        if results:
            return str(results[0]['Nivel'])
    except Exception as e:
        print(f"Error querying Prolog triaje: {e}")
    return 'amarillo'

def get_diagnosis_specialist(diagnosis):
    """Obtiene el especialista médico recomendado desde Prolog"""
    try:
        results = list(prolog.query(f"obtener_especialista({diagnosis}, Doc)"))
        if results:
            doc = results[0]['Doc']
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
    return str(s).lower().strip().replace(' ', '_')

if __name__ == '__main__':
    port = int(os.environ.get("PORT", 5000))
    print(f"Starting Python server on port {port}...")
    app.run(host='0.0.0.0', port=port, debug=False, threaded=False)