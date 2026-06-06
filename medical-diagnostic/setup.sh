#!/bin/bash

# Script de ejecución del Asistente de Diagnóstico Médico
# Debes ejecutar esto en la carpeta raíz del proyecto

echo "╔════════════════════════════════════════════════════════╗"
echo "║   ASISTENTE DE DIAGNÓSTICO MÉDICO - Scala/Python/Prolog║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar que Prolog está instalado
echo "[1/3] Verificando dependencias..."
if ! command -v swipl &> /dev/null; then
    echo -e "${RED}✗ SWI-Prolog no está instalado${NC}"
    echo "  Instala con: brew install swi-prolog (macOS)"
    exit 1
fi
echo -e "${GREEN}✓ SWI-Prolog detectado${NC}"

# Verificar Python
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}✗ Python 3 no está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Python 3 detectado${NC}"

# Verificar Scala/SBT
if ! command -v sbt &> /dev/null; then
    echo -e "${RED}✗ SBT no está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✓ SBT detectado${NC}"

echo ""
echo "[2/3] Instalando dependencias Python..."
cd python
pip install -q -r requirements.txt 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Dependencias Python instaladas${NC}"
else
    echo -e "${YELLOW}⚠ Error instalando dependencias${NC}"
fi
cd ..

echo ""
echo "[3/3] Compilando Scala..."
cd scala
sbt compile > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Scala compilado exitosamente${NC}"
else
    echo -e "${RED}✗ Error compilando Scala${NC}"
    exit 1
fi
cd ..

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║         INICIA LOS SERVIDORES EN ORDEN                  ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""
echo -e "${YELLOW}Terminal 1 - Servidor Python:${NC}"
echo "  cd python && python3 app.py"
echo ""
echo -e "${YELLOW}Terminal 2 - Servidor Scala:${NC}"
echo "  cd scala && sbt run"
echo ""
echo -e "${YELLOW}Luego abre en tu navegador:${NC}"
echo "  http://localhost:8080"
echo ""
