package com.betterlife.notify.domain;

import com.betterlife.notify.enums.DeviceType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Table(name = "fcm_tokens")
@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String token;

    @Column(columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(nullable = false)
    private String browserType;

    @Column(nullable = false)
    private LocalDate updatedAt;

    @Column(nullable = false)
    private Boolean enabled;

    @Builder
    public FcmToken(Long userId, String token, DeviceType deviceType, String browserType, LocalDate updatedAt, Boolean enabled) {
        this.userId = userId;
        this.token = token;
        this.deviceType = deviceType;
        this.browserType = browserType;
        this.updatedAt = updatedAt;
        this.enabled = enabled;
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDate.now();
    }
}
