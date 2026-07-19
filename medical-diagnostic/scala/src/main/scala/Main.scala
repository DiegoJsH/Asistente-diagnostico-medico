import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.{Try, Success, Failure}
import java.io.File

/**
 * Módulo Scala - Orquestador principal del sistema
 * Coordina la ejecución general y lógica de negocio
 * Comunica con Python via API REST
 */

// Estructura de datos para síntomas
case class DiagnosisRequest(symptoms: List[String])

// Estructura de datos para respuestas
case class DiagnosisResponse(
    symptoms: List[String],
    diagnoses: List[DiagnosisResult],
    totalDiagnoses: Int
)

case class DiagnosisResult(
    enfermedad: String,
    descripción: String,
    confianza: Double
)

// Servidor web embebido
object MedicalDiagnosticServer {
  
  private val PYTHON_API_URL = "http://localhost:5000"
  private val SCALA_PORT = 8080
  
  def main(args: Array[String]): Unit = {
    println("=" * 60)
    println("ASISTENTE DE DIAGNÓSTICO MÉDICO")
    println("Módulo Principal - Scala")
    println("=" * 60)
    println()
    
    // Verificar que Python esté activo
    println("[Scala] Verificando disponibilidad de módulo Python...")
    if (!checkPythonServer()) {
      println("[ERROR] El servidor Python no está disponible en " + PYTHON_API_URL)
      println("[Scala] Inicia Python con: python3 python/app.py")
      sys.exit(1)
    }
    println("[OK] Servidor Python está activo")
    println()
    
    // Iniciar servidor web
    println(s"[Scala] Iniciando servidor en puerto $SCALA_PORT...")
    startWebServer()
  }
  
  /**
   * Verificar que el servidor Python está activo
   */
  def checkPythonServer(): Boolean = {
    try {
      val url = new java.net.URL(s"$PYTHON_API_URL/health")
      val connection = url.openConnection().asInstanceOf[java.net.HttpURLConnection]
      connection.setRequestMethod("GET")
      connection.setConnectTimeout(5000)
      connection.setReadTimeout(5000)
      
      val responseCode = connection.getResponseCode
      connection.disconnect()
      
      responseCode == 200
    } catch {
      case _: Exception => false
    }
  }
  
  /**
   * Iniciar servidor web HTTP embebido
   */
  def startWebServer(): Unit = {
    val server = com.sun.net.httpserver.HttpServer.create(
      new java.net.InetSocketAddress(SCALA_PORT),
      0
    )
    
    // Ruta para servir HTML
    server.createContext("/", new com.sun.net.httpserver.HttpHandler {
      def handle(exchange: com.sun.net.httpserver.HttpExchange): Unit = {
        if (exchange.getRequestMethod == "GET") {
          val htmlContent = getHTMLContent()
          val bytes = htmlContent.getBytes("UTF-8") // <-- Fix UTF-8
          exchange.getResponseHeaders.set("Content-Type", "text/html; charset=UTF-8")
          exchange.sendResponseHeaders(200, bytes.length)
          exchange.getResponseBody.write(bytes)
          exchange.close()
        }
      }
    })
    
    // Ruta API para diagnóstico
    server.createContext("/api/diagnose", new com.sun.net.httpserver.HttpHandler {
      def handle(exchange: com.sun.net.httpserver.HttpExchange): Unit = {
        if (exchange.getRequestMethod == "POST") {
          try {
            val requestBody = scala.io.Source.fromInputStream(
              exchange.getRequestBody
            ).mkString
            
            // Parsear JSON manualmente
            val symptomsJson = extractSymptoms(requestBody)
            
            // Llamar a Python
            val result = callPythonDiagnose(symptomsJson)
            val bytes = result.getBytes("UTF-8") // <-- Fix UTF-8
            
            exchange.getResponseHeaders.set("Content-Type", "application/json; charset=UTF-8")
            exchange.getResponseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.getResponseBody.write(bytes)
            exchange.close()
          } catch {
            case e: Exception =>
              val error = s"""{"error": "${e.getMessage}"}"""
              val errorBytes = error.getBytes("UTF-8")
              exchange.getResponseHeaders.set("Content-Type", "application/json")
              exchange.sendResponseHeaders(500, errorBytes.length)
              exchange.getResponseBody.write(errorBytes)
              exchange.close()
          }
        }
      }
    })
    
    // Ruta API para obtener síntomas disponibles
    server.createContext("/api/symptoms", new com.sun.net.httpserver.HttpHandler {
      def handle(exchange: com.sun.net.httpserver.HttpExchange): Unit = {
        if (exchange.getRequestMethod == "GET") {
          val result = callPythonEndpoint("/symptoms")
          val bytes = result.getBytes("UTF-8") // <-- Fix UTF-8
          exchange.getResponseHeaders.set("Content-Type", "application/json; charset=UTF-8")
          exchange.getResponseHeaders.set("Access-Control-Allow-Origin", "*")
          exchange.sendResponseHeaders(200, bytes.length)
          exchange.getResponseBody.write(bytes)
          exchange.close()
        }
      }
    })
    
    // Ruta API para obtener enfermedades conocidas
    server.createContext("/api/diseases", new com.sun.net.httpserver.HttpHandler {
      def handle(exchange: com.sun.net.httpserver.HttpExchange): Unit = {
        if (exchange.getRequestMethod == "GET") {
          val result = callPythonEndpoint("/diseases")
          val bytes = result.getBytes("UTF-8") // <-- Fix UTF-8
          exchange.getResponseHeaders.set("Content-Type", "application/json; charset=UTF-8")
          exchange.getResponseHeaders.set("Access-Control-Allow-Origin", "*")
          exchange.sendResponseHeaders(200, bytes.length)
          exchange.getResponseBody.write(bytes)
          exchange.close()
        }
      }
    })
    
    // Ruta para verificar estado del sistema
    server.createContext("/api/status", new com.sun.net.httpserver.HttpHandler {
      def handle(exchange: com.sun.net.httpserver.HttpExchange): Unit = {
        val status = s"""{
          "sistema": "ok",
          "módulo_scala": "activo",
          "servidor_puerto": $SCALA_PORT,
          "python_api": "$PYTHON_API_URL",
          "timestamp": "${java.time.LocalDateTime.now()}"
        }"""
        val bytes = status.getBytes("UTF-8") // <-- Fix UTF-8
        exchange.getResponseHeaders.set("Content-Type", "application/json; charset=UTF-8")
        exchange.sendResponseHeaders(200, bytes.length)
        exchange.getResponseBody.write(bytes)
        exchange.close()
      }
    })
    
    server.setExecutor(null)
    server.start()
    
    println(s"[OK] Servidor Scala activo en http://localhost:$SCALA_PORT")
    println()
    println("ARQUITECTURA DEL SISTEMA:")
    println("  Frontend (HTML/JS) -> Scala (REST API) -> Python -> Prolog")
    println()
    println(s"Abre: http://localhost:$SCALA_PORT")
  }
  
