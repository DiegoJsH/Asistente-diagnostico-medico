# GUÍA RÁPIDA - Asistente de Diagnóstico Médico

## 🚀 Inicio Rápido

### Instalación única:
```bash
cd python
pip install -r requirements.txt
cd ../scala
sbt compile
```

### Ejecución (en dos terminales):

**Terminal 1:**
```bash
cd python
python3 app.py
```

**Terminal 2:**
```bash
cd scala
sbt run
```

**Navegador:**
```
http://localhost:8080
```

---

## 🏗️ Arquitectura en 30 segundos

```
HTML/CSS/JS (Puerto 8080)
        ↓ HTTP
    SCALA (Orquestador)
        ↓ HTTP
    PYTHON (Intermediario)
        ↓ PySwip
    PROLOG (Base de conocimientos)
```

---

## 📡 Endpoints API

| Método | Endpoint | Función |
|--------|----------|---------|
| GET | `/api/status` | Estado del sistema |
| GET | `/api/symptoms` | Lista síntomas |
| GET | `/api/diseases` | Lista enfermedades |
| POST | `/api/diagnose` | Obtiene diagnóstico |

---

## 📝 Ejemplo de consulta

```bash
curl -X POST http://localhost:8080/api/diagnose \
  -H "Content-Type: application/json" \
  -d '{
    "symptoms": ["fiebre", "tos", "fatiga", "cuerpo_adolorido"]
  }'
```

**Respuesta esperada:**
```json
{
  "síntomas_ingresados": ["fiebre", "tos", "fatiga", "cuerpo_adolorido"],
  "diagnósticos_posibles": [
    {
      "enfermedad": "gripe",
      "descripción": "Influenza. Requiere reposo, líquidos y antivirales en casos severos.",
      "confianza": 0.95
    }
  ],
  "total_diagnósticos": 1
}
```

---

## 🛠️ Agregar Nueva Enfermedad

1. **Edita:** `prolog/knowledge_base.pl`
2. **Agrega síntomas:**
   ```prolog
   sintoma(nuevo_síntoma).
   ```
3. **Agrega enfermedad:**
   ```prolog
   enfermedad(nueva_enfermedad).
   ```
4. **Agrega regla:**
   ```prolog
   diagnostico(nueva_enfermedad, Síntomas) :-
       member(síntoma1, Síntomas),
       member(síntoma2, Síntomas).
   ```
5. **Agrega información:**
   ```prolog
   información(nueva_enfermedad, 'Descripción aquí.').
   ```
6. **Reinicia Python** (Ctrl+C y python3 app.py)

---

## 🐛 Troubleshooting Rápido

| Problema | Solución |
|----------|----------|
| "Python no disponible" | Verifica que Python corre en puerto 5000 |
| "Puerto ocupado" | `lsof -i :8080` y `kill -9 <PID>` |
| "No carga síntomas" | Verifica Prolog instalado con `swipl --version` |
| "Error compilación Scala" | `cd scala && sbt clean && sbt compile` |

---

## 📚 Archivos Clave

- **`prolog/knowledge_base.pl`** → Base de conocimientos y reglas
- **`python/app.py`** → API REST (Flask + PySwip)
- **`scala/src/main/scala/Main.scala`** → Servidor web + orquestador
- **`README.md`** → Documentación completa

---

## ✅ Puntos de Evaluación Académica

1. ✓ Scala coordina y orquesta todo
2. ✓ Python intermedia entre Scala y Prolog
3. ✓ Prolog contiene base de conocimientos
4. ✓ APIs REST para comunicación
5. ✓ Frontend HTML/CSS/JS sin frameworks
6. ✓ Sistema funcional y modular
7. ✓ Código documentado

---

**¿Preguntas?** Ver `README.md` para documentación completa.
