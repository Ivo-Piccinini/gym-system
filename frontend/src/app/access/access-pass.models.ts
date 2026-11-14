export interface AccessPass {
  publicId: string;
  userPublicId: string;
  clientName: string;
  clientDni: string;
  clientEmail: string;
  planName: string;
  accessCode: string;
  qrData: string;
  qrImageBase64: string;
  generatedBy: string;
  generatedAt: string;
  validUntil: string;
  active: boolean;
}
