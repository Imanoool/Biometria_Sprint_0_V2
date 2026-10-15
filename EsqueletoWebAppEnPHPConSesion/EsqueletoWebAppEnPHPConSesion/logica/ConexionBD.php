<?php
declare(strict_types=1);

// ============================================================================
// Proyecto: Biometría y Medio Ambiente
// Asignatura: Proyecto Biometría (PBIO) - Sprint 0
// Archivo: ConexionBD.php
// Descripción: Gestor de conexión a la base de datos utilizando PDO con tipado estricto
//              y sentencias preparadas para máxima seguridad contra inyección SQL.
//
// ----------------------------------------------------------------------------
// () -> ConexionBD::getConexion() -> PDO
// ----------------------------------------------------------------------------
// ============================================================================

class ConexionBD {
    private static ?PDO $conexion = null;

    /**
     * Obtiene una instancia única de PDO (patrón Singleton).
     * Intenta conectar con MySQL (XAMPP por defecto) y si no está disponible,
     * conmuta de forma transparente y automática a SQLite garantizando portabilidad.
     *
     * @return PDO Objeto de conexión PDO configurado
     */
    public static function getConexion(): PDO {
        if (self::$conexion !== null) {
            return self::$conexion;
        }

        // Configuración de MySQL (por defecto en entornos de desarrollo / XAMPP)
        $host = '127.0.0.1';
        $puerto = 3306;
        $db = 'biometria_medioambiente';
        $usuario = 'root';
        $password = '';
        $charset = 'utf8mb4';

        $opciones = [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES   => false,
        ];

        // 1. Intento de conexión a MySQL
        try {
            $dsnMySQL = "mysql:host={$host};port={$puerto};dbname={$db};charset={$charset}";
            self::$conexion = new PDO($dsnMySQL, $usuario, $password, $opciones);
            return self::$conexion;
        } catch (PDOException $eMySQL) {
            // Si MySQL no está disponible, usamos SQLite como almacenamiento local robusto
            $archivoSQLite = __DIR__ . '/../bbdd/mediciones.sqlite';
            $directorioBBDD = dirname($archivoSQLite);
            if (!is_dir($directorioBBDD)) {
                mkdir($directorioBBDD, 0777, true);
            }

            $dsnSQLite = "sqlite:" . $archivoSQLite;
            self::$conexion = new PDO($dsnSQLite, null, null, $opciones);

            // Crear la tabla si no existe en la base de datos SQLite
            self::$conexion->exec("
                CREATE TABLE IF NOT EXISTS mediciones (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sensor INTEGER NOT NULL,
                    valor INTEGER NOT NULL,
                    contador INTEGER NOT NULL,
                    tiempo TEXT NOT NULL
                );
                CREATE INDEX IF NOT EXISTS idx_sensor ON mediciones(sensor);
                CREATE INDEX IF NOT EXISTS idx_tiempo ON mediciones(tiempo);
            ");

            return self::$conexion;
        }
    }

    /**
     * Permite reiniciar la conexión (útil en tests o cambios de contexto).
     */
    public static function cerrarConexion(): void {
        self::$conexion = null;
    }
}
