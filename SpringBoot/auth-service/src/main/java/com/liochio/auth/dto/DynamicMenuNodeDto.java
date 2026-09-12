package com.liochio.auth.dto;

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
    private String parentCode;
    private String portalType;
    private String routePath;
    private String icon;
    private Integer sortOrder;
    private Boolean isLeaf;
    private String title;
    private String description;
    @Builder.Default
    private List<String> permissions = new ArrayList<>();
    @Builder.Default
    private List<DynamicMenuNodeDto> children = new ArrayList<>();
}
