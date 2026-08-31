package auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporary_role_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporaryRoleAssigment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Long meetingId;

    private Long organizationalUnitId;

    @Column(nullable = false, length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_admin_id")
    private User assignedByAdmin;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    private LocalDateTime validUntil;

    @Builder.Default
    private boolean revoked = false;

    @PrePersist
    void onCreate() {this.assignedAt = LocalDateTime.now();}

    public boolean isActive(){
        if (revoked) return  false;
        return validUntil == null || validUntil.isAfter(LocalDateTime.now());
    }
}
