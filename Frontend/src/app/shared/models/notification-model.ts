
export interface NotificationResponseDTO {
  notificationId: number;
  userId: number;
  message: string;
  category: string; // e.g., 'BOOKING', 'PAYMENT', 'AGENT_NEGOTIATION'
  status: string;   // e.g., 'READ', 'UNREAD'
  createdDate: string; // LocalDateTime serializes cleanly into a standard ISO-8601 string
}