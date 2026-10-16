<!-- Nombre del archivo: communication_design.md
Autor: Imanol Figuero Parras
Fecha: 2026
Proyecto: Biometría y Monitorización - Sprint 0
Descripción: Adaptador REST. Copyright: 2026 Imanol Figuero Parras.
Aportación: contrato HTTP uniforme. -->
# Component Design
`Payload = (tipo_medicion: Z, valor: R)`.
`RespuestaREST = (codigo: Z, cuerpo: Text)`.
```text
payload: Text --> guardarMedicion.php() --> respuesta: RespuestaREST
obtenerUltimaMedicion.php() --> respuesta: RespuestaREST
codigo: Z, exito: B, medicion: MedicionOpcional, error: Text --> responder() --x
crearLogica() --x --> logica: LogicaNegocio
mensaje: Text --> registrarError() --x
```
# Design Clarifications
- Endpoints y códigos documentados en `docs/especificaciones.md`.
- `POST` valida estructura, tipos JSON y tamaño antes de construir la conexión.
- Dominio valida rangos; el transporte convierte excepciones a JSON estable.
- Configuración por entorno; despliegue mismo origen. No CORS permisivo.
- Fuentes: `backend/rest/`; adaptadores ejecutables `src/communication/`.
# General Rules
- Programming Language: PHP 8.2+.
- Function/Method Headers: firma v3 delimitada con `--------------------`.
- Code Readability: transporte -> dominio -> PDO, nunca en sentido inverso.
- Automated Testing: integración sobre métodos, MIME, JSON, dominio y fallos.
