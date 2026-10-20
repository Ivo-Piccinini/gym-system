export interface UserProfile {
  publicId: string;
  firstName: string;
  lastName: string;
  email: string;
  dni: string;
  phone: string | null;
  birthDate: string | null;
  role: string;
}

export interface UpdateProfileRequest {
  phone?: string;
  birthDay?: string;
  birthDate?: string;
}
