export type FeedbackKind = 'success' | 'warning' | 'error';

export interface Feedback {
  kind: FeedbackKind;
  title: string;
  message: string;
}

/** Forma mínima de un HttpErrorResponse; permite testear sin depender de Angular. */
export interface HttpErrorLike {
  status?: number;
  error?: { message?: string } | null;
}

const GENERIC_MESSAGE = 'Ocurrió un error inesperado. Por favor, intentá de nuevo.';

function serverMessage(err: HttpErrorLike): string {
  return err.error?.message || GENERIC_MESSAGE;
}

/**
 * Alertas al inscribirse:
 *  - 403: membresía no activa
 *  - 409: clase sin cupo
 *  - resto: mensaje del back si lo hay (hoy el back responde 400 con texto cuando no hay cupo)
 */
export function feedbackForEnrollmentError(err: HttpErrorLike): Feedback {
  switch (err.status) {
    case 403:
      return {
        kind: 'warning',
        title: 'Membresía inactiva',
        message:
          'Tu membresía no está activa, por eso no podés inscribirte. Renová tu plan para reservar tu lugar en las clases.'
      };
    case 409:
      return {
        kind: 'warning',
        title: 'Clase sin cupo',
        message: 'Esta clase ya alcanzó su cupo máximo. Elegí otro horario o probá más tarde.'
      };
    case 0:
      return {
        kind: 'error',
        title: 'Sin conexión',
        message: 'No se pudo conectar con el servidor. Revisá tu conexión e intentá de nuevo.'
      };
    default:
      return { kind: 'error', title: 'No se pudo inscribir', message: serverMessage(err) };
  }
}

export function feedbackForCancelError(err: HttpErrorLike): Feedback {
  if (err.status === 0) {
    return feedbackForEnrollmentError(err);
  }
  return { kind: 'error', title: 'No se pudo cancelar', message: serverMessage(err) };
}
