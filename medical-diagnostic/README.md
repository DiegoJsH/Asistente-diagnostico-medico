# Asistente de Diagnóstico Médico
## Proyecto Académico: Scala · Python · Prolog

Un sistema de diagnóstico médico que integra tres lenguajes de programación con arquitectura modular y clara separación de responsabilidades.

---

## 📋 Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────┐
│              Frontend Web (HTML/CSS/JS)                  │
│         - Interfaz de usuario simple                     │
│         - Selección de síntomas                          │
│         - Visualización de resultados                    │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP REST API
                     ↓
┌─────────────────────────────────────────────────────────┐
│         Módulo SCALA (Orquestador Principal)             │
│         - Servidor HTTP embebido                         │
│         - Lógica de negocio                              │
│         - Ruteo de solicitudes                           │
│         - Puerto: 8080                                   │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP REST
                     ↓
┌─────────────────────────────────────────────────────────┐
│         Módulo PYTHON (Intermediario)                    │
│         - API REST con Flask                             │
│         - Procesamiento de datos                         │
│         - Interfaz con Prolog                            │
│         - Puerto: 5000                                   │
└────────────────────┬────────────────────────────────────┘
                     │ PySwip
                     ↓
┌─────────────────────────────────────────────────────────┐
│         Módulo PROLOG (Base de Conocimientos)            │
│         - Hechos (síntomas, enfermedades)                │
│         - Reglas de inferencia lógica                    │
│         - Motor de consultas                             │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 Estructura del Proyecto

```
medical-diagnostic/
├── prolog/
│   └── knowledge_base.pl          # Base de conocimientos Prolog
├── python/
│   ├── app.py                     # Servidor Flask
│   └── requirements.txt           # Dependencias Python
├── scala/
│   ├── build.sbt                  # Configuración SBT
│   └── src/main/scala/
│       └── Main.scala             # Servidor HTTP Scala
└── README.md                      # Este archivo
```

---

## 🚀 Instalación y Ejecución

### Requisitos Previos

- **Scala 2.13+** con SBT instalado
- **Python 3.8+**
- **SWI-Prolog** instalado en el sistema

#### Verificar instalaciones:
```bash
scala -version
sbt --version
python3 --version
swipl --version
```

### Paso 1: Instalar Dependencias Python

```bash
cd python
pip install -r requirements.txt
```

En algunos sistemas, puede necesitar instalar SWI-Prolog primero:
```bash
# macOS
brew install swi-prolog

# Ubuntu/Debian
sudo apt-get install swi-prolog

# Windows
# Descargar de: https://www.swi-prolog.org/Download
```

### Paso 2: Compilar Scala

```bash
cd scala
sbt compile
```

### Paso 3: Ejecutar el Sistema

**Terminal 1 - Iniciar servidor Python:**
```bash
cd python
python3 app.py
```

Verás:
```
Iniciando servidor Python...
Módulo intermediario entre Scala y Prolog
Escuchando en http://localhost:5000
```

**Terminal 2 - Iniciar servidor Scala:**
```bash
cd scala
sbt run
```

Verás:
```
============================================================
ASISTENTE DE DIAGNÓSTICO MÉDICO
Módulo Principal - Scala
============================================================

[Scala] Verificando disponibilidad de módulo Python...
[OK] Servidor Python está activo

[Scala] Iniciando servidor en puerto 8080...
[OK] Servidor Scala activo en http://localhost:8080

ARQUITECTURA DEL SISTEMA:
  Frontend (HTML/JS) -> Scala (REST API) -> Python -> Prolog
```

### Paso 4: Acceder a la Aplicación

Abre tu navegador en: **http://localhost:8080**

---

## 🔍 Funcionalidades

### 1. Selección de Síntomas
- Interfaz con checkboxes para síntomas disponibles
- Cargados dinámicamente desde la base de conocimientos

### 2. Motor de Diagnóstico
- Análisis de síntomas mediante reglas Prolog
- Retorna enfermedades posibles con nivel de confianza

### 3. Información de Enfermedades
- Descripción y recomendaciones para cada diagnóstico
- Nivel de confianza basado en coincidencia de síntomas

### 4. API REST Completa

#### Endpoints disponibles:

**GET** `/api/status`
```bash
curl http://localhost:8080/api/status
```
Retorna estado del sistema

**GET** `/api/symptoms`
```bash
curl http://localhost:8080/api/symptoms
```
Lista todos los síntomas disponibles

**GET** `/api/diseases`
```bash
curl http://localhost:8080/api/diseases
```
Lista todas las enfermedades conocidas

**POST** `/api/diagnose`
```bash
curl -X POST http://localhost:8080/api/diagnose \
  -H "Content-Type: application/json" \
  -d '{"symptoms": ["fiebre", "tos", "fatiga", "cuerpo_adolorido"]}'
```
Obtiene diagnósticos para un conjunto de síntomas

---

## 📚 Síntomas y Enfermedades Disponibles

