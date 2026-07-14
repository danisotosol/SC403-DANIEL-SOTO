
USE eventappdb;

INSERT INTO eventos (nombre, descripcion, fecha, lugar, categoria, organizador, cupo_maximo, cupos_vendidos, precio)
VALUES
('Concierto de Bandas Locales',
 'Noche de bandas emergentes con musica en vivo y comida de food trucks.',
 '2026-08-15', 'Anfiteatro UNED', 'Musica', 'Asociacion Cultural CR', 500, 120, 8000),

('Taller de Spring Boot',
 'Introduccion practica a Spring Boot para desarrolladores junior.',
 '2026-08-22', 'Aula 301 - Universidad Fidelitas', 'Taller', 'Esteban Ortega', 30, 28, 0),

('Conferencia de Inteligencia Artificial',
 'Panel con expertos de la region sobre el estado actual de la IA aplicada.',
 '2026-09-05', 'Centro de Convenciones', 'Conferencia', 'Camara TIC', 800, 450, 15000),

('Maraton San Jose 10k',
 'Carrera urbana con salida desde el parque central.',
 '2026-09-12', 'Parque Central San Jose', 'Deporte', 'Federacion Atletismo CR', 1500, 980, 12000),

('Festival Gastronomico',
 'Stands de comida tradicional y demostraciones de cocina.',
 '2026-09-20', 'Plaza de la Democracia', 'Gastronomia', 'Municipalidad San Jose', 2000, 1100, 0),

('Hackaton 24 horas',
 'Competencia de programacion en equipos de hasta 4 personas.',
 '2026-10-03', 'Sala de Innovacion - Fidelitas', 'Tecnologia', 'CodeCR Community', 80, 75, 5000),

('Obra de Teatro Independiente',
 'Puesta en escena de la obra "La casa vacia" de la compania nacional.',
 '2026-10-15', 'Teatro Vargas Calvo', 'Cultura', 'Compania Nacional Teatro', 200, 60, 7000),

('Charla Magistral - Patrones de Diseno',
 'Repaso de los patrones GoF aplicados al desarrollo moderno.',
 '2026-10-22', 'Auditorio Principal Fidelitas', 'Charla', 'Esteban Ortega', 150, 12, 0),

('Feria del Empleo TI',
 'Empresas reclutadoras y entrevistas en sitio.',
 '2026-11-05', 'Centro de Convenciones', 'Carrera', 'Camara TIC', 1000, 320, 0),

('Cierre Anual de Programa',
 'Acto de graduacion y entrega de reconocimientos del cuatrimestre.',
 '2026-11-30', 'Salon Magno Fidelitas', 'Universitario', 'Universidad Fidelitas', 400, 0, 0);

SELECT 'Seed cargado correctamente. Total:' AS msg, COUNT(*) AS total FROM eventos;
