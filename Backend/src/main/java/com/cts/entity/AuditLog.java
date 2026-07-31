package com.cts.entity;

import com.cts.enumeration.EventLevel;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") 
    private User user;

    @Column(nullable = false)
    private String action; 

    @Column(nullable = false)
    private String resourceType; 

    private Long resourceId; 

    @Column(columnDefinition = "TEXT")
    private String details; 

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private EventLevel eventLevel = EventLevel.INFO;
}