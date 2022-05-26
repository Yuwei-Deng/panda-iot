/**
 * Copyright © 2016-2022 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.service.entitiy.otaPackageController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.OtaPackage;
import org.thingsboard.server.common.data.OtaPackageInfo;
import org.thingsboard.server.common.data.SaveOtaPackageInfoRequest;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.OtaPackageId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.entitiy.AbstractTbEntityService;
import org.thingsboard.server.service.security.model.SecurityUser;

@Service
@TbCoreComponent
@AllArgsConstructor
@Slf4j
public class DefaultTbOtaPackageService extends AbstractTbEntityService implements TbOtaPackageService {
    @Override
    public OtaPackageInfo save(SaveOtaPackageInfoRequest saveOtaPackageInfoRequest, SecurityUser user) throws ThingsboardException {
        TenantId tenantId = saveOtaPackageInfoRequest.getTenantId();
        ActionType actionType = saveOtaPackageInfoRequest.getId() == null ? ActionType.ADDED : ActionType.UPDATED;
        try {
            OtaPackageInfo savedOtaPackageInfo = otaPackageService.saveOtaPackageInfo(new OtaPackageInfo(saveOtaPackageInfoRequest), saveOtaPackageInfoRequest.isUsesUrl());
            notificationEntityService.notifyEntity(tenantId, savedOtaPackageInfo.getId(), savedOtaPackageInfo, null,
                    actionType, user, null);
            return savedOtaPackageInfo;
        } catch (Exception e) {
            notificationEntityService.notifyEntity(tenantId, emptyId(EntityType.OTA_PACKAGE), saveOtaPackageInfoRequest, null,
                    actionType, user, e);
            throw handleException(e);
        }
    }

    @Override
    public void delete(OtaPackageInfo otaPackageInfo, SecurityUser user) throws ThingsboardException {
        TenantId tenantId = otaPackageInfo.getTenantId();
        OtaPackageId otaPackageId = otaPackageInfo.getId();
        try {
            otaPackageService.deleteOtaPackage(tenantId, otaPackageId);
            notificationEntityService.notifyEntity(tenantId, otaPackageId, otaPackageInfo, null,
                    ActionType.DELETED, user, null, otaPackageInfo.getId().toString());
        } catch (Exception e) {
            notificationEntityService.notifyEntity(tenantId, emptyId(EntityType.OTA_PACKAGE), null, null,
                    ActionType.DELETED, user, e, otaPackageInfo.getId().toString());
            throw handleException(e);
        }


    }

    @Override
    public OtaPackageInfo saveOtaPackageData(EntityId otaPackageId, OtaPackage otaPackage, SecurityUser user, Exception e) throws ThingsboardException {
        TenantId tenantId = otaPackage.getTenantId();
        if (e == null) {
            try {
                OtaPackageInfo savedOtaPackage = otaPackageService.saveOtaPackage(otaPackage);
                notificationEntityService.notifyEntity(tenantId, savedOtaPackage.getId(), savedOtaPackage, null,
                        ActionType.UPDATED, user, null);
                return savedOtaPackage;
            } catch (Exception e1) {
                notificationEntityService.notifyEntity(tenantId, emptyId(EntityType.OTA_PACKAGE), null, null,
                        ActionType.UPDATED, user, e1, otaPackageId.toString());
                throw handleException(e1);
            }
        } else {
            notificationEntityService.notifyEntity(tenantId, emptyId(EntityType.OTA_PACKAGE), null, null,
                    ActionType.UPDATED, user, e, otaPackageId.toString());
            throw handleException(e);
        }
    }


}