  /**
   * Llamar a endpoint de Python para diagnóstico
   */
  def callPythonDiagnose(symptomsJson: String): String = {
    try {
      val url = new java.net.URL(s"$PYTHON_API_URL/diagnose")
      val connection = url.openConnection().asInstanceOf[java.net.HttpURLConnection]
      connection.setRequestMethod("POST")
      connection.setRequestProperty("Content-Type", "application/json")
      connection.setDoOutput(true)
      
      // Enviar request
      connection.getOutputStream.write(symptomsJson.getBytes)
      connection.getOutputStream.close()
      
      // Leer respuesta
      val response = scala.io.Source.fromInputStream(connection.getInputStream)("UTF-8").mkString
      
      connection.disconnect()
      response
    } catch {
      case e: Exception =>
        s"""{"error": "Error llamando a Python: ${e.getMessage}"}"""
    }
  }
  
  /**
   * Llamar a endpoint de Python
   */
  def callPythonEndpoint(endpoint: String): String = {
    try {
      val url = new java.net.URL(s"$PYTHON_API_URL$endpoint")
      val connection = url.openConnection().asInstanceOf[java.net.HttpURLConnection]
      connection.setRequestMethod("GET")
      connection.setConnectTimeout(5000)
      
      val response = scala.io.Source.fromInputStream(connection.getInputStream)("UTF-8").mkString
      
      connection.disconnect()
      response
    } catch {
      case e: Exception =>
        s"""{"error": "Error llamando a Python: ${e.getMessage}"}"""
    }
  }
  
  /**
   * Extraer síntomas del JSON (parsing manual sin librerías)
   */
  def extractSymptoms(json: String): String = {
    val symptomPattern = """"([^"]+)"""".r
    val symptoms = symptomPattern.findAllMatchIn(json)
      .map(_.group(1))
      .filter(s => s != "symptoms")
      .map(s => s"""\"$s\"""")
      .mkString("[", ",", "]")
    
    s"""{"symptoms": $symptoms}"""
  }
  
