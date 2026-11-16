/** "YYYY-MM-DD" de hoy en hora local (sin desfases por UTC). */
export function todayIso(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}

/**
 * "YYYY-MM-DD" -> "YYYY-MM-DDT12:00:00". El back usa java.util.Date: a mediodía la fecha
 * no se corre de día al guardarse en zonas horarias detrás de UTC (Argentina).
 */
export function toApiDate(iso: string): string {
  return `${iso}T12:00:00`;
}

/** Normaliza lo que manda el back a "YYYY-MM-DD" para los <input type="date">. */
export function toDateInput(value: string | null | undefined): string {
  return value ? value.slice(0, 10) : '';
}

/** "YYYY-MM-DD" -> "DD/MM/YYYY". Devuelve "—" si no hay fecha. */
export function formatDate(value: string | null | undefined): string {
  const iso = toDateInput(value);
  if (!iso) {
    return '—';
  }
  const [year, month, day] = iso.split('-');
  return `${day}/${month}/${year}`;
}

/** Arma un mensaje legible a partir del ErrorResponse del back (message + fieldErrors). */
export function extractApiError(err: unknown, fallback: string): string {
  const httpErr = err as { status?: number; error?: { message?: string; fieldErrors?: Record<string, string> } };

  if (httpErr?.status === 0) {
    return 'No se pudo conectar con el servidor. Verificá tu conexión e intentá nuevamente.';
  }

  const fieldErrors = httpErr?.error?.fieldErrors;
  if (fieldErrors && typeof fieldErrors === 'object') {
    const messages = Array.from(new Set(Object.values(fieldErrors)));
    if (messages.length > 0) {
      return messages.join(' · ');
    }
  }

  return httpErr?.error?.message || fallback;
}