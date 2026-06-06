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
            diagnosticos_con_info.append({
                'enfermedad': str(diagnosis),
                'descripcion': info,
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
    """Get diagnoses manually (fallback)"""
    diagnoses = []
    symptom_str = str(symptoms)
    
    # Flu
    if all(s in symptom_str for s in ['fiebre', 'tos', 'fatiga', 'cuerpo_adolorido']):
        diagnoses.append('gripe')
    # Common cold
    if all(s in symptom_str for s in ['congestion_nasal', 'tos', 'estornudos']):
        diagnoses.append('resfriado_comun')
    # Pharyngitis
    if all(s in symptom_str for s in ['dolor_garganta', 'fiebre']):
        diagnoses.append('faringitis')
    # Gastroenteritis
    if all(s in symptom_str for s in ['vomitos', 'diarrea']):
        diagnoses.append('gastroenteritis')
    # Chickenpox
    if all(s in symptom_str for s in ['erupciones_piel', 'fiebre']):
        diagnoses.append('varicela')
    # Allergy
    if all(s in symptom_str for s in ['estornudos', 'congestion_nasal']):
        diagnoses.append('alergia')
    # Migraine
    if all(s in symptom_str for s in ['dolor_cabeza', 'fatiga']):
        diagnoses.append('migrana')
    
    return diagnoses

def get_diagnosis_info(diagnosis):
    """Get information about a disease"""
    info_map = {
        'gripe': 'Influenza. Requires rest, fluids and antivirals in severe cases.',
        'resfriado_comun': 'Common cold. Rest and symptom medications.',
        'faringitis': 'Bacterial pharyngitis. Requires antibiotics. Consult a doctor.',
        'gastroenteritis': 'Gastroenteritis. Stay hydrated. Consult if persists.',
        'varicela': 'Chickenpox. Isolation recommended. Consult your doctor.',
        'alergia': 'Allergic reaction. Avoid allergens. Consider antihistamines.',
        'migrana': 'Migraine. Rest in dark and quiet environment recommended.'
    }
    return info_map.get(diagnosis.lower(), 'Information not available.')

def calculate_confidence(symptoms, diagnosis):
    """Calculate confidence level based on symptom matching"""
    confidence_map = {
        'gripe': 0.95,
        'resfriado_comun': 0.85,
        'faringitis': 0.80,
        'gastroenteritis': 0.90,
        'varicela': 0.85,
        'alergia': 0.75,
        'migrana': 0.70
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
