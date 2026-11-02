// Siempre hora local: toISOString() en Argentina (UTC-3) corre la fecha un día a la noche.

export function toIsoDate(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function parseIsoDate(iso: string): Date {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(y, m - 1, d);
}

export function addDays(date: Date, days: number): Date {
  const copy = new Date(date);
  copy.setDate(copy.getDate() + days);
  return copy;
}

// Lunes de la semana de la fecha dada (el domingo pertenece a la semana que termina en él).
export function startOfWeek(date: Date): Date {
  const diff = (date.getDay() + 6) % 7;
  return addDays(date, -diff);
}
