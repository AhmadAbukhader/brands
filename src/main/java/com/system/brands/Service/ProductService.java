package com.system.brands.Service;

import com.system.brands.Dto.ProductOrderRequestDto;
import com.system.brands.Dto.ProductRequestDto;
import com.system.brands.Dto.ProductResponseDto;
import com.system.brands.Exception.ResourceNotFoundException;
import com.system.brands.Model.Brand;
import com.system.brands.Model.Category;
import com.system.brands.Model.Product;
import com.system.brands.Repository.BrandRepository;
import com.system.brands.Repository.CategoryRepository;
import com.system.brands.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

        private final ProductRepository productRepository;
        private final BrandRepository brandRepository;
        private final CategoryRepository categoryRepository;

        @Transactional(readOnly = true)
        public List<ProductResponseDto> getAllProducts() {
                return productRepository.findAllOrderedByProductOrder().stream()
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
        public Product getProductEntityById(Integer id) {
                return productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        }

        @Transactional(readOnly = true)
        public List<ProductResponseDto> getProductsByBrandId(Integer brandId) {
                return productRepository.findByBrandIdOrderedByProductOrder(brandId).stream()
                                .map(this::convertToProductResponseDto)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<ProductResponseDto> getProductsByCategoryId(Integer categoryId) {
                return productRepository.findByCategoryIdOrderedByProductOrder(categoryId).stream()
                                .map(this::convertToProductResponseDto)
                                .collect(Collectors.toList());
        }

        @Transactional
        public ProductResponseDto createProduct(ProductRequestDto requestDto, MultipartFile image) throws IOException {
                Brand brand = brandRepository.findById(requestDto.getBrandId())
                                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id",
                                                requestDto.getBrandId()));

                Category category = null;
                if (requestDto.getCategoryId() != null) {
                        category = categoryRepository.findById(requestDto.getCategoryId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                                                        requestDto.getCategoryId()));
                }

                byte[] imageBytes = null;
                if (image != null && !image.isEmpty()) {
                        imageBytes = image.getBytes();
                }

                Product product = Product.builder()
                                .brand(brand)
                                .category(category)
                                .name(requestDto.getName())
                                .image(imageBytes)
                                .build();

                Product savedProduct = productRepository.save(product);
                // Initialize productOrder to id if not set
                if (savedProduct.getProductOrder() == null) {
                        savedProduct.setProductOrder(savedProduct.getId());
                        savedProduct = productRepository.save(savedProduct);
                }
                return convertToProductResponseDto(savedProduct);
        }

        @Transactional
        public ProductResponseDto updateProduct(Integer id, ProductRequestDto requestDto, MultipartFile image)
                        throws IOException {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

                Brand brand = brandRepository.findById(requestDto.getBrandId())
                                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id",
                                                requestDto.getBrandId()));

                Category category = null;
                if (requestDto.getCategoryId() != null) {
                        category = categoryRepository.findById(requestDto.getCategoryId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                                                        requestDto.getCategoryId()));
                }

                product.setBrand(brand);
                product.setCategory(category);
                product.setName(requestDto.getName());

                // Handle image update
                if (image != null && !image.isEmpty()) {
                        product.setImage(image.getBytes());
                }

                Product updatedProduct = productRepository.save(product);
                return convertToProductResponseDto(updatedProduct);
        }

        @Transactional
        public void deleteProduct(Integer id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
                productRepository.delete(product);
        }

        @Transactional
        public ProductResponseDto reorderProduct(ProductOrderRequestDto requestDto) {
                Product product = productRepository.findById(requestDto.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id",
                                                requestDto.getProductId()));

                Integer currentOrder = product.getProductOrder();
                Integer newOrder = requestDto.getNewOrder();

                if (currentOrder == null) {
                        // If product has no order, initialize it to its id
                        currentOrder = product.getId();
                        product.setProductOrder(currentOrder);
                        productRepository.save(product);
                }

                if (currentOrder.equals(newOrder)) {
                        // No change needed
                        return convertToProductResponseDto(product);
                }

                if (newOrder < currentOrder) {
                        // Product moved upwards (e.g., 55 → 5)
                        // Shift products from newOrder to currentOrder-1 up by 1 (increase their order
                        // values)
                        productRepository.shiftOrdersUp(newOrder, currentOrder);
                } else {
                        // Product moved downwards (e.g., 5 → 55)
                        // Shift products from currentOrder+1 to newOrder down by 1 (decrease their
                        // order values)
                        productRepository.shiftOrdersDown(currentOrder, newOrder);
                }

                // Set the new order for the product
                product.setProductOrder(newOrder);
                productRepository.save(product);

                // Reload the product from database to ensure we have the latest data
                Product updatedProduct = productRepository.findById(requestDto.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id",
                                                requestDto.getProductId()));
                return convertToProductResponseDto(updatedProduct);
        }

        private ProductResponseDto convertToProductResponseDto(Product product) {
                ProductResponseDto.ProductResponseDtoBuilder builder = ProductResponseDto.builder()
                                .id(product.getId())
                                .brandId(product.getBrand().getId())
                                .brandName(product.getBrand().getName())
                                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                                .name(product.getName())
                                .productOrder(product.getProductOrder());

                if (product.getImage() != null) {
                        builder.image(Base64.getEncoder().encodeToString(product.getImage()))
                                        .imageUrl("/api/products/" + product.getId() + "/image");
                }

                return builder.build();
        }
}
