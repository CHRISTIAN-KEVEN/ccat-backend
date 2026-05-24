package com.ccat.api.model.entity;

import com.ccat.api.model.enums.UserRole;
import com.ccat.api.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    // Exposed in API and tokens — avoids leaking the sequential internal ID
    @Column(name = "str_uuid", unique = true, nullable = false, length = 36)
    private String strUuid;

    @Column(name = "str_email", unique = true, nullable = false, length = 255)
    private String strEmail;

    // NULL for OAuth accounts — never attempt to hash or compare if null
    @Column(name = "str_password_hash")
    private String strPasswordHash;

    @Column(name = "str_first_name", length = 100)
    private String strFirstName;

    @Column(name = "str_last_name", length = 100)
    private String strLastName;

    @Column(name = "str_profile_image_url", length = 500)
    private String strProfileImageUrl;

    @Column(name = "str_oauth_provider", length = 50)
    private String strOauthProvider;

    @Column(name = "str_oauth_id", length = 255)
    private String strOauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_role", nullable = false, length = 20)
    private UserRole emRole = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_status", nullable = false, length = 20)
    private UserStatus emStatus = UserStatus.ACTIVE;

    @Column(name = "b_email_verified", nullable = false)
    private Boolean bEmailVerified = false;

    @Column(name = "str_verification_token", length = 255)
    private String strVerificationToken;

    @Column(name = "dt_verification_token_expires")
    private LocalDateTime dtVerificationTokenExpires;

    @Column(name = "str_reset_token", length = 255)
    private String strResetToken;

    @Column(name = "dt_reset_token_expires")
    private LocalDateTime dtResetTokenExpires;

    @Column(name = "str_refresh_token", length = 255)
    private String strRefreshToken;

    @Column(name = "dt_refresh_token_expires")
    private LocalDateTime dtRefreshTokenExpires;

    @Column(name = "str_timezone", length = 60)
    private String strTimezone;

    @Column(name = "str_locale", nullable = false, length = 10)
    private String strLocale = "en";

    @Column(name = "int_login_count", nullable = false)
    private Integer intLoginCount = 0;

    @Column(name = "dt_last_login")
    private LocalDateTime dtLastLogin;

    @Column(name = "dt_created", nullable = false)
    private LocalDateTime dtCreated;

    @Column(name = "dt_updated", nullable = false)
    private LocalDateTime dtUpdated;

    @PrePersist
    protected void onCreate() {
        dtCreated = LocalDateTime.now();
        dtUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dtUpdated = LocalDateTime.now();
    }
}
