package com.liochio.auth.service;

import com.liochio.auth.dto.DynamicMenuNodeDto;
import com.liochio.auth.entity.MasterMenuEntity;
import com.liochio.auth.entity.MenuI18nEntity;
import com.liochio.auth.entity.MasterPermissionEntity;
import com.liochio.auth.entity.TenantMenuEntity;
import com.liochio.auth.repository.MasterMenuRepository;
import com.liochio.auth.repository.MenuI18nRepository;
import com.liochio.auth.repository.MasterPermissionRepository;
import com.liochio.auth.repository.TenantMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicMenuService {

    private final MasterMenuRepository masterMenuRepository;
    private final MenuI18nRepository menuI18nRepository;
    private final MasterPermissionRepository masterPermissionRepository;
    private final TenantMenuRepository tenantMenuRepository;

    @Transactional(readOnly = true)
    public List<DynamicMenuNodeDto> getMenuTreeForUser(String portalType, String tenantId, String lang, List<String> userRoles) {
        String effectiveLang = (lang != null && !lang.isBlank()) ? lang.toLowerCase() : "vi";
        
        // 1. Lấy toàn bộ menus thuộc portalType hoặc tất cả nếu SUPERADMIN
        List<MasterMenuEntity> allMenus = masterMenuRepository.findByIsActiveOrderBySortOrderAsc(true);
        
        // 2. Lấy i18n map
        List<MenuI18nEntity> i18nList = menuI18nRepository.findByLang(effectiveLang);
        Map<String, MenuI18nEntity> i18nMap = i18nList.stream()
                .collect(Collectors.toMap(MenuI18nEntity::getMenuCode, item -> item, (k1, k2) -> k1));

        // Fallback Vietnamese map nếu ngôn ngữ hiện tại thiếu
        Map<String, MenuI18nEntity> fallbackViMap = effectiveLang.equals("vi") ? Collections.emptyMap() :
                menuI18nRepository.findByLang("vi").stream()
                        .collect(Collectors.toMap(MenuI18nEntity::getMenuCode, item -> item, (k1, k2) -> k1));

        // 3. Lấy master permissions map
        List<MasterPermissionEntity> allPermissions = masterPermissionRepository.findAll();
        Map<String, List<String>> menuPermissionsMap = new HashMap<>();
        for (MasterPermissionEntity perm : allPermissions) {
            menuPermissionsMap.computeIfAbsent(perm.getMenuCode(), k -> new ArrayList<>()).add(perm.getPermissionCode());
        }

        // 4. Nếu là Corp / Tenant context, kiểm tra tenant_menus
        Set<String> enabledTenantMenuCodes = null;
        if (tenantId != null && !tenantId.equalsIgnoreCase("SYSTEM") && !"SUPERADMIN".equalsIgnoreCase(portalType)) {
            List<TenantMenuEntity> tenantMenus = tenantMenuRepository.findByTenantIdAndIsEnabledTrue(tenantId);
            enabledTenantMenuCodes = tenantMenus.stream().map(TenantMenuEntity::getMenuCode).collect(Collectors.toSet());
        }

        // 5. Lọc menus phù hợp theo portalType
        String rootPortalCode = switch (portalType.toUpperCase()) {
            case "SUPERADMIN" -> "PORTAL_SUPERADMIN";
            case "CORP", "CORP_ADMIN" -> "PORTAL_CORP_ADMIN";
            default -> "PORTAL_CONSUMER_APP";
        };

        // Lọc các menu thuộc rootPortalCode hoặc con cháu của nó
        Set<String> allowedCodes = new HashSet<>();
        collectAllowedMenuCodes(rootPortalCode, allMenus, allowedCodes);
        allowedCodes.add(rootPortalCode);

        // Map sang DTO
        Map<String, DynamicMenuNodeDto> dtoMap = new HashMap<>();
        for (MasterMenuEntity entity : allMenus) {
            if (!allowedCodes.contains(entity.getCode())) {
                continue;
            }
            if (enabledTenantMenuCodes != null && !enabledTenantMenuCodes.isEmpty() && !entity.getCode().equals(rootPortalCode)) {
                // Kiểm tra xem menu hoặc cha của nó có trong enabled list không
                if (!enabledTenantMenuCodes.contains(entity.getCode()) && !entity.getCode().startsWith("PORTAL_")) {
                    continue;
                }
            }

            MenuI18nEntity i18n = i18nMap.get(entity.getCode());
            if (i18n == null) {
                i18n = fallbackViMap.get(entity.getCode());
            }

            String title = (i18n != null) ? i18n.getTitle() : entity.getCode();
            String description = (i18n != null) ? i18n.getDescription() : "";

            DynamicMenuNodeDto dto = DynamicMenuNodeDto.builder()
                    .code(entity.getCode())
                    .parentCode(entity.getParentCode())
                    .portalType(entity.getPortalType())
                    .routePath(entity.getRoutePath())
                    .icon(entity.getIcon())
                    .sortOrder(entity.getSortOrder())
                    .isLeaf(entity.getIsLeaf())
                    .title(title)
                    .description(description)
                    .permissions(menuPermissionsMap.getOrDefault(entity.getCode(), Collections.emptyList()))
                    .children(new ArrayList<>())
                    .build();

            dtoMap.put(entity.getCode(), dto);
        }

        // 6. Dựng cây phân cấp (Hierarchy Tree)
        List<DynamicMenuNodeDto> rootNodes = new ArrayList<>();
        for (DynamicMenuNodeDto node : dtoMap.values()) {
            if (node.getParentCode() == null || node.getCode().equals(rootPortalCode)) {
                rootNodes.add(node);
            } else {
                DynamicMenuNodeDto parent = dtoMap.get(node.getParentCode());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else if (node.getParentCode().equals(rootPortalCode)) {
                    rootNodes.add(node);
                }
            }
        }

        // Sắp xếp các node theo sortOrder
        sortMenuTree(rootNodes);
        return rootNodes;
    }

    private void collectAllowedMenuCodes(String parentCode, List<MasterMenuEntity> allMenus, Set<String> allowedCodes) {
        for (MasterMenuEntity menu : allMenus) {
            if (parentCode.equals(menu.getParentCode())) {
                allowedCodes.add(menu.getCode());
                collectAllowedMenuCodes(menu.getCode(), allMenus, allowedCodes);
            }
        }
    }

    private void sortMenuTree(List<DynamicMenuNodeDto> nodes) {
        nodes.sort(Comparator.comparingInt(n -> (n.getSortOrder() != null ? n.getSortOrder() : 0)));
        for (DynamicMenuNodeDto node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortMenuTree(node.getChildren());
            }
        }
    }
}
