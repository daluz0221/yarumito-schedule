-- V32: El servicio admite el número 0 para una franja de descanso.
-- La restricción original solo permitía números del 1 al 6.

ALTER TABLE tiempoyespacio.franjahoraria
DROP CONSTRAINT franjahoraria_numero_check;

ALTER TABLE tiempoyespacio.franjahoraria
ADD CONSTRAINT franjahoraria_numero_check
CHECK (numero >= 0);
