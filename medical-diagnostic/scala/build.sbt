name := "MedicalDiagnosticSystem"
version := "1.0.0"
scalaVersion := "2.13.12"

// Dependencias (mínimas, sin frameworks innecesarios)
libraryDependencies ++= Seq()

// Configuración de compilación
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:postfixOps"
)

// Punto de entrada
mainClass in (Compile, run) := Some("MedicalDiagnosticServer")

// Configuración para ejecutar sin argumentos adicionales
fork in run := true
