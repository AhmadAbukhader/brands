package com.system.brands.Service;

import com.system.brands.Dto.BrandRequestDto;
import com.system.brands.Dto.BrandResponseDto;
import com.system.brands.Dto.ProductResponseDto;
import com.system.brands.Exception.DuplicateResourceException;
import com.system.brands.Exception.ResourceNotFoundException;
import com.system.brands.Model.Brand;
import com.system.brands.Repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandResponseDto> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::convertToBrandResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BrandResponseDto getBrandById(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        return convertToBrandResponseDto(brand);
    }

    @Transactional
    public BrandResponseDto createBrand(BrandRequestDto requestDto) {
        if (brandRepository.existsByName(requestDto.getName())) {
            throw new DuplicateResourceException("Brand", "name", requestDto.getName());
        }

        Brand brand = Brand.builder()
                .name(requestDto.getName())
                .build();

        Brand savedBrand = brandRepository.save(brand);
        return convertToBrandResponseDto(savedBrand);
    }

    @Transactional
    public BrandResponseDto updateBrand(Integer id, BrandRequestDto requestDto) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));

        // Check if new name already exists for another brand
        if (!brand.getName().equals(requestDto.getName()) &&
                brandRepository.existsByName(requestDto.getName())) {
            throw new DuplicateResourceException("Brand", "name", requestDto.getName());
        }

        brand.setName(requestDto.getName());
        Brand updatedBrand = brandRepository.save(brand);
        return convertToBrandResponseDto(updatedBrand);
    }

    @Transactional
    public void deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
        brandRepository.delete(brand);
    }

    private BrandResponseDto convertToBrandResponseDto(Brand brand) {
        List<ProductResponseDto> products = null;

        if (brand.getProducts() != null) {
            products = brand.getProducts().stream()
                    .map(product -> {
                        ProductResponseDto.ProductResponseDtoBuilder builder = ProductResponseDto.builder()
                                .id(product.getId())
                                .brandId(brand.getId())
                                .brandName(brand.getName())
                                .name(product.getName());

                        if (product.getCategory() != null) {
                            builder.categoryId(product.getCategory().getId())
                                    .categoryName(product.getCategory().getName());
                        }

                        if (product.getProductOrder() != null) {
                            builder.productOrder(product.getProductOrder());
                        }

                        return builder.build();
                    })
                    .collect(Collectors.toList());
        }

        return BrandResponseDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .products(products)
                .build();
    }
}
