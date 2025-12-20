package com.system.brands.Service;

import com.system.brands.Dto.BrandRequestDto;
import com.system.brands.Dto.BrandResponseDto;
import com.system.brands.Dto.ProductResponseDto;
import com.system.brands.Exception.BadRequestException;
import com.system.brands.Exception.DuplicateResourceException;
import com.system.brands.Exception.ResourceNotFoundException;
import com.system.brands.Model.Brand;
import com.system.brands.Repository.BrandRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<BrandResponseDto> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brand -> convertToBrandResponseDto(brand, false)) // Don't include products for list
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BrandResponseDto getBrandById(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        return convertToBrandResponseDto(brand, true); // Include products for single brand
    }

    @Transactional(readOnly = true)
    public Brand getBrandEntityById(Integer id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
    }

    @Transactional
    public BrandResponseDto createBrand(BrandRequestDto requestDto, MultipartFile image) throws IOException {
        // Validate request DTO
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new BadRequestException("Brand name is required");
        }

        String brandName = requestDto.getName().trim();
        if (brandRepository.existsByName(brandName)) {
            throw new DuplicateResourceException("Brand", "name", brandName);
        }

        byte[] imageBytes = null;
        if (image != null && !image.isEmpty()) {
            imageBytes = image.getBytes();
        }

        Brand brand = Brand.builder()
                .name(brandName)
                .image(imageBytes)
                .build();

        log.debug("Creating brand with name: {}", brandName);
        Brand savedBrand = brandRepository.save(brand);

        // Flush to ensure the entity is persisted immediately
        entityManager.flush();
        entityManager.refresh(savedBrand);

        log.debug("Brand saved successfully with ID: {}", savedBrand.getId());

        // Verify the brand was saved
        if (savedBrand == null || savedBrand.getId() == null) {
            throw new RuntimeException("Failed to save brand to database");
        }

        // Double-check by querying the database
        Brand verifiedBrand = brandRepository.findById(savedBrand.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Brand was saved but cannot be retrieved from database. ID: " + savedBrand.getId()));

        log.info("Brand verified in database - ID: {}, Name: {}", verifiedBrand.getId(), verifiedBrand.getName());

        BrandResponseDto response = convertToBrandResponseDto(verifiedBrand, false);
        log.debug("Returning brand response with ID: {}", response.getId());
        return response;
    }

    @Transactional
    public BrandResponseDto updateBrand(Integer id, BrandRequestDto requestDto, MultipartFile image)
            throws IOException {
        // Validate request DTO
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new BadRequestException("Brand name is required");
        }

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        String brandName = requestDto.getName().trim();
        // Check if new name already exists for another brand
        if (!brand.getName().equals(brandName) &&
                brandRepository.existsByName(brandName)) {
            throw new DuplicateResourceException("Brand", "name", brandName);
        }

        brand.setName(brandName);

        // Handle image update
        if (image != null && !image.isEmpty()) {
            brand.setImage(image.getBytes());
        }

        log.debug("Updating brand with ID: {}", id);
        Brand updatedBrand = brandRepository.save(brand);

        // Flush to ensure the entity is persisted immediately
        entityManager.flush();
        entityManager.refresh(updatedBrand);

        log.debug("Brand updated successfully with ID: {}", updatedBrand.getId());

        // Verify the brand was updated
        if (updatedBrand == null || updatedBrand.getId() == null) {
            throw new RuntimeException("Failed to update brand in database");
        }

        BrandResponseDto response = convertToBrandResponseDto(updatedBrand, false);
        log.debug("Returning updated brand response with ID: {}", response.getId());
        return response;
    }

    @Transactional
    public void deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        brandRepository.delete(brand);
    }

    /**
     * Converts a Brand entity to BrandResponseDto.
     * 
     * @param brand           The brand entity to convert
     * @param includeProducts Whether to include the list of products in the
     *                        response
     * @return BrandResponseDto
     */
    private BrandResponseDto convertToBrandResponseDto(Brand brand, boolean includeProducts) {
        if (brand == null) {
            throw new IllegalArgumentException("Brand cannot be null");
        }

        BrandResponseDto.BrandResponseDtoBuilder builder = BrandResponseDto.builder()
                .id(brand.getId())
                .name(brand.getName());

        // Only include products if requested (for optimization)
        if (includeProducts && brand.getProducts() != null && !brand.getProducts().isEmpty()) {
            List<ProductResponseDto> products = brand.getProducts().stream()
                    .map(product -> {
                        ProductResponseDto.ProductResponseDtoBuilder productBuilder = ProductResponseDto.builder()
                                .id(product.getId())
                                .brandId(brand.getId())
                                .brandName(brand.getName())
                                .name(product.getName());

                        if (product.getCategory() != null) {
                            productBuilder.categoryId(product.getCategory().getId())
                                    .categoryName(product.getCategory().getName());
                        }

                        if (product.getProductOrder() != null) {
                            productBuilder.productOrder(product.getProductOrder());
                        }

                        if (product.getImage() != null) {
                            productBuilder.image(Base64.getEncoder().encodeToString(product.getImage()))
                                    .imageUrl("/api/products/" + product.getId() + "/image");
                        }

                        return productBuilder.build();
                    })
                    .collect(Collectors.toList());
            builder.products(products);
        }

        if (brand.getImage() != null) {
            builder.image(Base64.getEncoder().encodeToString(brand.getImage()))
                    .imageUrl("/api/brands/" + brand.getId() + "/image");
        }

        return builder.build();
    }
}
