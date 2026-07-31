export interface AuthRequestDTO {
  email: string;
  password?: string;
}

export interface AuthResponseDTO {
  userId: number;
  token: string;
  email: string;
  role: 'ADMIN' | 'CUSTOMER' | 'TRAVEL_AGENT' | 'FINANCE_OFFICER' | 'COMPLIANCE_OFFICER' | 'PARTNER';
  name: string;
}