  /**
   * Contenido HTML estático
   */
  def getHTMLContent(): String = {
    """<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Asistente de Diagnóstico Médico</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@400;500;600;700&family=IBM+Plex+Sans:wght@400;500;600&display=swap" rel="stylesheet">
    <style>
        :root {
            --paper: #EDF1EE;
            --paper-raised: #FFFFFF;
            --ink: #16241F;
            --ink-soft: #52645C;
            --ink-faint: #8B978F;
            --line: #C9D3CD;
            --accent: #1F5C52;
            --accent-dark: #123B34;
            --accent-soft: #DCEAE6;
            --triage-verde: #2E8B57;
            --triage-verde-bg: #E3F3E9;
            --triage-amarillo: #B8720F;
            --triage-amarillo-bg: #FBEBD3;
            --triage-rojo: #C23B33;
            --triage-rojo-bg: #FAE3E0;
            --shadow-soft: 0 1px 2px rgba(18, 34, 28, 0.04), 0 4px 14px rgba(18, 34, 28, 0.05);
            --mono: 'IBM Plex Mono', 'SFMono-Regular', Consolas, monospace;
            --sans: 'IBM Plex Sans', -apple-system, 'Segoe UI', sans-serif;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: var(--sans);
            background:
                repeating-linear-gradient(0deg, transparent, transparent 39px, rgba(31,92,82,0.05) 40px),
                var(--paper);
            color: var(--ink);
            min-height: 100vh;
            padding: 32px 20px 60px;
        }

        .container {
            max-width: 1020px;
            margin: 0 auto;
        }

        /* ---------- Landing page ---------- */
        .landing-nav {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 4px 4px 28px;
        }

        .brand {
            font-family: var(--sans);
            font-weight: 700;
            font-size: 18px;
            color: var(--accent-dark);
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .brand-icon {
            font-size: 22px;
            line-height: 1;
        }

        .nav-user {
            display: flex;
            align-items: center;
            gap: 14px;
            font-family: var(--sans);
            font-size: 13px;
            color: var(--ink-soft);
        }

        .btn-inline {
            flex: none;
            width: auto;
        }

        .hero {
            background: linear-gradient(135deg, var(--accent-dark) 0%, var(--accent) 55%, var(--triage-verde) 100%);
            border-radius: 6px;
            padding: 64px 44px;
            text-align: center;
            margin-bottom: 44px;
            color: #FFFFFF;
        }

        .hero .eyebrow {
            display: block;
            color: rgba(255,255,255,0.85);
        }

        .hero h1 {
            color: #FFFFFF;
            font-size: 34px;
            font-weight: 700;
            letter-spacing: -0.01em;
            line-height: 1.3;
            margin: 14px auto 16px;
            max-width: 640px;
        }

        .hero-sub {
            max-width: 540px;
            margin: 0 auto 30px;
            color: rgba(255,255,255,0.9);
            font-size: 15px;
            line-height: 1.65;
        }

        .btn-hero-cta {
            background: #FFFFFF;
            color: var(--accent-dark);
            border-color: #FFFFFF;
        }

        .btn-hero-cta:hover {
            background: var(--paper);
            border-color: var(--paper);
        }

        .section-title {
            text-align: center;
            font-size: 24px;
            font-weight: 700;
            color: var(--ink);
            margin-bottom: 32px;
        }

        .feature-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 16px;
            margin-bottom: 48px;
        }

        .feature-card {
            background: var(--paper-raised);
            border: 1px solid var(--line);
            border-radius: 6px;
            box-shadow: var(--shadow-soft);
            padding: 28px 20px;
            text-align: center;
        }

        .feature-icon {
            font-size: 30px;
            margin-bottom: 14px;
        }

        .feature-card h3 {
            font-size: 15px;
            font-weight: 700;
            color: var(--accent-dark);
            margin-bottom: 8px;
        }

        .feature-card p {
            font-size: 13px;
            color: var(--ink-soft);
            line-height: 1.55;
        }

        .checklist-section {
            text-align: center;
            margin-bottom: 48px;
        }

        .checklist {
            max-width: 520px;
            margin: 0 auto;
            text-align: left;
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .checklist-item {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            font-size: 14.5px;
            color: var(--ink);
        }

        .checklist-item .check {
            color: var(--triage-verde);
            font-weight: 700;
            flex-shrink: 0;
        }

        .cta-banner {
            background: linear-gradient(120deg, var(--triage-verde), var(--accent));
            border-radius: 6px;
            padding: 48px 30px;
            text-align: center;
            color: #FFFFFF;
            margin-bottom: 20px;
        }

        .cta-banner h2 {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 10px;
        }

        .cta-banner p {
            font-size: 14px;
            color: rgba(255,255,255,0.9);
            margin-bottom: 24px;
        }

        .btn-cta-white {
            background: #FFFFFF;
            color: var(--accent-dark);
            border-color: #FFFFFF;
        }

        .btn-cta-white:hover {
            background: var(--paper);
            border-color: var(--paper);
        }

        .landing-footer {
            background: var(--ink);
            color: rgba(255,255,255,0.75);
            text-align: center;
            padding: 26px 20px;
            border-radius: 6px;
            font-family: var(--sans);
            font-size: 12.5px;
            line-height: 1.8;
            margin-top: 20px;
        }

        .landing-footer .disclaimer-text {
            display: block;
            font-style: italic;
            color: rgba(255,255,255,0.55);
            margin-top: 4px;
        }

        @media (max-width: 900px) {
            .feature-grid { grid-template-columns: repeat(2, 1fr); }
        }

        @media (max-width: 560px) {
            .feature-grid { grid-template-columns: 1fr; }
            .hero { padding: 40px 24px; }
            .hero h1 { font-size: 24px; }
        }

        /* ---------- Welcome / intake screen ---------- */
        .welcome-screen {
            min-height: calc(100vh - 92px);
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .intake-card {
            background: var(--paper-raised);
            border: 1px solid var(--line);
            border-radius: 6px;
            box-shadow: var(--shadow-soft);
            padding: 40px 36px;
            max-width: 380px;
            width: 100%;
            position: relative;
        }

        .intake-card::before {
            content: '';
            position: absolute;
            top: 0; left: 0; right: 0;
            height: 3px;
            border-radius: 6px 6px 0 0;
            background: var(--accent);
        }

        .intake-card h1 {
            font-size: 26px;
            font-weight: 600;
            margin: 12px 0 8px;
            color: var(--ink);
        }

        .intake-sub {
            font-size: 13px;
            color: var(--ink-soft);
            line-height: 1.55;
            margin-bottom: 22px;
        }

        .intake-input {
            width: 100%;
            padding: 11px 13px;
            border: 1.5px solid var(--line);
            border-radius: 4px;
            font-family: var(--sans);
            font-size: 14px;
            color: var(--ink);
            background: var(--paper);
            transition: border-color 0.15s ease, background 0.15s ease;
        }

        .intake-input:focus {
            outline: none;
            border-color: var(--accent);
            background: var(--paper-raised);
        }

        .intake-back {
            display: inline-block;
            margin-top: 16px;
            font-family: var(--mono);
            font-size: 11px;
            color: var(--ink-faint);
            cursor: pointer;
            text-decoration: none;
        }

        .intake-back:hover {
            color: var(--accent);
        }

        /* ---------- Header: chart header ---------- */
        .chart-header {
            background: var(--paper-raised);
            border: 1px solid var(--line);
            border-radius: 6px 6px 0 0;
            box-shadow: var(--shadow-soft);
            padding: 28px 32px 24px;
            position: relative;
        }

        .eyebrow {
            font-family: var(--mono);
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 0.16em;
            text-transform: uppercase;
            color: var(--accent);
        }

        .btn-logout {
            position: absolute;
            top: 20px;
            right: 24px;
            flex: none;
            background: transparent;
            border: 1px solid var(--line);
            border-radius: 4px;
            color: var(--ink-soft);
            padding: 7px 12px;
            font-size: 10.5px;
        }

        .btn-logout:hover {
            border-color: var(--triage-rojo);
            color: var(--triage-rojo);
            background: var(--triage-rojo-bg);
        }

        @media (max-width: 560px) {
            .btn-logout {
                position: static;
                margin-top: 14px;
                width: auto;
                display: inline-block;
            }
        }

        .chart-header h1 {
            font-family: var(--sans);
            font-weight: 600;
            font-size: 30px;
            letter-spacing: -0.01em;
            margin: 8px 0 14px;
            color: var(--ink);
        }

        .welcome-msg {
            font-family: var(--mono);
            font-size: 12px;
            color: var(--ink-soft);
            margin-top: 12px;
        }

        .welcome-msg b {
            color: var(--accent-dark);
            font-weight: 600;
        }

        /* ---------- ECG signature divider ---------- */
        .pulse-divider {
            width: 100%;
            height: 34px;
            display: block;
        }

        .pulse-divider path {
            fill: none;
            stroke: var(--accent);
            stroke-width: 1.5;
        }

        /* ---------- Status monitor row ---------- */
        .monitor {
            background: var(--paper-raised);
            border: 1px solid var(--line);
            border-top: none;
            border-radius: 0 0 6px 6px;
            box-shadow: var(--shadow-soft);
            padding: 14px 32px;
            display: flex;
            flex-wrap: wrap;
            gap: 28px;
            margin-bottom: 28px;
            font-family: var(--mono);
            font-size: 12px;
        }

        .monitor-item {
            display: flex;
            align-items: center;
            gap: 8px;
            color: var(--ink-soft);
        }

        .dot {
            width: 7px;
            height: 7px;
            border-radius: 50%;
            background: var(--ink-faint);
            flex-shrink: 0;
        }

        .dot.ok { background: var(--triage-verde); box-shadow: 0 0 0 3px var(--triage-verde-bg); }
        .dot.bad { background: var(--triage-rojo); box-shadow: 0 0 0 3px var(--triage-rojo-bg); }

        /* ---------- Panels ---------- */
        .content {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 28px;
        }

        @media (max-width: 768px) {
            .content { grid-template-columns: 1fr; }
            .chart-header { padding: 22px 20px; }
            .chart-header h1 { font-size: 24px; }
            .intake-card { padding: 30px 24px; }
        }

        .panel {
            background: var(--paper-raised);
            border: 1px solid var(--line);
            border-radius: 6px;
            box-shadow: var(--shadow-soft);
            padding: 24px 26px;
            display: flex;
            flex-direction: column;
        }

        .panel-eyebrow {
            font-family: var(--mono);
            font-size: 10px;
            font-weight: 600;
            letter-spacing: 0.14em;
            text-transform: uppercase;
            color: var(--ink-faint);
            display: block;
            margin-bottom: 6px;
        }

        .panel h2 {
            font-size: 17px;
            font-weight: 600;
            margin-bottom: 18px;
            color: var(--ink);
        }

        /* ---------- Symptom checklist, grouped by category ---------- */
        #symptomContainer {
            border: 1px solid var(--line);
            border-radius: 4px;
            max-height: 340px;
            overflow-y: auto;
        }

        .symptom-group-label {
            font-family: var(--mono);
            font-size: 10.5px;
            font-weight: 600;
            letter-spacing: 0.1em;
            text-transform: uppercase;
            color: var(--accent-dark);
            background: var(--accent-soft);
            padding: 6px 14px;
            border-bottom: 1px solid var(--line);
        }

        .symptom-group + .symptom-group .symptom-group-label {
            border-top: 1px solid var(--line);
        }

        .symptom-list {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        }

        .symptom-item {
            display: flex;
            align-items: center;
            gap: 9px;
            padding: 9px 12px;
            border-bottom: 1px dashed var(--line);
            cursor: pointer;
            transition: background 0.12s ease;
        }

        .symptom-item:hover {
            background: var(--accent-soft);
        }

        .symptom-item input[type="checkbox"] {
            appearance: none;
            width: 14px;
            height: 14px;
            border: 1.5px solid var(--ink-faint);
            border-radius: 3px;
            flex-shrink: 0;
            cursor: pointer;
            position: relative;
        }

        .symptom-item input[type="checkbox"]:checked {
            background: var(--accent);
            border-color: var(--accent);
        }

        .symptom-item input[type="checkbox"]:checked::after {
            content: '';
            position: absolute;
            left: 3.5px;
            top: 0.5px;
            width: 4px;
            height: 8px;
            border: solid white;
            border-width: 0 1.5px 1.5px 0;
            transform: rotate(45deg);
        }

        .symptom-item label {
            font-family: var(--mono);
            font-size: 12px;
            letter-spacing: 0.01em;
            color: var(--ink);
            cursor: pointer;
        }

        /* ---------- Buttons ---------- */
        .button-group {
            display: flex;
            gap: 10px;
            margin-top: 18px;
        }

        button {
            flex: 1;
            padding: 12px 18px;
            border: 1.5px solid var(--accent);
            border-radius: 4px;
            cursor: pointer;
            font-family: var(--mono);
            font-size: 12px;
            font-weight: 600;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            transition: background 0.15s ease, color 0.15s ease, transform 0.1s ease;
        }

        .btn-diagnose {
            background: var(--accent);
            color: white;
        }

        .btn-diagnose:hover {
            background: var(--accent-dark);
            border-color: var(--accent-dark);
            transform: translateY(-1px);
        }

        .btn-clear {
            background: transparent;
            color: var(--accent);
        }

        .btn-clear:hover {
            background: var(--accent-soft);
        }

        button:focus-visible, .intake-input:focus-visible {
            outline: 2px solid var(--accent-dark);
            outline-offset: 2px;
        }

        /* ---------- Alert / error ---------- */
        .error {
            background: var(--triage-rojo-bg);
            color: var(--triage-rojo);
            border-left: 3px solid var(--triage-rojo);
            border-radius: 0 4px 4px 0;
            padding: 10px 14px;
            margin-top: 14px;
            display: none;
            font-family: var(--mono);
            font-size: 12px;
        }

        /* ---------- Loading: scanning line ---------- */
        .loading {
            display: none;
            padding: 30px 0;
            text-align: center;
        }

        .scan-bar {
            position: relative;
            width: 100%;
            height: 2px;
            background: var(--line);
            overflow: hidden;
            margin-bottom: 14px;
        }

        .scan-bar::after {
            content: '';
            position: absolute;
            top: 0; left: -30%;
            width: 30%;
            height: 100%;
            background: var(--accent);
            animation: scan 1.1s ease-in-out infinite;
        }

        @keyframes scan {
            0% { left: -30%; }
            100% { left: 100%; }
        }

        .loading p {
            font-family: var(--mono);
            font-size: 12px;
            color: var(--ink-soft);
            letter-spacing: 0.04em;
        }

        @media (prefers-reduced-motion: reduce) {
            .scan-bar::after { animation: none; left: 0; width: 100%; opacity: 0.5; }
        }

        /* ---------- Results ---------- */
        .results { display: none; margin-top: 4px; }

        .results-label {
            font-family: var(--mono);
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 0.1em;
            text-transform: uppercase;
            color: var(--ink-faint);
            margin-bottom: 4px;
        }

        #selectedSymptoms {
            font-family: var(--mono);
            font-size: 12px;
            color: var(--ink-soft);
            margin-bottom: 16px;
            padding-bottom: 14px;
            border-bottom: 1px dashed var(--line);
        }

        .record {
            background: var(--paper-raised);
            border: 1px solid var(--line);
            border-left: 4px solid var(--ink-faint);
            border-radius: 4px;
            box-shadow: var(--shadow-soft);
            padding: 16px 18px;
            margin-bottom: 12px;
            position: relative;
        }

        .record.triage-verde { border-left-color: var(--triage-verde); }
        .record.triage-amarillo { border-left-color: var(--triage-amarillo); }
        .record.triage-rojo { border-left-color: var(--triage-rojo); background: #FFFCFB; }

        .record-head {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 12px;
            margin-bottom: 10px;
        }

        .record-head h3 {
            font-size: 16px;
            font-weight: 600;
            color: var(--ink);
        }

        .confidence-readout {
            text-align: right;
            flex-shrink: 0;
        }

        .confidence-readout .value {
            font-family: var(--mono);
            font-size: 20px;
            font-weight: 600;
            font-variant-numeric: tabular-nums;
            line-height: 1;
            color: var(--ink);
        }

        .confidence-readout .caption {
            font-family: var(--mono);
            font-size: 9px;
            letter-spacing: 0.1em;
            color: var(--ink-faint);
            text-transform: uppercase;
        }

        .confidence-bar {
            width: 100%;
            height: 3px;
            background: var(--line);
            border-radius: 2px;
            margin-bottom: 12px;
            overflow: hidden;
        }

        .confidence-bar-fill {
            height: 100%;
            background: var(--accent);
        }

        .triage-stamp {
            display: inline-block;
            font-family: var(--mono);
            font-weight: 700;
            font-size: 10.5px;
            letter-spacing: 0.1em;
            text-transform: uppercase;
            padding: 3px 9px;
            border: 1.5px solid currentColor;
            border-radius: 2px;
            transform: rotate(-2deg);
            margin-bottom: 10px;
        }

        .triage-verde .triage-stamp { color: var(--triage-verde); }
        .triage-amarillo .triage-stamp { color: var(--triage-amarillo); }
        .triage-rojo .triage-stamp { color: var(--triage-rojo); }

        .diagnosis-description {
            color: var(--ink-soft);
            font-size: 13.5px;
            line-height: 1.55;
            margin-bottom: 12px;
        }

        .specialist-tag {
            font-family: var(--mono);
            font-size: 11.5px;
            color: var(--accent-dark);
            background: var(--accent-soft);
            border-radius: 3px;
            padding: 5px 10px;
            display: inline-block;
        }

        #noResults {
            color: var(--ink-faint);
            font-size: 13px;
            font-style: italic;
            padding: 10px 0;
        }

        footer {
            text-align: center;
            color: var(--ink-faint);
            font-family: var(--mono);
            font-size: 11px;
            letter-spacing: 0.03em;
            margin-top: 32px;
        }
    </style>
</head>
<body>

    <div id="landingScreen" class="container">
        <nav class="landing-nav">
            <span class="brand"><span class="brand-icon"></span>Asistente de Diagnóstico Médico</span>
            <button class="btn-diagnose btn-inline" onclick="showWelcomeScreen()">Iniciar sesión</button>
        </nav>

        <section class="hero">
            <span class="eyebrow">Tu asistente médico inteligente</span>
            <h1>Análisis de síntomas guiado por un motor de reglas clínicas</h1>
            <p class="hero-sub">Selecciona lo que sientes y recibe una orientación preliminar sobre posibles diagnósticos, su nivel de urgencia y el especialista recomendado.</p>
            <button class="btn-diagnose btn-inline btn-hero-cta" style="padding:14px 32px; font-size:13px;" onclick="showWelcomeScreen()">Ir al Asistente Médico</button>
        </section>

        <h2 class="section-title">¿Cómo Funciona?</h2>
        <div class="feature-grid">
            <div class="feature-card">
                <div class="feature-icon">🩺</div>
                <h3>Análisis de Síntomas</h3>
                <p>Selecciona tus síntomas de forma sencilla, organizados por categoría clínica.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">📚</div>
                <h3>Base de Conocimiento</h3>
                <p>Un motor de reglas clínicas evalúa tus síntomas contra enfermedades comunes.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">📊</div>
                <h3>Diagnósticos con Confianza</h3>
                <p>Recibe posibles diagnósticos con un nivel de confianza para cada uno.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">🏥</div>
                <h3>Recomendaciones</h3>
                <p>Obtén el nivel de urgencia y el especialista recomendado para tu caso.</p>
            </div>
        </div>

        <div class="checklist-section">
            <h2 class="section-title">Características Principales</h2>
            <div class="checklist">
                <div class="checklist-item"><span class="check">✓</span> Análisis rápido de síntomas por categoría</div>
                <div class="checklist-item"><span class="check">✓</span> Interfaz sencilla y fácil de usar</div>
                <div class="checklist-item"><span class="check">✓</span> Información detallada de cada posible diagnóstico</div>
                <div class="checklist-item"><span class="check">✓</span> Recomendación de especialista y nivel de triaje</div>
                <div class="checklist-item"><span class="check">✓</span> Tus síntomas se procesan únicamente en este sistema, sin enviarse a servidores externos</div>
                <div class="checklist-item"><span class="check">✓</span> Disponible las veces que lo necesites</div>
            </div>
        </div>

        <div class="cta-banner">
            <h2>¿Listo para comenzar?</h2>
            <p>Accede ahora a tu asistente médico y obtén un diagnóstico preliminar basado en tus síntomas.</p>
            <button class="btn-diagnose btn-inline btn-cta-white" onclick="showWelcomeScreen()">Acceder al Asistente</button>
        </div>

        <div class="landing-footer">
            <p>© 2026 Asistente de Diagnóstico Médico. Proyecto académico. Todos los derechos reservados.</p>
        </div>
    </div>

    <div id="welcomeScreen" class="welcome-screen" style="display:none;">
        <div class="intake-card">
            <span class="eyebrow">Registro de paciente</span>
            <h1>¿Cómo te llamas?</h1>
            <p class="intake-sub">Usaremos tu nombre para personalizar esta sesión de evaluación de síntomas.</p>
            <form id="nameForm" onsubmit="return handleNameSubmit(event)">
                <input type="text" id="patientName" class="intake-input" placeholder="Escribe tu nombre" autocomplete="off">
                <button type="submit" class="btn-diagnose" style="width:100%; margin-top:14px;">Continuar</button>
            </form>
            <a class="intake-back" onclick="showLandingScreen()">&larr; Volver al inicio</a>
        </div>
    </div>

    <div id="mainScreen" class="container" style="display:none;">
        <header class="chart-header">
            <button class="btn-logout" onclick="logout()">Cerrar sesión</button>
            <span class="eyebrow">Sistema de apoyo al diagnóstico</span>
            <h1>Asistente de Diagnóstico Médico</h1>
            <p class="welcome-msg">Bienvenido/a, <b id="patientNameDisplay"></b> — selecciona tus síntomas y obtén tu diagnóstico preliminar.</p>
        </header>

        <svg class="pulse-divider" viewBox="0 0 1020 34" preserveAspectRatio="none">
            <path d="M0,17 L400,17 L418,17 L428,3 L440,31 L452,10 L462,17 L480,17 L1020,17" />
        </svg>

        <div class="monitor">
            <div class="monitor-item"><span class="dot" id="pythonDot"></span><span id="pythonStatus">Verificando…</span></div>
            <div class="monitor-item"><span class="dot ok"></span><span>Sesión activa</span></div>
            <div class="monitor-item"><span id="systemStatus">Verificando estado del sistema…</span></div>
        </div>

        <div class="content">
            <div class="panel">
                <span class="panel-eyebrow">01 — Formulario</span>
                <h2>Síntomas del paciente</h2>
                <div id="symptomContainer">
                    <p style="padding:14px; font-family:var(--mono); font-size:12px; color:var(--ink-faint);">Cargando síntomas…</p>
                </div>
                <div class="button-group">
                    <button class="btn-diagnose" onclick="diagnose()">Obtener diagnóstico</button>
                    <button class="btn-clear" onclick="clearSymptoms()">Limpiar</button>
                </div>
                <div class="error" id="error"></div>
            </div>

            <div class="panel">
                <span class="panel-eyebrow">02 — Lectura</span>
                <h2>Resultado diagnóstico</h2>
                <div id="loading" class="loading">
                    <div class="scan-bar"></div>
                    <p>Analizando síntomas…</p>
                </div>
                <div id="results" class="results">
                    <p class="results-label">Síntomas ingresados</p>
                    <p id="selectedSymptoms"></p>
                    <p class="results-label">Posibles diagnósticos</p>
                    <div id="diagnosisList" style="margin-top:10px;"></div>
                </div>
                <p id="noResults">Selecciona síntomas y haz clic en "Obtener diagnóstico"</p>
            </div>
        </div>

        <footer>
            <p>Proyecto académico · Asistente de Diagnóstico Médico</p>
        </footer>
    </div>

    <script>
        const API_BASE = window.location.origin;
        const STORAGE_KEY = 'diag_patient_name';

        // Categorías clínicas: agrupan los síntomas devueltos por la API.
        // Cualquier síntoma que no encaje en una categoría cae en "Otros".
        const SYMPTOM_CATEGORIES = [
            { label: 'Respiratorios', items: ['tos', 'congestion_nasal', 'dolor_garganta', 'estornudos', 'dificultad_respirar', 'dolor_pecho', 'dificultad_tragar', 'perdida_olfato'] },
            { label: 'Digestivos', items: ['vomitos', 'diarrea', 'dolor_abdominal', 'perdida_apetito', 'acidez'] },
            { label: 'Generales / Sistémicos', items: ['fiebre', 'fatiga', 'escalofrios', 'cuerpo_adolorido', 'dolor_articular', 'sudoracion_nocturna', 'mareo', 'perdida_peso'] },
            { label: 'Piel', items: ['erupciones_piel', 'picazon', 'hinchazon'] },
            { label: 'Neurológicos', items: ['dolor_cabeza', 'sensibilidad_luz'] },
            { label: 'Urinarios', items: ['dolor_orinar', 'sangre_orina'] }
        ];

        document.addEventListener('DOMContentLoaded', function() {
            const savedName = localStorage.getItem(STORAGE_KEY);
            if (savedName) {
                showMainScreen(savedName);
            }
            // Si no hay nombre guardado, se queda en la landing page (pantalla por defecto).
        });

        function showWelcomeScreen() {
            document.getElementById('landingScreen').style.display = 'none';
            document.getElementById('welcomeScreen').style.display = 'flex';
            document.getElementById('patientName').focus();
        }

        function showLandingScreen() {
            document.getElementById('welcomeScreen').style.display = 'none';
            document.getElementById('mainScreen').style.display = 'none';
            document.getElementById('landingScreen').style.display = 'block';
        }

        function handleNameSubmit(event) {
            event.preventDefault();
            const input = document.getElementById('patientName');
            const name = input.value.trim();
            if (!name) {
                input.focus();
                return false;
            }
            localStorage.setItem(STORAGE_KEY, name);
            showMainScreen(name);
            return false;
        }

        function showMainScreen(name) {
            document.getElementById('patientNameDisplay').textContent = name;
            document.getElementById('landingScreen').style.display = 'none';
            document.getElementById('welcomeScreen').style.display = 'none';
            document.getElementById('mainScreen').style.display = 'block';
            loadSymptoms();
            checkSystemStatus();
        }

        function logout() {
            localStorage.removeItem(STORAGE_KEY);
            clearSymptoms();
            clearError();
            document.getElementById('patientName').value = '';
            showLandingScreen();
        }

        async function checkSystemStatus() {
            try {
                const response = await fetch(API_BASE + '/api/status');
                if (response.ok) {
                    document.getElementById('pythonStatus').textContent = 'Motor de diagnóstico — conectado';
                    document.getElementById('pythonDot').classList.add('ok');
                    document.getElementById('systemStatus').textContent = 'Sistema funcionando correctamente';
                }
            } catch (e) {
                document.getElementById('pythonStatus').textContent = 'Motor de diagnóstico — error de conexión';
                document.getElementById('pythonDot').classList.add('bad');
                document.getElementById('systemStatus').textContent = 'No se pudo verificar el estado del sistema';
            }
        }

        function buildSymptomGroups(symptoms) {
            const used = new Set();
            const groups = SYMPTOM_CATEGORIES.map(cat => {
                const present = cat.items.filter(s => symptoms.includes(s));
                present.forEach(s => used.add(s));
                return { label: cat.label, items: present };
            }).filter(g => g.items.length > 0);

            const rest = symptoms.filter(s => !used.has(s));
            if (rest.length > 0) {
                groups.push({ label: 'Otros', items: rest });
            }
            return groups;
        }

        async function loadSymptoms() {
            try {
                const response = await fetch(API_BASE + '/api/symptoms');
                const data = await response.json();
                const symptoms = data.sintomas_disponibles || [];

                const container = document.getElementById('symptomContainer');
                container.innerHTML = '';

                const groups = buildSymptomGroups(symptoms);

                groups.forEach(group => {
                    const section = document.createElement('div');
                    section.className = 'symptom-group';

                    const heading = document.createElement('div');
                    heading.className = 'symptom-group-label';
                    heading.textContent = group.label;
                    section.appendChild(heading);

                    const grid = document.createElement('div');
                    grid.className = 'symptom-list';

                    group.items.forEach(symptom => {
                        const id = 'symptom_' + symptom;
                        const div = document.createElement('div');
                        div.className = 'symptom-item';
                        div.innerHTML = `
                            <input type="checkbox" id="${id}" value="${symptom}">
                            <label for="${id}">${formatSymptomName(symptom)}</label>
                        `;
                        grid.appendChild(div);
                    });

                    section.appendChild(grid);
                    container.appendChild(section);
                });
            } catch (error) {
                console.error('Error cargando síntomas:', error);
                document.getElementById('symptomContainer').innerHTML = '<p style="padding:14px; font-family:var(--mono); font-size:12px; color:var(--triage-rojo);">Error cargando síntomas</p>';
            }
        }

        function formatSymptomName(symptom) {
            return symptom
                .replace(/_/g, ' ')
                .split(' ')
                .map(word => word.charAt(0).toUpperCase() + word.slice(1))
                .join(' ');
        }

        async function diagnose() {
            const checkboxes = document.querySelectorAll('input[type="checkbox"]:checked');
            const symptoms = Array.from(checkboxes).map(cb => cb.value);

            if (symptoms.length === 0) {
                showError('Por favor selecciona al menos un síntoma');
                return;
            }

            showLoading(true);
            clearError();

            try {
                const response = await fetch(API_BASE + '/api/diagnose', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ symptoms: symptoms })
                });

                if (!response.ok) {
                    throw new Error('Error en la respuesta del servidor');
                }

                const data = await response.json();
                displayResults(data);
            } catch (error) {
                console.error('Error:', error);
                showError('Error al procesar el diagnóstico: ' + error.message);
            } finally {
                showLoading(false);
            }
        }

        const TRIAGE_LABEL = {
            rojo: 'Urgencia alta — acudir a emergencias',
            amarillo: 'Precaución — agendar cita médica',
            verde: 'Estable — manejo de síntomas en casa'
        };

        function displayResults(data) {
            const diagnosisList = data.diagnosticos_posibles || [];

            if (diagnosisList.length === 0) {
                showError('No se encontraron diagnósticos para los síntomas ingresados');
                return;
            }

            document.getElementById('selectedSymptoms').textContent = data.sintomas_ingresados
                .map(s => formatSymptomName(s))
                .join(', ');

            const diagnosisHtml = diagnosisList.map(diagnosis => {
                const porcentaje = Math.round(diagnosis.confianza * 100);
                const triaje = (diagnosis.triaje || 'amarillo').toLowerCase();
                const stampLabel = triaje === 'rojo' ? 'Urgente' : (triaje === 'verde' ? 'Estable' : 'Precaución');
                const textoDescripcion = diagnosis.descripción || diagnosis.descripcion || 'Información no disponible.';
                const especialista = diagnosis.especialista || 'Médico General';

                return `
                <div class="record triage-${triaje}">
                    <div class="record-head">
                        <div>
                            <span class="triage-stamp">${stampLabel}</span>
                            <h3>${formatSymptomName(diagnosis.enfermedad)}</h3>
                        </div>
                        <div class="confidence-readout">
                            <div class="value">${porcentaje}%</div>
                            <div class="caption">Confianza</div>
                        </div>
                    </div>
                    <div class="confidence-bar"><div class="confidence-bar-fill" style="width:${porcentaje}%;"></div></div>
                    <p style="font-family:var(--mono); font-size:11px; color:var(--ink-soft); margin-bottom:10px;">${TRIAGE_LABEL[triaje] || ''}</p>
                    <p class="diagnosis-description">${textoDescripcion}</p>
                    <p class="results-label" style="margin-top:2px; margin-bottom:6px;">Recomendación de especialista</p>
                    <span class="specialist-tag">${especialista}</span>
                </div>
                `;
            }).join('');

            document.getElementById('diagnosisList').innerHTML = diagnosisHtml;
            document.getElementById('noResults').style.display = 'none';
            document.getElementById('results').style.display = 'block';
        }

        function clearSymptoms() {
            document.querySelectorAll('input[type="checkbox"]').forEach(cb => cb.checked = false);
            document.getElementById('results').style.display = 'none';
            document.getElementById('noResults').style.display = 'block';
            clearError();
        }

        function showLoading(show) {
            document.getElementById('loading').style.display = show ? 'block' : 'none';
        }

        function showError(message) {
            const errorDiv = document.getElementById('error');
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
        }

        function clearError() {
            document.getElementById('error').style.display = 'none';
        }
    </script>
</body>
</html>"""
  }
}