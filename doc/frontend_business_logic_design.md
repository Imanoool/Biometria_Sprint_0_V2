<!-- Nombre del archivo: frontend_business_logic_design.md
Autor: Imanol Figuero Parras
Fecha: 2026
Proyecto: Biometría y Monitorización - Sprint 0
Descripción: Interfaz del proxy y fake. Copyright: 2026 Imanol Figuero Parras.
Aportación: paridad de dominio. -->
# Component Design
Única operación pública de dominio, idéntica en nombre, entradas y salida al backend:
```text
obtenerUltimaMedicion() --> medicion: MedicionOpcional
```
`Medicion` y `MedicionOpcional` coinciden con `business_logic_design.md`.
# Design Clarifications
- Proxy real: `web/js/ux_logica.js`; fake: `web/js/fake/fake_ux_logica.js`.
- Promises son un mecanismo de implementación, no parte del tipo lógico.
- Constructores reciben configuración de transporte o datos simulados; no son operaciones de dominio.
- El proxy encapsula fetch, timeout y validación de respuesta; fake no usa red.
- Reexportación ejecutable en `src/frontend_business_logic/`.
# General Rules
- Programming Language: JavaScript ES modules.
- Function/Method Headers: firma v3 delimitada con `--------------------`.
- Code Readability: ninguna dependencia de DOM en proxy/fake.
- Automated Testing: respuestas válidas, vacías, errores, formato inválido y fake.
