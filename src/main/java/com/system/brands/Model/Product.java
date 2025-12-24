package com.system.brands.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "brands_schema", name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_s3_key", length = 500)
    private String imageS3Key;

    @Column(name = "priority")
    private Integer productOrder;

    @Column(name = "packaging")
    private String packaging;

    @Column(name = "is_new")
    private Boolean isNew;

    @Column(name = "is_hidden")
    private Boolean isHidden;
}
