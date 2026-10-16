<!-- Nombre del archivo: business_logic_design.md
Autor: Imanol Figuero Parras
Fecha: 2026
Proyecto: Biometría y Monitorización - Sprint 0
Descripción: Dominio independiente. Copyright: 2026 Imanol Figuero Parras.
Aportación: contrato de persistencia de mediciones. -->
# Component Design
Tipos: `Medicion`, `MedicionOpcional` según `docs/especificaciones.md`.
`Almacen` es la capacidad de persistir las cuatro columnas de `database_design.md`.
`Reloj` es la capacidad de obtener el instante UTC.
```text
                          -------- LogicaNegocio ----------------
                          | almacen: Almacen
                          | reloj: Reloj
                          |
almacen: Almacen, reloj: Reloj --> __construct() -->
                          |
                          |
tipo: Z, valor: R      --> guardarMedicion() -->
exito: B              <--
                          |
                          |
medicion: MedicionOpcional <-- obtenerUltimaMedicion() <--
                          |
                          |
tipo: Z, valor: R      --> validarMedicion() --x
                          |
                          ---------------------------------------
```
# Design Clarifications
- `guardarMedicion` valida, calcula el timestamp e inserta tipo, valor y timestamp; el identificador se genera en `mediciones`.
- `obtenerUltimaMedicion` ordena por id descendente. La ausencia no es error.
- Errores de dominio y de almacenamiento se propagan, sin ocultarlos.
- Fuente canónica: `backend/domain/LogicaNegocio.php`; entrada compatible: `src/business_logic/LogicaNegocio.php`.
# General Rules
- Programming Language: PHP 8.2+, tipos estrictos, PDO preparado e inyectado.
- Function/Method Headers: firma v3 entre líneas `--------------------`.
- Code Readability: nombres claros y ausencia de dependencias de comunicación.
- Automated Testing: PHPUnit con PDO simulado; integración sobre MariaDB aislada.
