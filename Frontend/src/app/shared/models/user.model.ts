export interface UserResponseDTO {
  userId: number;
  name: string;
  email: string;
  role: string;
  phone: string;
  address: string;
  city: string;
  state: string;
  country: string;
  dateOfBirth?: string;
  gender?: string;
}

export interface UserDirectoryDTO {
  userId: number;
  name: string;
  email: string;
  phone: string;
  role: string;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  gender?: string;
  dateOfBirth?: string; // Added field property mapping
  age?: number | null;   // Added runtime dynamic display variable
  agentBio?: string;
  agentExperienceYears?: number;
  partnerId?: number;
  partnerType?: string;
  gstNumber?: string;
  commissionRate?: number;
}