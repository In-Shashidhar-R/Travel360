export interface PassengerProfile {
  profileId?: number; 
  name: string;
  age: number;
  gender: 'Male' | 'Female';
  idProofType: 'PAN' | 'AADHAAR' | 'PASSPORT' | 'DRIVING_LICENSE';
  idProofNumber: string;
}

export interface PassengerSnapshot {
  name: string;
  age: number;
  gender: string;
  idProofType: string;
  idProofNumber: string;
}