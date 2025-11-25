package com.system.brands.Service;

import com.system.brands.Dto.ProductRequestDto;
import com.system.brands.Dto.ProductResponseDto;
import com.system.brands.Exception.ResourceNotFoundException;
import com.system.brands.Model.Brand;
import com.system.brands.Model.Product;
import com.system.brands.Repository.BrandRepository;
import com.system.brands.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToProductResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return convertToProductResponseDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsByBrandId(Integer brandId) {
        return productRepository.findByBrandId(brandId).stream()
                .map(this::convertToProductResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        Brand brand = brandRepository.findById(requestDto.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", requestDto.getBrandId()));

        Product product = Product.builder()
                .brand(brand)
                .name(requestDto.getName())
                .quantity(requestDto.getQuantity())
                .packaging(requestDto.getPackaging())
                .unit(requestDto.getUnit())
                .build();

        Product savedProduct = productRepository.save(product);
        return convertToProductResponseDto(savedProduct);
    }

    @Transactional
    public ProductResponseDto updateProduct(Integer id, ProductRequestDto requestDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Brand brand = brandRepository.findById(requestDto.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", requestDto.getBrandId()));

        product.setBrand(brand);
        product.setName(requestDto.getName());
        product.setQuantity(requestDto.getQuantity());
        product.setPackaging(requestDto.getPackaging());
        product.setUnit(requestDto.getUnit());

        Product updatedProduct = productRepository.save(product);
        return convertToProductResponseDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
    }

    private ProductResponseDto convertToProductResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .brandId(product.getBrand().getId())
                .brandName(product.getBrand().getName())
                .name(product.getName())
                .quantity(product.getQuantity())
                .packaging(product.getPackaging())
                .unit(product.getUnit())
                .build();
    }
}

