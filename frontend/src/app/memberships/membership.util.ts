import { parseIsoDate, toIsoDate } from '../classes/date.utils';
import { MembershipPlan, Subscription } from './membership.models';

/** Un "mes" comercial a efectos de comparar planes. */
export const DAYS_PER_MONTH = 30;

const priceFormatter = new Intl.NumberFormat('es-AR', {
  style: 'currency',
  currency: 'ARS',
  maximumFractionDigits: 0
});

export function formatPrice(value: number): string {
  return priceFormatter.format(value);
}

/** "2026-12-02" -> "2 de diciembre de 2026" */
export function formatDate(iso: string): string {
  return parseIsoDate(iso).toLocaleDateString('es-AR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  });
}


/** 30 -> "1 mes", 180 -> "6 meses", 365 -> "1 año", 45 -> "45 días". */
export function durationLabel(days: number): string {
  if (days % 365 === 0) {
    const years = days / 365;
    return years === 1 ? '1 año' : `${years} años`;
  }
  if (days % DAYS_PER_MONTH === 0) {
    const months = days / DAYS_PER_MONTH;
    return months === 1 ? '1 mes' : `${months} meses`;
  }
  return days === 1 ? '1 día' : `${days} días`;
}

export function pricePerDay(plan: MembershipPlan): number {
  return plan.price / plan.durationDays;
}

export function pricePerMonth(plan: MembershipPlan): number {
  return pricePerDay(plan) * DAYS_PER_MONTH;
}

/**
 * Una suscripción bloquea una nueva contratación solo si está ACTIVE y no venció.
 * El back marca las vencidas como EXPIRED al listar, pero no dependemos de eso.
 * Las fechas ISO "YYYY-MM-DD" se comparan bien como strings.
 */
export function isCurrentlyActive(
  subscription: Subscription,
  today: string = toIsoDate(new Date())
): boolean {
  return subscription.status === 'ACTIVE' && subscription.endDate >= today;
}