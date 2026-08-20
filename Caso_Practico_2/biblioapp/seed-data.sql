-- ============================================================
-- Seed data - BiblioApp (Caso Practico 2 - SC-403)
-- ============================================================
-- Ejecutar DESPUES de:
--   1) CREATE DATABASE biblioappdb CHARACTER SET utf8mb4;
--   2) Arrancar la app una vez, para que Hibernate (ddl-auto=update) cree
--      las tablas libros, usuarios y prestamos con sus FK.
--
-- Las fechas de los prestamos se calculan con CURDATE() y DATE_SUB/DATE_ADD
-- a proposito: asi el escenario de "atrasados" sigue siendo valido sin
-- importar el dia en que se corra el script.
-- ============================================================

USE biblioappdb;

-- Limpieza (prestamos primero: tiene FK a libros y usuarios)
-- DELETE FROM prestamos;
-- DELETE FROM libros;
-- DELETE FROM usuarios;

-- ============================================================
-- 1) USUARIOS
-- ============================================================
-- Los passwords van hasheados con BCrypt (cost 10). NUNCA texto plano.
-- Credenciales reales, solo para el laboratorio:
--   biblio  -> biblio123   (BIBLIOTECARIO)
--   lector  -> lector123   (LECTOR)
--   ana     -> ana123      (LECTOR)
INSERT INTO usuarios (username, password, rol, nombre_completo, email) VALUES
('biblio', '$2a$10$ZL5YM2.5LWGY0Faw8Z5.BOKJNAtR8zM4tnGfGDZP1etnSXhce5Qoq', 'BIBLIOTECARIO', 'Daniel Soto', 'biblio@ufide.ac.cr'),
('lector', '$2a$10$xOWScPOmk9RemX/HYcTdxe2637OEmyo0lreWRtv2SUOmSnGwg6iCi', 'LECTOR', 'Carlos Jimenez', 'lector@ufide.ac.cr'),
('ana',    '$2a$10$NnIRfsc00Leq2rgmZa4lju2rkcglCCjAnq8NsDt1kdbtqf8DZE9xG', 'LECTOR', 'Ana Vargas', 'ana@ufide.ac.cr');

SELECT id, username, rol, nombre_completo FROM usuarios;

-- ============================================================
-- 2) LIBROS
-- ============================================================
INSERT INTO libros (titulo, autor, isbn, categoria, anio_publicacion, ejemplares_totales, ejemplares_disponibles) VALUES
('Clean Code', 'Robert C. Martin', '978-0132350884', 'Programacion', 2008, 3, 3),
('El Programador Pragmatico', 'Andrew Hunt', '978-0201616224', 'Programacion', 1999, 2, 2),
('Refactoring', 'Martin Fowler', '978-0134757599', 'Programacion', 2018, 2, 2),
('Patrones de Diseno', 'Erich Gamma', '978-0201633610', 'Arquitectura', 1994, 2, 2),
('Domain-Driven Design', 'Eric Evans', '978-0321125215', 'Arquitectura', 2003, 1, 1),
('The Rust Programming Language', 'Steve Klabnik', '978-1718503106', 'Sistemas', 2019, 2, 2),
('Cien Anos de Soledad', 'Gabriel Garcia Marquez', '978-0307474728', 'Literatura', 1967, 4, 4),
('Rayuela', 'Julio Cortazar', '978-8437604572', 'Literatura', 1963, 2, 2),
('Sapiens', 'Yuval Noah Harari', '978-0062316097', 'Historia', 2011, 3, 3),
('Introduccion a los Algoritmos', 'Thomas H. Cormen', '978-0262046305', 'Algoritmos', 2022, 1, 1);

SELECT id, titulo, categoria, ejemplares_disponibles FROM libros;

-- ============================================================
-- 3) PRESTAMOS
-- ============================================================
-- Escenario armado para poder demostrar la consulta JPQL de atrasados:
--
--   a) DOS prestamos ATRASADOS  -> sin fecha real y con vencimiento pasado.
--   b) UNO en plazo             -> sin fecha real y vencimiento futuro.
--   c) UNO que vence HOY        -> caso borde: NO cuenta como atrasado,
--                                  porque la condicion es "< hoy", no "<=".
--   d) UNO devuelto tarde       -> tiene fecha real: aunque se entrego
--                                  despues del plazo, ya esta cerrado y la
--                                  consulta de atrasados NO lo trae.
--   e) UNO devuelto en plazo    -> control normal.

-- a) ATRASADOS
INSERT INTO prestamos (libro_id, usuario_id, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real) VALUES
(1, 2, DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 26 DAY), NULL),
(4, 3, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), NULL);

-- b) En plazo (vence dentro de 7 dias)
INSERT INTO prestamos (libro_id, usuario_id, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real) VALUES
(6, 2, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY), NULL);

-- c) Caso borde: vence HOY, todavia en plazo
INSERT INTO prestamos (libro_id, usuario_id, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real) VALUES
(9, 3, DATE_SUB(CURDATE(), INTERVAL 14 DAY), CURDATE(), NULL);

-- d) Devuelto TARDE (no es atrasado: ya volvio)
INSERT INTO prestamos (libro_id, usuario_id, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real) VALUES
(7, 2, DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_SUB(CURDATE(), INTERVAL 46 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY));

-- e) Devuelto en plazo
INSERT INTO prestamos (libro_id, usuario_id, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real) VALUES
(8, 3, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(CURDATE(), INTERVAL 20 DAY));

-- Los 4 prestamos ACTIVOS (a, b, c) descuentan ejemplares del inventario.
-- El seed entra por SQL, no por PrestamoService, asi que hay que ajustar
-- ejemplares_disponibles a mano para que el inventario quede coherente.
UPDATE libros SET ejemplares_disponibles = ejemplares_disponibles - 1 WHERE id IN (1, 4, 6, 9);

-- ============================================================
-- 4) VERIFICACION
-- ============================================================
-- Equivalente SQL de la consulta JPQL findAtrasados(:hoy):
--   SELECT p FROM Prestamo p JOIN FETCH p.libro JOIN FETCH p.usuario
--   WHERE p.fechaDevolucionReal IS NULL AND p.fechaDevolucionEsperada < :hoy
SELECT p.id,
       l.titulo,
       u.username,
       p.fecha_devolucion_esperada,
       DATEDIFF(CURDATE(), p.fecha_devolucion_esperada) AS dias_atraso
FROM prestamos p
JOIN libros l   ON l.id = p.libro_id
JOIN usuarios u ON u.id = p.usuario_id
WHERE p.fecha_devolucion_real IS NULL
  AND p.fecha_devolucion_esperada < CURDATE()
ORDER BY p.fecha_devolucion_esperada ASC;
-- Esperado: 2 filas (los prestamos del grupo "a").

-- Inventario resultante
SELECT id, titulo, ejemplares_totales, ejemplares_disponibles FROM libros;
