package com.betterlife.notify.repository;

import com.betterlife.notify.domain.NotificationTemplate;
import com.betterlife.notify.enums.ChannelType;
import com.betterlife.notify.enums.EventType;
import com.betterlife.notify.enums.Lang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByEventTypeAndChannelTypeAndLang(EventType eventType, ChannelType channelType, Lang lang);
}
