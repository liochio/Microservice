package com.liochio.entityservice.service;

import com.liochio.entityservice.dto.DynamicMenuNodeDto;
import com.liochio.entityservice.entity.MasterMenuEntity;
import com.liochio.entityservice.entity.MasterPermissionEntity;
import com.liochio.entityservice.entity.MenuI18nEntity;
import com.liochio.entityservice.entity.TenantMenuEntity;
import com.liochio.entityservice.repository.MasterMenuRepository;
import com.liochio.entityservice.repository.MasterPermissionRepository;
import com.liochio.entityservice.repository.MenuI18nRepository;
import com.liochio.entityservice.repository.TenantMenuRepository;
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

        List<MasterMenuEntity> allMenus = masterMenuRepository.findByIsActiveOrderBySortOrderAsc(true);

        List<MenuI18nEntity> i18nList = menuI18nRepository.findByLang(effectiveLang);
        Map<String, MenuI18nEntity> i18nMap = i18nList.stream()
                .collect(Collectors.toMap(MenuI18nEntity::getMenuCode, item -> item, (k1, k2) -> k1));

        Map<String, MenuI18nEntity> fallbackViMap = effectiveLang.equals("vi") ? Collections.emptyMap() :
                menuI18nRepository.findByLang("vi").stream()
                        .collect(Collectors.toMap(MenuI18nEntity::getMenuCode, item -> item, (k1, k2) -> k1));

        List<MasterPermissionEntity> allPermissions = masterPermissionRepository.findAll();
        Map<String, List<String>> menuPermissionsMap = new HashMap<>();
        for (MasterPermissionEntity perm : allPermissions) {
            menuPermissionsMap.computeIfAbsent(perm.getMenuCode(), k -> new ArrayList<>()).add(perm.getPermissionCode());
        }

        Set<String> enabledTenantMenuCodes = null;
        if (tenantId != null && !tenantId.equalsIgnoreCase("SYSTEM") && !"SUPERADMIN".equalsIgnoreCase(portalType)) {
            List<TenantMenuEntity> tenantMenus = tenantMenuRepository.findByTenantIdAndIsEnabledTrue(tenantId);
            enabledTenantMenuCodes = tenantMenus.stream().map(TenantMenuEntity::getMenuCode).collect(Collectors.toSet());
        }

        String rootPortalCode = switch (portalType.toUpperCase()) {
            case "SUPERADMIN" -> "PORTAL_SUPERADMIN";
            case "CORP", "CORP_ADMIN" -> "PORTAL_CORP_ADMIN";
            default -> "PORTAL_CONSUMER_APP";
        };

        Set<String> allowedCodes = new HashSet<>();
        collectAllowedMenuCodes(rootPortalCode, allMenus, allowedCodes);

        Map<String, DynamicMenuNodeDto> nodeMap = new HashMap<>();
        List<DynamicMenuNodeDto> rootNodes = new ArrayList<>();

        for (MasterMenuEntity menu : allMenus) {
            if (!allowedCodes.contains(menu.getCode()) && !menu.getCode().equalsIgnoreCase(rootPortalCode)) {
                continue;
            }

            if (enabledTenantMenuCodes != null && !menu.getCode().equalsIgnoreCase(rootPortalCode)) {
                if (!enabledTenantMenuCodes.contains(menu.getCode())) {
                    continue;
                }
            }

            String title = menu.getCode();
            if (i18nMap.containsKey(menu.getCode())) {
                title = i18nMap.get(menu.getCode()).getTitle();
            } else if (fallbackViMap.containsKey(menu.getCode())) {
                title = fallbackViMap.get(menu.getCode()).getTitle();
            }

            DynamicMenuNodeDto node = DynamicMenuNodeDto.builder()
                    .code(menu.getCode())
                    .title(title)
                    .routePath(menu.getRoutePath())
                    .icon(menu.getIcon())
                    .portalType(menu.getPortalType())
                    .sortOrder(menu.getSortOrder())
                    .isLeaf(menu.getIsLeaf())
                    .requiredPermissions(menuPermissionsMap.getOrDefault(menu.getCode(), Collections.emptyList()))
                    .children(new ArrayList<>())
                    .build();

            nodeMap.put(menu.getCode(), node);
        }

        for (MasterMenuEntity menu : allMenus) {
            DynamicMenuNodeDto currentNode = nodeMap.get(menu.getCode());
            if (currentNode == null) continue;

            String parentCode = menu.getParentCode();
            if (parentCode == null || parentCode.isBlank() || parentCode.equalsIgnoreCase("ROOT") || !nodeMap.containsKey(parentCode)) {
                rootNodes.add(currentNode);
            } else {
                DynamicMenuNodeDto parentNode = nodeMap.get(parentCode);
                parentNode.getChildren().add(currentNode);
            }
        }

        rootNodes.sort(Comparator.comparingInt(DynamicMenuNodeDto::getSortOrder));
        sortChildrenRecursive(rootNodes);

        return rootNodes;
    }

    private void collectAllowedMenuCodes(String parentCode, List<MasterMenuEntity> allMenus, Set<String> allowed) {
        for (MasterMenuEntity menu : allMenus) {
            if (parentCode.equalsIgnoreCase(menu.getParentCode())) {
                allowed.add(menu.getCode());
                collectAllowedMenuCodes(menu.getCode(), allMenus, allowed);
            }
        }
    }

    private void sortChildrenRecursive(List<DynamicMenuNodeDto> nodes) {
        for (DynamicMenuNodeDto node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                node.getChildren().sort(Comparator.comparingInt(DynamicMenuNodeDto::getSortOrder));
                sortChildrenRecursive(node.getChildren());
            }
        }
    }
}
