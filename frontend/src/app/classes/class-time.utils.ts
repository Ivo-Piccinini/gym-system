// Utilidades para los selectores de horario (formato "HH:mm", 24 horas).

export function toMinutes(time: string): number {
  const [h, m] = time.slice(0, 5).split(':').map(Number);
  return h * 60 + m;
}

export function formatMinutes(total: number): string {
  const h = String(Math.floor(total / 60)).padStart(2, '0');
  const m = String(total % 60).padStart(2, '0');
  return `${h}:${m}`;
}

/** Suma minutos a un horario; devuelve null si se pasa de las 23:59. */
export function addMinutes(time: string, minutes: number): string | null {
  const total = toMinutes(time) + minutes;
  return total >= 24 * 60 ? null : formatMinutes(total);
}

/** "1 h 30 min", "45 min", "2 h"; null si el rango no es válido. */
export function formatDuration(start: string, end: string): string | null {
  const diff = toMinutes(end) - toMinutes(start);
  if (diff <= 0) {
    return null;
  }
  const hours = Math.floor(diff / 60);
  const minutes = diff % 60;
  return [hours ? `${hours} h` : '', minutes ? `${minutes} min` : ''].filter(Boolean).join(' ');
}
