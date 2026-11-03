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

export type PaymentMethod = 'EFECTIVO' | 'TARJETA';

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  EFECTIVO: 'Efectivo',
  TARJETA: 'Tarjeta'
};

export interface Payment {
  id: number;
  publicId: string | null;
  subscriptionId: number; // id numérico de la suscripción (Subscription.id), no el UUID
  amount: number;
  paymentDate: string; // "YYYY-MM-DD"
  method: PaymentMethod;
}

/** Body de POST /api/payments. */
export interface PaymentRequest {
  subscriptionId: string; // UUID de la suscripción (Subscription.publicId)
  amount: number;
  paymentDate: string; // "YYYY-MM-DD"
  method: PaymentMethod;
}