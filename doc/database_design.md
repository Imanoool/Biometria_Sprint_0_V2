<!-- Nombre del archivo: database_design.md
Autor: Imanol Figuero Parras
Fecha: 2026
Proyecto: Biometría y Monitorización - Sprint 0
Descripción: Esquema relacional. Copyright: 2026 Imanol Figuero Parras.
Aportación: tabla única V2. -->
# Component Design
```text
====================================================================================
TABLE: mediciones
DESCRIPTION: Mediciones ambientales recibidas por el gateway.
COLUMNS:
+ id_medicion | INT | NOT NULL | AUTO_INCREMENT
+ tipo_medicion | INT | NOT NULL | sin valor por defecto
+ valor | DOUBLE | NOT NULL | sin valor por defecto
+ timestamp | BIGINT | NOT NULL | sin valor por defecto
PRIMARY KEY: id_medicion
FOREIGN KEYS:
+ Ninguna.
CONSTRAINTS:
+ CHECK (tipo_medicion IN (11,12,13))
+ CHECK (timestamp >= 0)
+ CHECK ((tipo_medicion = 12 AND valor BETWEEN -32768 AND 32767)
         OR (tipo_medicion IN (11,13) AND valor BETWEEN 0 AND 65535))
====================================================================================
```
# Design Clarifications
- No tablas auxiliares, usuarios ni columnas adicionales. El contador solo viaja por BLE.
- Los índices no añaden campos; basta la clave primaria para la última inserción.
- Seeds de diez filas docentes, ejecutados una sola vez al inicializar el volumen.
- Fuente canónica: `backend/config/schema.sql`; comprobador: `src/database/verificarEsquema.php`.
# General Rules
- Programming Language: SQL MySQL 8.0.16+ / MariaDB 10.11.
- Function/Method Headers: contratos v3 delimitados con `--------------------`.
- Code Readability: restricciones nombradas, sin SQL dinámico.
- Automated Testing: verificar columnas, inserción y rechazo de restricciones.
