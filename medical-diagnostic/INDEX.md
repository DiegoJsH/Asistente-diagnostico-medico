# ÍNDICE DEL PROYECTO

## 📚 Documentación

- **[README.md](README.md)** - Documentación completa del proyecto
- **[QUICK_START.md](QUICK_START.md)** - Guía rápida para empezar
- **[ARQUITECTURA.txt](ARQUITECTURA.txt)** - Diagrama y flujo detallado
- **[EXTENSIONES.md](EXTENSIONES.md)** - Cómo agregar nuevas enfermedades

## 🗂️ Estructura de Archivos

### Prolog (Base de Conocimientos)
```
prolog/
└── knowledge_base.pl          Hechos, reglas y lógica de diagnóstico
```

### Python (Intermediario)
```
python/
├── app.py                     Servidor Flask + API REST + PySwip
└── requirements.txt           Dependencias (Flask, PySwip)
```

### Scala (Orquestador)
```
scala/
├── build.sbt                  Configuración de compilación SBT
└── src/main/scala/
    └── Main.scala             Servidor HTTP + Orquestación + HTML embebido
```

### Configuración
```
setup.sh                        Script de instalación y verificación
```

## 🚀 Inicio Rápido

1. **Terminal 1 - Python:**
   ```bash
   cd python && python3 app.py
   ```

2. **Terminal 2 - Scala:**
   ```bash
   cd scala && sbt run
   ```

3. **Navegador:**
   ```
   http://localhost:8080
   ```

## 📡 Arquitectura (Capas)

```
Frontend (HTML/CSS/JS)  ← Puerto 8080, Scala
         ↓
    Scala REST API      ← Orquestador, puerto 8080
         ↓
   Python REST API      ← Intermediario, puerto 5000
         ↓
      Prolog            ← Base de conocimientos, PySwip
```

## 🎯 Endpoints API

| Método | Ruta | Función |
|--------|------|---------|
| GET | `/` | Página HTML |
| GET | `/api/status` | Estado del sistema |
| GET | `/api/symptoms` | Síntomas disponibles |
| GET | `/api/diseases` | Enfermedades conocidas |
| POST | `/api/diagnose` | Obtener diagnóstico |

## 📊 Contenido Actual

### 12 Síntomas
- Fiebre, Tos, Dolor cabeza, Congestión nasal
- Dolor garganta, Erupciones de piel, Vómitos, Diarrea
- Fatiga, Escalofríos, Estornudos, Cuerpo adolorido

### 7 Enfermedades
- Gripe (95%)
- Resfriado común (85%)
- Faringitis (80%)
- Gastroenteritis (90%)
- Varicela (85%)
- Alergia (75%)
- Migraña (70%)

## 🔧 Modificar el Sistema

### Agregar Nueva Enfermedad
Ver [EXTENSIONES.md](EXTENSIONES.md)

### Cambiar Interfaz Web
Ver `scala/src/main/scala/Main.scala` → función `getHTMLContent()`

### Ajustar Niveles de Confianza
Ver `python/app.py` → función `calculate_confidence()`

## 📋 Requisitos

- Scala 2.13+ con SBT
- Python 3.8+
- SWI-Prolog

## ✅ Verificación del Proyecto

```bash
# Verificar estructura
ls -la prolog/ python/ scala/

# Verificar Prolog
swipl --version

# Verificar Python
python3 --version
pip list | grep Flask

# Verificar Scala
sbt --version
```

## 🐛 Troubleshooting Común

| Problema | Solución |
|----------|----------|
| "Python no disponible" | Verificar que Python corre en puerto 5000 |
| "Símbolos extraños en terminal" | Normal para átomos Prolog |
| "Puerto ocupado" | `lsof -i :8080` y `kill -9 <PID>` |
| "No carga síntomas" | Reiniciar Python |

## 📖 Recursos

- [SWI-Prolog Documentation](https://www.swi-prolog.org/)
- [Scala Language](https://www.scala-lang.org/)
- [Python Flask](https://flask.palletsprojects.com/)
- [PySwip](https://github.com/yuce/pyswip)

## 🎓 Para Presentación Académica

**Puntos a cubrir:**
1. ✓ Arquitectura modular (3 lenguajes)
2. ✓ Flujo de datos completo
3. ✓ Reglas Prolog y lógica de inferencia
4. ✓ APIs REST para comunicación
5. ✓ Base de conocimientos extensible
6. ✓ Interfaz web funcional
7. ✓ Integración sin frameworks innecesarios

**Demo:**
1. Mostrar flujo en navegador
2. Usar curl para demostrar API
3. Agregar nueva enfermedad en vivo en Prolog
4. Explicar ventajas de la arquitectura

## 📞 Soporte

- Ver README.md para documentación completa
- Ver QUICK_START.md para ejecución rápida
- Ver ARQUITECTURA.txt para diagramas detallados
- Ver EXTENSIONES.md para extensibilidad

---

**Proyecto Académico:** Asistente de Diagnóstico Médico
**Lenguajes:** Scala · Python · Prolog
**Versión:** 1.0.0
