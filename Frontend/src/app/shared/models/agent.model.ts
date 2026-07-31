export interface AgentRequestDTO {
  name: string;
  email: string;
  password?: string;
  phone: string;
  address?: string;
  country: string;
  state: string;
  city: string;
  agentBio?: string;
  gender?: string;
  dateOfBirth?: string; 
  agentExperienceYears: number;
}