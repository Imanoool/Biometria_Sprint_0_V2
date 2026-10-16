<!-- Nombre del archivo: gui_design.md
Autor: Imanol Figuero Parras
Fecha: 2026
Proyecto: Biometría y Monitorización - Sprint 0
Descripción: Pantalla de monitorización. Copyright: 2026 Imanol Figuero Parras.
Aportación: separación GUI/proxy. -->
# Component Design
```text
medicion: MedicionOpcional --> renderizarMedicion() --> 
proxy: LogicaCliente --> iniciarPanel() --> 
actualizar() -->
detener() -->
```
Pantalla única: título, estado de conexión anunciado mediante `aria-live`, modo REAL/FAKE, KPI principal, tipo, unidad y momento de recepción. Vacío muestra “Sin mediciones”. Error mantiene el valor anterior como obsoleto. CO2 cambia de nivel a 800 y 1000 ppm.
# Design Clarifications
- HTML semántico y CSS responsive; ninguna petición de red en GUI.
- Refresco tras finalizar petición anterior; cierre cancela programación y evita actualizar DOM.
- `?fake=1` habilita simulación explícita; nunca fallback silencioso ante fallo real.
- Implementación: `web/js/dashboard.js`, `web/index.html`, `web/css/styles.css`; reexportación `src/gui/`.
# General Rules
- Programming Language: HTML5, CSS3, JavaScript.
- Function/Method Headers: firma v3 delimitada con `--------------------`.
- Code Readability: transporte exclusivamente en proxy.
- Automated Testing: Playwright con AJAX simulado y E2E contra servidor real.
