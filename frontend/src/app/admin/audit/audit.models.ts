export interface AuditLog {
  id: number;
  action: string;
  performedBy: string;
  timestamp: string;
  details: string;
}
