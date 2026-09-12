package com.liochio.film.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "streaming_servers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamingServerEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_name", length = 100, nullable = false)
    private String serverName;

    @Column(name = "base_url", length = 500, nullable = false)
    private String baseUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
