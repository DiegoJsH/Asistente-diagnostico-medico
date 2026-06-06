# GUÍA: Cómo Extender la Base de Conocimientos

## Ejemplo: Agregar Dengue

### Paso 1: Edita `prolog/knowledge_base.pl`

Agrega estos síntomas si no existen:
```prolog
sintoma(malestar_generalizado).
sintoma(ojos_adoloridos).
sintoma(dolor_articulaciones).
```

### Paso 2: Agrega la enfermedad
```prolog
enfermedad(dengue).
```

### Paso 3: Agrega la regla de diagnóstico
```prolog
% Dengue: fiebre + dolor cabeza + ojos adoloridos + dolor articulaciones
diagnostico(dengue, Síntomas) :-
    member(fiebre, Síntomas),
    member(dolor_cabeza, Síntomas),
    member(ojos_adoloridos, Síntomas),
    member(dolor_articulaciones, Síntomas).
```

### Paso 4: Agrega información
```prolog
información(dengue, 'Dengue - Infección viral. Reposo absoluto recomendado. Consulte inmediatamente con su médico.').
```

### Paso 5: Reinicia Python
```bash
# En terminal de Python
Ctrl+C
python3 app.py
```

¡Listo! Ahora el sistema reconocerá dengue.

---

## Ejemplo 2: Agregar Bronquitis

```prolog
% Agregar síntoma
sintoma(secreción_mucosa).

% Agregar enfermedad
enfermedad(bronquitis).

% Agregar regla
diagnostico(bronquitis, Síntomas) :-
    member(tos, Síntomas),
    member(secreción_mucosa, Síntomas),
    member(fatiga, Síntomas),
    member(dolor_cabeza, Síntomas).

% Agregar información
información(bronquitis, 'Bronquitis - Inflamación de las vías respiratorias. Requiere descanso y medicación broncodilatadora.').
```

---

## Sintaxis Prolog Básica para Diagnósticos

### Operador `member(Elemento, Lista)`
Verifica si un elemento está en la lista
```prolog
member(fiebre, [fiebre, tos, fatiga])  % TRUE
member(alergia, [fiebre, tos])         % FALSE
```

### Operador `,` (AND)
Todas las condiciones deben ser verdaderas
```prolog
diagnostico(gripe, S) :-
    member(fiebre, S),      % Y
    member(tos, S),         % Y
    member(fatiga, S).      % Y
```

### Backtracking automático
Prolog prueba todas las reglas y retorna múltiples resultados
```prolog
% Si síntomas coinciden con gripe Y resfriado,
% ambos se retornarán
```

---

## Estructura Completa de una Enfermedad

Para cada enfermedad, necesitas 3 componentes:

```prolog
% 1. Declaración de síntomas necesarios
sintoma(síntoma1).
sintoma(síntoma2).
sintoma(síntoma3).

% 2. Declaración de enfermedad
enfermedad(mi_enfermedad).

% 3. Regla de diagnóstico (mínimo 2 síntomas recomendado)
diagnostico(mi_enfermedad, Síntomas) :-
    member(síntoma1, Síntomas),
    member(síntoma2, Síntomas),
    member(síntoma3, Síntomas).

% 4. Información y recomendaciones
información(mi_enfermedad, 'Descripción clara de la enfermedad. Síntomas y recomendaciones.').
```

---

## Niveles de Confianza

En `python/app.py`, la función `calculate_confidence()` define:

```python
confidence_map = {
    'gripe': 0.95,           # 95% - Muy alta (síntomas muy específicos)
    'resfriado_común': 0.85, # 85% - Alta
    'alergia': 0.75,         # 75% - Media (síntomas menos específicos)
    'migraña': 0.70          # 70% - Baja (poco diagnóstico)
}
```

**Usa confianza alta (0.90+) para:**
- Enfermedades con síntomas muy específicos
- Combinaciones únicas de síntomas

**Usa confianza media (0.70-0.85) para:**
- Enfermedades comunes
- Síntomas que se solapan con otras

**Usa confianza baja (0.50-0.70) para:**
- Enfermedades genéricas
- Síntomas muy generales

---

## Mejores Prácticas

1. **Usa nombres descriptivos:**
   ```prolog
   % ✓ Bueno
   sintoma(dolor_pecho_izquierdo).
   
   % ✗ Evita
   sintoma(dpc).
   ```

2. **Separa síntomas con guiones bajos:**
   ```prolog
   sintoma(congestión_nasal)      % ✓
   sintoma(congestión nasal)      % ✗ (error)
   sintoma(congestión-nasal)      % ✗ (menos claro)
   ```

3. **Agrupa síntomas relacionados:**
   ```prolog
   % Síntomas respiratorios
   sintoma(tos).
   sintoma(congestión_nasal).
   sintoma(secreción_mucosa).
   ```

4. **Documenta reglas complejas:**
   ```prolog
   % Neumonía: síntomas respiratorios + fiebre alta + fatiga severa
   diagnostico(neumonía, Síntomas) :-
       member(tos, Síntomas),
       member(fiebre, Síntomas),
       member(fatiga, Síntomas),
       member(dolor_pecho, Síntomas).
   ```

---

## Testing Manual

Desde Python interactivo:
```python
from pyswip import Prolog

prolog = Prolog()
prolog.consult('prolog/knowledge_base.pl')

# Probar si una regla funciona
symptoms = [atom('fiebre'), atom('tos'), atom('fatiga'), atom('cuerpo_adolorido')]
for result in prolog.query(f'diagnostico(gripe, {symptoms})'):
    print("Gripe confirmada:", result)
```

---

## Revisión de Cambios

Después de agregar enfermedades:

1. ✓ Reinicia Python
2. ✓ Recarga la página web (F5)
3. ✓ Prueba con los síntomas nuevos
4. ✓ Verifica la confianza mostrada
5. ✓ Valida la información displayada

---

**¿Necesitas ayuda?** Ver `README.md` o `QUICK_START.md`