### Síntomas
- fiebre
- tos
- dolor_cabeza
- congestión_nasal
- dolor_garganta
- erupciones_piel
- vómitos
- diarrea
- fatiga
- escalofríos
- estornudos
- cuerpo_adolorido

### Enfermedades y Reglas

| Enfermedad | Síntomas Requeridos | Confianza |
|---|---|---|
| **Gripe** | Fiebre + Tos + Fatiga + Cuerpo adolorido | 95% |
| **Resfriado Común** | Congestión nasal + Tos + Estornudos | 85% |
| **Faringitis** | Dolor garganta + Fiebre + Dolor cabeza | 80% |
| **Gastroenteritis** | Vómitos + Diarrea + Fatiga | 90% |
| **Varicela** | Erupciones de piel + Fiebre + Fatiga | 85% |
| **Alergia** | Estornudos + Congestión nasal + Erupciones piel | 75% |
| **Migraña** | Dolor cabeza + Fatiga | 70% |

---

## 🔧 Modificar la Base de Conocimientos

Para agregar nuevas enfermedades o síntomas:

1. Edita `prolog/knowledge_base.pl`
2. Agrega síntomas:
   ```prolog
   sintoma(nuevo_síntoma).
   ```
3. Agrega enfermedad:
   ```prolog
   enfermedad(nueva_enfermedad).
   ```
4. Agrega regla de diagnóstico:
   ```prolog
   diagnostico(nueva_enfermedad, Síntomas) :-
       member(síntoma1, Síntomas),
       member(síntoma2, Síntomas),
       member(síntoma3, Síntomas).
   ```
5. Agrega información:
   ```prolog
   información(nueva_enfermedad, 'Descripción y recomendaciones.').
   ```

---

## 📝 Flujo de Datos Ejemplo

**Usuario selecciona:** Fiebre, Tos, Fatiga, Cuerpo Adolorido

1. **Frontend (JavaScript)** → Recolecta síntomas y envía JSON a Scala
2. **Scala (Orquestador)** → Recibe POST /api/diagnose, lo reenvía a Python
3. **Python (Intermediario)** → Recibe síntomas, los convierte a átomos Prolog, consulta Prolog
4. **Prolog (Base de Conocimientos)** → Evalúa reglas:
   - ¿Tiene fiebre, tos, fatiga y cuerpo adolorido? → SÍ → Gripe
5. **Python** → Retorna resultado con información y confianza
6. **Scala** → Pasa respuesta al Frontend
7. **Frontend** → Muestra "Posible Gripe (95% confianza)" con recomendaciones

---

## 🛠️ Troubleshooting

### Error: "Python no está disponible"
- Verificar que Python está ejecutándose en terminal 1
- Verificar puerto 5000 no está ocupado: `lsof -i :5000`

### Error: "No se pueden cargar síntomas"
- Verificar que `knowledge_base.pl` existe en `prolog/`
- Verificar que SWI-Prolog está instalado
- Ver logs de Python para errores de PySwip

### Error: Puerto 8080 ocupado
```bash
# Encontrar proceso usando puerto 8080
lsof -i :8080

# Matar proceso
kill -9 <PID>
```

### Error al compilar Scala
```bash
# Limpiar caché SBT
cd scala
sbt clean
sbt compile
```

---

## 📖 Conceptos Académicos

### ¿Por qué esta arquitectura?

1. **Scala como Orquestador**
   - Lenguaje compilado, tipado, de alto rendimiento
   - Servidor HTTP embebido sin dependencias externas
   - Lógica de negocio clara

2. **Python como Intermediario**
   - Facilita comunicación con Prolog
   - Procesamiento de datos flexible
   - API REST simple de consumir

3. **Prolog como Base de Conocimientos**
   - Lógica pura para inferencias
   - Hechos y reglas separados de lógica de negocio
   - Motor de búsqueda automático

### Ventajas de esta separación
- ✅ Modularidad clara
- ✅ Fácil de entender y mantener
- ✅ Cada módulo tiene responsabilidad única
- ✅ Escalable para agregar más conocimiento
- ✅ Permite reemplazar cualquier módulo sin afectar otros

---

## 📄 Licencia

Proyecto académico - Uso libre para fines educativos

---

## 👨‍🎓 Notas para Evaluación

Este proyecto demuestra:
- ✅ Integración efectiva de 3 lenguajes de programación
- ✅ Arquitectura en capas bien definida
- ✅ APIs REST para comunicación entre módulos
- ✅ Base de conocimientos lógica en Prolog
- ✅ Interfaz web funcional y usable
- ✅ Código documentado y mantenible
- ✅ Uso mínimo de dependencias externas

**Puntos clave para presentación:**
1. Mostrar flujo de datos entre módulos
2. Explicar reglas de diagnóstico en Prolog
3. Demostrar agregar nueva enfermedad
4. Revisar código de cada módulo
5. Mostrar API en acción con curl

---

**Última actualización:** 2024
**Versión:** 1.0.0
