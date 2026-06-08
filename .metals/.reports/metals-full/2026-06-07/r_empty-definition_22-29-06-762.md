error id: file:///C:/Users/angel/Downloads/Proyecto%20Lenguaje%20de%20Programación/Asistente-diagnostico-medico/medical-diagnostic/scala/src/main/scala/Main.scala:java/net/HttpURLConnection#disconnect().
file:///C:/Users/angel/Downloads/Proyecto%20Lenguaje%20de%20Programación/Asistente-diagnostico-medico/medical-diagnostic/scala/src/main/scala/Main.scala
empty definition using pc, found symbol in pc: java/net/HttpURLConnection#disconnect().
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -connection/disconnect.
	 -connection/disconnect#
	 -connection/disconnect().
	 -scala/Predef.connection.disconnect.
	 -scala/Predef.connection.disconnect#
	 -scala/Predef.connection.disconnect().
offset: 7896
uri: file:///C:/Users/angel/Downloads/Proyecto%20Lenguaje%20de%20Programación/Asistente-diagnostico-medico/medical-diagnostic/scala/src/main/scala/Main.scala
text:
```scala
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
          exchange.getResponseHeaders.set("Content-Type", "text/html; charset=UTF-8")
          exchange.sendResponseHeaders(200, htmlContent.getBytes.length)
          exchange.getResponseBody.write(htmlContent.getBytes)
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
            
            exchange.getResponseHeaders.set("Content-Type", "application/json; charset=UTF-8")
            exchange.getResponseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.sendResponseHeaders(200, result.getBytes.length)
            exchange.getResponseBody.write(result.getBytes)
            exchange.close()
          } catch {
            case e: Exception =>
              val error = s"""{"error": "${e.getMessage}"}"""
              exchange.getResponseHeaders.set("Content-Type", "application/json")
              exchange.sendResponseHeaders(500, error.getBytes.length)
              exchange.getResponseBody.write(error.getBytes)
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
          exchange.getResponseHeaders.set("Content-Type", "application/json; charset=UTF-8")
          exchange.getResponseHeaders.set("Access-Control-Allow-Origin", "*")
          exchange.sendResponseHeaders(200, result.getBytes.length)
          exchange.getResponseBody.write(result.getBytes)
          exchange.close()
        }
      }
    })
    
    // Ruta API para obtener enfermedades conocidas
    server.createContext("/api/diseases", new com.sun.net.httpserver.HttpHandler {
      def handle(exchange: com.sun.net.httpserver.HttpExchange): Unit = {
        if (exchange.getRequestMethod == "GET") {
          val result = callPythonEndpoint("/diseases")
          exchange.getResponseHeaders.set("Content-Type", "application/json; charset=UTF-8")
          exchange.getResponseHeaders.set("Access-Control-Allow-Origin", "*")
          exchange.sendResponseHeaders(200, result.getBytes.length)
          exchange.getResponseBody.write(result.getBytes)
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
        exchange.getResponseHeaders.set("Content-Type", "application/json; charset=UTF-8")
        exchange.sendResponseHeaders(200, status.getBytes.length)
        exchange.getResponseBody.write(status.getBytes)
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
      
      connection.@@disconnect()
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
    <link rel="stylesheet" href="style.css">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 1000px;
            margin: 0 auto;
        }
        
        header {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            margin-bottom: 30px;
            text-align: center;
        }
        
        header h1 {
            color: #667eea;
            margin-bottom: 10px;
        }
        
        header p {
            color: #666;
            font-size: 14px;
        }
        
        .content {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 30px;
        }
        
        @media (max-width: 768px) {
            .content {
                grid-template-columns: 1fr;
            }
        }
        
        .card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        }
        
        .card h2 {
            color: #667eea;
            margin-bottom: 20px;
            font-size: 20px;
        }
        
        .form-group {
            margin-bottom: 15px;
        }
        
        label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
        }
        
        .symptom-list {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
            gap: 10px;
            max-height: 300px;
            overflow-y: auto;
            border: 1px solid #eee;
            padding: 15px;
            border-radius: 5px;
        }
        
        .symptom-item {
            display: flex;
            align-items: center;
        }
        
        .symptom-item input[type="checkbox"] {
            margin-right: 8px;
            cursor: pointer;
        }
        
        .symptom-item label {
            margin: 0;
            cursor: pointer;
            font-weight: normal;
        }
        
        .button-group {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }
        
        button {
            flex: 1;
            padding: 12px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s ease;
        }
        
        .btn-diagnose {
            background: #667eea;
            color: white;
        }
        
        .btn-diagnose:hover {
            background: #5568d3;
            transform: translateY(-2px);
            box-shadow: 0 6px 12px rgba(102, 126, 234, 0.4);
        }
        
        .btn-clear {
            background: #f0f0f0;
            color: #333;
        }
        
        .btn-clear:hover {
            background: #e0e0e0;
        }
        
        .loading {
            display: none;
            text-align: center;
            padding: 20px;
        }
        
        .spinner {
            border: 3px solid #f3f3f3;
            border-top: 3px solid #667eea;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 0 auto;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        .results {
            display: none;
            margin-top: 20px;
        }
        
        .diagnosis-card {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 12px;
            border-left: 4px solid #667eea;
        }
        
        .diagnosis-card h3 {
            color: #333;
            margin-bottom: 8px;
            font-size: 16px;
        }
        
        .diagnosis-description {
            color: #666;
            font-size: 14px;
            margin-bottom: 8px;
        }
        
        .confidence {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
        }
        
        .error {
            background: #fee;
            color: #c33;
            padding: 15px;
            border-radius: 5px;
            margin-top: 15px;
            display: none;
            border-left: 4px solid #c33;
        }
        
        .status {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        
        .status-item {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid #eee;
            font-size: 14px;
        }
        
        .status-item:last-child {
            border-bottom: none;
        }
        
        .status-label {
            font-weight: 600;
            color: #333;
        }
        
        .status-value {
            color: #666;
        }
        
        .status-ok {
            color: #28a745;
        }
        
        footer {
            text-align: center;
            color: white;
            font-size: 12px;
            margin-top: 30px;
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>🏥 Asistente de Diagnóstico Médico</h1>
            <p>Sistema integrado: Scala (Orquestador) → Python (Intermediario) → Prolog (Base de Conocimientos)</p>
        </header>
        
        <div class="status">
            <div class="status-item">
                <span class="status-label">Estado del Sistema:</span>
                <span class="status-value" id="systemStatus">Verificando...</span>
            </div>
            <div class="status-item">
                <span class="status-label">Módulo Scala:</span>
                <span class="status-value status-ok">Activo</span>
            </div>
            <div class="status-item">
                <span class="status-label">Servidor Frontend:</span>
                <span class="status-value" id="pythonStatus">Verificando...</span>
            </div>
        </div>
        
        <div class="content">
            <div class="card">
                <h2>Seleccione sus Síntomas</h2>
                <div id="symptomContainer" class="symptom-list">
                    <p>Cargando síntomas...</p>
                </div>
                <div class="button-group">
                    <button class="btn-diagnose" onclick="diagnose()">Obtener Diagnóstico</button>
                    <button class="btn-clear" onclick="clearSymptoms()">Limpiar</button>
                </div>
                <div class="error" id="error"></div>
            </div>
            
            <div class="card">
                <h2>Resultados del Diagnóstico</h2>
                <div id="loading" class="loading">
                    <div class="spinner"></div>
                    <p>Analizando síntomas...</p>
                </div>
                <div id="results" class="results">
                    <p><strong>Síntomas ingresados:</strong></p>
                    <p id="selectedSymptoms" style="color: #666; margin-bottom: 15px;"></p>
                    <p><strong>Posibles diagnósticos:</strong></p>
                    <div id="diagnosisList"></div>
                </div>
                <p id="noResults" style="color: #999; font-style: italic;">Selecciona síntomas y haz clic en "Obtener Diagnóstico"</p>
            </div>
        </div>
        
        <footer>
            <p>Proyecto Académico - Asistente de Diagnóstico Médico | Scala · Python · Prolog</p>
        </footer>
    </div>
    
    <script>
        const API_BASE = window.location.origin;
        
        // Cargar síntomas al iniciar
        document.addEventListener('DOMContentLoaded', function() {
            loadSymptoms();
            checkSystemStatus();
        });
        
        async function checkSystemStatus() {
            try {
                const response = await fetch(API_BASE + '/api/status');
                if (response.ok) {
                    document.getElementById('pythonStatus').textContent = '✓ Conectado';
                    document.getElementById('pythonStatus').style.color = '#28a745';
                    document.getElementById('systemStatus').textContent = 'Funcionando correctamente';
                    document.getElementById('systemStatus').style.color = '#28a745';
                }
            } catch (e) {
                document.getElementById('pythonStatus').textContent = '✗ Error de conexión';
                document.getElementById('pythonStatus').style.color = '#dc3545';
            }
        }
        
        async function loadSymptoms() {
            try {
                const response = await fetch(API_BASE + '/api/symptoms');
                const data = await response.json();
                const symptoms = data.sintomas_disponibles || [];
                
                const container = document.getElementById('symptomContainer');
                container.innerHTML = '';
                
                symptoms.forEach(symptom => {
                    const id = 'symptom_' + symptom;
                    const div = document.createElement('div');
                    div.className = 'symptom-item';
                    div.innerHTML = `
                        <input type="checkbox" id="${id}" value="${symptom}">
                        <label for="${id}">${formatSymptomName(symptom)}</label>
                    `;
                    container.appendChild(div);
                });
            } catch (error) {
                console.error('Error cargando síntomas:', error);
                document.getElementById('symptomContainer').innerHTML = '<p style="color: red;">Error cargando síntomas</p>';
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
        
        function displayResults(data) {
            const diagnosisList = data.diagnosticos_posibles || [];
            
            if (diagnosisList.length === 0) {
                showError('No se encontraron diagnósticos para los síntomas ingresados');
                return;
            }
            
            // Mostrar síntomas ingresados
            document.getElementById('selectedSymptoms').textContent = data.sintomas_ingresados
                .map(s => formatSymptomName(s))
                .join(', ');
            
            // Mostrar diagnósticos
            const diagnosisHtml = diagnosisList.map(diagnosis => {
                // Calcular el porcentaje
                const porcentaje = Math.round(diagnosis.confianza * 100);
                
                // Asignar color según el nivel de confianza
                let colorClase = '#dc3545'; // Rojo por defecto (< 50%)
                if (porcentaje >= 85) {
                    colorClase = '#28a745'; // Verde para alta confianza
                } else if (porcentaje >= 50) {
                    colorClase = '#ffc107'; // Amarillo/Naranja para media confianza
                    // Si es amarillo, cambiamos el texto a oscuro para que se lea bien
                }

                const textColor = porcentaje >= 50 && porcentaje < 85 ? '#333' : 'white';
                
                return `
                <div class="diagnosis-card">
                    <h3>${formatSymptomName(diagnosis.enfermedad)}</h3>
                    <p class="diagnosis-description" style="margin-bottom: 12px; color: #555; font-size: 14px;">${diagnosis.descripcion}</p>
                    <span class="confidence" style="background-color: ${colorClase}; color: ${textColor};">Confianza: ${porcentaje}%</span>
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

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/net/HttpURLConnection#disconnect().