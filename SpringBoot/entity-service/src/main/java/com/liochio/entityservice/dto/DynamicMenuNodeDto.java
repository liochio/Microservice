package com.liochio.entityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicMenuNodeDto {

    private String code;
    private String title;
    private String routePath;
    private String icon;
    private String portalType;
    private Integer sortOrder;
    private Boolean isLeaf;
    private List<String> requiredPermissions;

    @Builder.Default
    private List<DynamicMenuNodeDto> children = new ArrayList<>();
}
