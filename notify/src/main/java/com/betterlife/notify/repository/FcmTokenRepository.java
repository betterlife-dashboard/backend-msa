package com.betterlife.notify.repository;

import com.betterlife.notify.domain.FcmToken;
import com.betterlife.notify.enums.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByUserId(Long userId);
    Optional<FcmToken> findByUserIdAndDeviceTypeAndBrowserType(Long userId, DeviceType deviceType, String browserType);

    List<FcmToken> findByUserIdAndEnabled(Long userId, Boolean enabled);
}
