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
package org.thingsboard.server.dao.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.TenantInfo;
import org.thingsboard.server.common.data.TenantProfile;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.tenant.profile.DefaultTenantProfileConfiguration;
import org.thingsboard.server.common.data.tenant.profile.TenantProfileData;
import org.thingsboard.server.dao.exception.DataValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseTenantServiceTest extends AbstractServiceTest {
    
    private IdComparator<Tenant> idComparator = new IdComparator<>();

    @Test
    public void testSaveTenant() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        Assert.assertNotNull(savedTenant.getId());
        Assert.assertTrue(savedTenant.getCreatedTime() > 0);
        Assert.assertEquals(tenant.getTitle(), savedTenant.getTitle());
        
        savedTenant.setTitle("My new tenant");
        tenantService.saveTenant(savedTenant);
        Tenant foundTenant = tenantService.findTenantById(savedTenant.getId());
        Assert.assertEquals(foundTenant.getTitle(), savedTenant.getTitle());
        
        tenantService.deleteTenant(savedTenant.getId());
    }
    
    @Test
    public void testFindTenantById() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Tenant foundTenant = tenantService.findTenantById(savedTenant.getId());
        Assert.assertNotNull(foundTenant);
        Assert.assertEquals(savedTenant, foundTenant);
        tenantService.deleteTenant(savedTenant.getId());
    }

    @Test
    public void testFindTenantInfoById() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        TenantInfo foundTenant = tenantService.findTenantInfoById(savedTenant.getId());
        Assert.assertNotNull(foundTenant);
        Assert.assertEquals(new TenantInfo(savedTenant, "Default"), foundTenant);
        tenantService.deleteTenant(savedTenant.getId());
    }
    
    @Test(expected = DataValidationException.class)
    public void testSaveTenantWithEmptyTitle() {
        Tenant tenant = new Tenant();
        tenantService.saveTenant(tenant);
    }
    
    @Test(expected = DataValidationException.class)
    public void testSaveTenantWithInvalidEmail() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        tenant.setEmail("invalid@mail");
        tenantService.saveTenant(tenant);
    }
    
    @Test
    public void testDeleteTenant() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        tenantService.deleteTenant(savedTenant.getId());
        Tenant foundTenant = tenantService.findTenantById(savedTenant.getId());
        Assert.assertNull(foundTenant);
    }
    
    @Test
    public void testFindTenants() {
        
        List<Tenant> tenants = new ArrayList<>();
        PageLink pageLink = new PageLink(17);
        PageData<Tenant> pageData = tenantService.findTenants(pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());
        tenants.addAll(pageData.getData());
        
        for (int i=0;i<156;i++) {
            Tenant tenant = new Tenant();
            tenant.setTitle("Tenant"+i);
            tenants.add(tenantService.saveTenant(tenant));
        }
        
        List<Tenant> loadedTenants = new ArrayList<>();
        pageLink = new PageLink(17);
        do {
            pageData = tenantService.findTenants(pageLink);
            loadedTenants.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(tenants, idComparator);
        Collections.sort(loadedTenants, idComparator);
        
        Assert.assertEquals(tenants, loadedTenants);
        
        for (Tenant tenant : loadedTenants) {
            tenantService.deleteTenant(tenant.getId());
        }
        
        pageLink = new PageLink(17);
        pageData = tenantService.findTenants(pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());
        
    }
    
    @Test
    public void testFindTenantsByTitle() {
        String title1 = "Tenant title 1";
        List<Tenant> tenantsTitle1 = new ArrayList<>();
        for (int i=0;i<134;i++) {
            Tenant tenant = new Tenant();
            String suffix = RandomStringUtils.randomAlphanumeric((int)(Math.random()*15));
            String title = title1+suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            tenant.setTitle(title);
            tenantsTitle1.add(tenantService.saveTenant(tenant));
        }
        String title2 = "Tenant title 2";
        List<Tenant> tenantsTitle2 = new ArrayList<>();
        for (int i=0;i<127;i++) {
            Tenant tenant = new Tenant();
            String suffix = RandomStringUtils.randomAlphanumeric((int)(Math.random()*15));
            String title = title2+suffix;
            title = i % 2 == 0 ? title.toLowerCase() : title.toUpperCase();
            tenant.setTitle(title);
            tenantsTitle2.add(tenantService.saveTenant(tenant));
        }
        
        List<Tenant> loadedTenantsTitle1 = new ArrayList<>();
        PageLink pageLink = new PageLink(15, 0, title1);
        PageData<Tenant> pageData = null;
        do {
            pageData = tenantService.findTenants(pageLink);
            loadedTenantsTitle1.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());
        
        Collections.sort(tenantsTitle1, idComparator);
        Collections.sort(loadedTenantsTitle1, idComparator);
        
        Assert.assertEquals(tenantsTitle1, loadedTenantsTitle1);
        
        List<Tenant> loadedTenantsTitle2 = new ArrayList<>();
        pageLink = new PageLink(4, 0, title2);
        do {
            pageData = tenantService.findTenants(pageLink);
            loadedTenantsTitle2.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(tenantsTitle2, idComparator);
        Collections.sort(loadedTenantsTitle2, idComparator);
        
        Assert.assertEquals(tenantsTitle2, loadedTenantsTitle2);

        for (Tenant tenant : loadedTenantsTitle1) {
            tenantService.deleteTenant(tenant.getId());
        }
        
        pageLink = new PageLink(4, 0, title1);
        pageData = tenantService.findTenants(pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
        
        for (Tenant tenant : loadedTenantsTitle2) {
            tenantService.deleteTenant(tenant.getId());
        }
        
        pageLink = new PageLink(4, 0, title2);
        pageData = tenantService.findTenants(pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindTenantInfos() {

        List<TenantInfo> tenants = new ArrayList<>();
        PageLink pageLink = new PageLink(17);
        PageData<TenantInfo> pageData = tenantService.findTenantInfos(pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());
        tenants.addAll(pageData.getData());

        for (int i=0;i<156;i++) {
            Tenant tenant = new Tenant();
            tenant.setTitle("Tenant"+i);
            tenants.add(new TenantInfo(tenantService.saveTenant(tenant), "Default"));
        }

        List<TenantInfo> loadedTenants = new ArrayList<>();
        pageLink = new PageLink(17);
        do {
            pageData = tenantService.findTenantInfos(pageLink);
            loadedTenants.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(tenants, idComparator);
        Collections.sort(loadedTenants, idComparator);

        Assert.assertEquals(tenants, loadedTenants);

        for (TenantInfo tenant : loadedTenants) {
            tenantService.deleteTenant(tenant.getId());
        }

        pageLink = new PageLink(17);
        pageData = tenantService.findTenantInfos(pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

    }

    @Test(expected = DataValidationException.class)
    public void testSaveTenantWithIsolatedProfileInMonolithSetup() {
        TenantProfile tenantProfile = new TenantProfile();
        tenantProfile.setName("Isolated Tenant Profile");
        TenantProfileData profileData = new TenantProfileData();
        profileData.setConfiguration(new DefaultTenantProfileConfiguration());
        tenantProfile.setProfileData(profileData);
        tenantProfile.setDefault(false);
        tenantProfile.setIsolatedTbCore(true);
        tenantProfile.setIsolatedTbRuleEngine(true);
        TenantProfile isolatedTenantProfile = tenantProfileService.saveTenantProfile(TenantId.SYS_TENANT_ID, tenantProfile);

        Tenant tenant = new Tenant();
        tenant.setTitle("Tenant");
        tenant.setTenantProfileId(isolatedTenantProfile.getId());
        tenantService.saveTenant(tenant);
    }
}
