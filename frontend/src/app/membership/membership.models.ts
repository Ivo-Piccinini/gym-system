export interface MembershipPlan {
  id: number;
  publicId: string; // UUID: es el que va en la URL al contratar
  name: string;
  price: number;
  durationDays: number;
}

export type SubscriptionStatus = 'ACTIVE' | 'PENDING' | 'EXPIRED' | 'CANCELED';

export const SUBSCRIPTION_STATUS_LABELS: Record<SubscriptionStatus, string> = {
  ACTIVE: 'Activa',
  PENDING: 'Pendiente',
  EXPIRED: 'Vencida',
  CANCELED: 'Cancelada'
};


export interface Subscription {
  id: number;
  publicId: string;
  userId: number;
  planId: number; // id numérico del plan (MembershipPlan.id), no el UUID
  startDate: string; // "YYYY-MM-DD"
  endDate: string; // "YYYY-MM-DD", la calcula el back (inicio + durationDays)
  status: SubscriptionStatus;
}