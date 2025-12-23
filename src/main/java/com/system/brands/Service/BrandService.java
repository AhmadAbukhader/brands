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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final S3StorageService s3StorageService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<BrandResponseDto> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brand -> convertToBrandResponseDto(brand, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BrandResponseDto getBrandById(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        return convertToBrandResponseDto(brand, true);
    }

    @Transactional(readOnly = true)
    public Brand getBrandEntityById(Integer id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
    }

    @Transactional
    public BrandResponseDto createBrand(BrandRequestDto requestDto, MultipartFile image) throws IOException {
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new BadRequestException("Brand name is required");
        }

        String brandName = requestDto.getName().trim();
        if (brandRepository.existsByName(brandName)) {
            throw new DuplicateResourceException("Brand", "name", brandName);
        }

        // Upload image to S3
        String imageS3Key = null;
        if (image != null && !image.isEmpty()) {
            imageS3Key = s3StorageService.uploadFile(image, "brands");
            log.info("Brand image uploaded to S3: key={}", imageS3Key);
        }

        Brand brand = Brand.builder()
                .name(brandName)
                .imageS3Key(imageS3Key)
                .build();

        log.debug("Creating brand with name: {}", brandName);
        Brand savedBrand = brandRepository.save(brand);

        entityManager.flush();
        entityManager.refresh(savedBrand);

        log.debug("Brand saved successfully with ID: {}", savedBrand.getId());

        if (savedBrand == null || savedBrand.getId() == null) {
            throw new RuntimeException("Failed to save brand to database");
        }

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
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // Only update name if provided
        if (requestDto != null && requestDto.getName() != null && !requestDto.getName().trim().isEmpty()) {
            String brandName = requestDto.getName().trim();
            // Check for duplicate name only if name is changing
            if (!brand.getName().equals(brandName) &&
                    brandRepository.existsByName(brandName)) {
                throw new DuplicateResourceException("Brand", "name", brandName);
            }
            brand.setName(brandName);
        }

        // Handle image update
        if (image != null && !image.isEmpty()) {
            // Delete old image from S3 if exists
            if (brand.getImageS3Key() != null) {
                s3StorageService.deleteFile(brand.getImageS3Key());
                log.info("Old brand image deleted from S3: key={}", brand.getImageS3Key());
            }
            // Upload new image to S3
            String imageS3Key = s3StorageService.uploadFile(image, "brands");
            brand.setImageS3Key(imageS3Key);
            log.info("New brand image uploaded to S3: key={}", imageS3Key);
        }

        log.debug("Updating brand with ID: {}", id);
        Brand updatedBrand = brandRepository.save(brand);

        entityManager.flush();
        entityManager.refresh(updatedBrand);

        log.debug("Brand updated successfully with ID: {}", updatedBrand.getId());

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

        // Delete associated image from S3
        if (brand.getImageS3Key() != null) {
            s3StorageService.deleteFile(brand.getImageS3Key());
            log.info("Brand image deleted from S3: key={}", brand.getImageS3Key());
        }

        brandRepository.delete(brand);
    }

    @Transactional
    public BrandResponseDto deleteBrandImage(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // Delete image from S3 if exists
        if (brand.getImageS3Key() != null) {
            s3StorageService.deleteFile(brand.getImageS3Key());
            log.info("Brand image deleted from S3: key={}", brand.getImageS3Key());
            brand.setImageS3Key(null);
            brand = brandRepository.save(brand);
        }

        return convertToBrandResponseDto(brand, false);
    }

    private BrandResponseDto convertToBrandResponseDto(Brand brand, boolean includeProducts) {
        if (brand == null) {
            throw new IllegalArgumentException("Brand cannot be null");
        }

        BrandResponseDto.BrandResponseDtoBuilder builder = BrandResponseDto.builder()
                .id(brand.getId())
                .name(brand.getName());

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

                        if (product.getPackaging() != null) {
                            productBuilder.packaging(product.getPackaging());
                        }

                        // Get image URL from S3
                        if (product.getImageS3Key() != null) {
                            String imageUrl = s3StorageService.getFileUrl(product.getImageS3Key());
                            productBuilder.imageUrl(imageUrl);
                        }

                        return productBuilder.build();
                    })
                    .collect(Collectors.toList());
            builder.products(products);
        }

        // Get image URL from S3
        if (brand.getImageS3Key() != null) {
            String imageUrl = s3StorageService.getFileUrl(brand.getImageS3Key());
            builder.imageUrl(imageUrl);
        }

        return builder.build();
    }
}
