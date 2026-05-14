package com.ccat.api.mapper;

import com.ccat.api.dto.request.AppConfigUpdateRequest;
import com.ccat.api.dto.response.AppConfigResponse;
import com.ccat.api.model.entity.AppConfig;
import com.ccat.api.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AppConfigMapper {

    public AppConfigResponse toResponse(AppConfig config) {
        return new AppConfigResponse(
                config.getLgId(),
                config.getStrKey(),
                config.getStrValue(),
                config.getEmValueType(),
                config.getStrDescription(),
                config.getStrDefaultValue(),
                config.getEmGroup(),
                config.getBIsSensitive(),
                config.getBRequiresRestart(),
                config.getDtUpdated()
        );
    }

    public AppConfig toEntity(AppConfigUpdateRequest request, User updatedBy) {
        AppConfig config = new AppConfig();
        config.setStrKey(request.strKey());
        config.setStrValue(request.strValue());
        config.setEmValueType(request.emValueType());
        config.setEmGroup(request.emGroup());
        config.setStrDescription(request.strDescription());
        config.setStrDefaultValue(request.strDefaultValue());
        config.setBIsSensitive(request.bIsSensitive() != null ? request.bIsSensitive() : false);
        config.setBRequiresRestart(request.bRequiresRestart() != null ? request.bRequiresRestart() : false);
        config.setUpdatedBy(updatedBy);
        return config;
    }
}
