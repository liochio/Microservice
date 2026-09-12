package com.liochio.ai.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "ai_prompts")
@SQLDelete(sql = "UPDATE ai_prompts SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPromptEntity extends BaseEntity {

    @Column(name = "prompt_code", length = 50, nullable = false)
    private String promptCode;

    @Column(name = "prompt_title", length = 150, nullable = false)
    private String promptTitle;

    @Column(name = "prompt_template", columnDefinition = "LONGTEXT", nullable = false)
    private String promptTemplate;

    @Column(name = "variables_json", columnDefinition = "JSON")
    private String variablesJson;
}
