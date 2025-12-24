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
public class ProductService {

        private final ProductRepository productRepository;
        private final BrandRepository brandRepository;
        private final CategoryRepository categoryRepository;
        private final S3StorageService s3StorageService;

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

                // Upload image to S3
                String imageS3Key = null;
                if (image != null && !image.isEmpty()) {
                        imageS3Key = s3StorageService.uploadFile(image, "products");
                        log.info("Product image uploaded to S3: key={}", imageS3Key);
                }

                Product product = Product.builder()
                                .brand(brand)
                                .category(category)
                                .name(requestDto.getName())
                                .imageS3Key(imageS3Key)
                                .packaging(requestDto.getPackaging())
                                .isNew(requestDto.getIsNew())
                                .isHidden(requestDto.getIsHidden())
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

                // Only update brand if provided
                if (requestDto.getBrandId() != null) {
                        Brand brand = brandRepository.findById(requestDto.getBrandId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Brand", "id",
                                                        requestDto.getBrandId()));
                        product.setBrand(brand);
                }

                // Only update category if provided
                if (requestDto.getCategoryId() != null) {
                        Category category = categoryRepository.findById(requestDto.getCategoryId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Category", "id",
                                                        requestDto.getCategoryId()));
                        product.setCategory(category);
                }

                // Only update name if provided
                if (requestDto.getName() != null && !requestDto.getName().trim().isEmpty()) {
                        product.setName(requestDto.getName().trim());
                }

                // Only update packaging if provided
                if (requestDto.getPackaging() != null) {
                        product.setPackaging(requestDto.getPackaging().trim().isEmpty() ? null
                                        : requestDto.getPackaging().trim());
                }

                // Only update isNew if provided
                if (requestDto.getIsNew() != null) {
                        product.setIsNew(requestDto.getIsNew());
                }

                // Only update isHidden if provided
                if (requestDto.getIsHidden() != null) {
                        product.setIsHidden(requestDto.getIsHidden());
                }

                // Handle image update
                if (image != null && !image.isEmpty()) {
                        // Delete old image from S3 if exists
                        if (product.getImageS3Key() != null) {
                                s3StorageService.deleteFile(product.getImageS3Key());
                                log.info("Old product image deleted from S3: key={}", product.getImageS3Key());
                        }
                        // Upload new image to S3
                        String imageS3Key = s3StorageService.uploadFile(image, "products");
                        product.setImageS3Key(imageS3Key);
                        log.info("New product image uploaded to S3: key={}", imageS3Key);
                }

                Product updatedProduct = productRepository.save(product);
                return convertToProductResponseDto(updatedProduct);
        }

        @Transactional
        public void deleteProduct(Integer id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

                // Delete associated image from S3
                if (product.getImageS3Key() != null) {
                        s3StorageService.deleteFile(product.getImageS3Key());
                        log.info("Product image deleted from S3: key={}", product.getImageS3Key());
                }

                productRepository.delete(product);
        }

        @Transactional
        public ProductResponseDto deleteProductImage(Integer id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

                // Delete image from S3 if exists
                if (product.getImageS3Key() != null) {
                        s3StorageService.deleteFile(product.getImageS3Key());
                        log.info("Product image deleted from S3: key={}", product.getImageS3Key());
                        product.setImageS3Key(null);
                        product = productRepository.save(product);
                }

                return convertToProductResponseDto(product);
        }

        @Transactional
        public ProductResponseDto reorderProduct(ProductOrderRequestDto requestDto) {
                Product product = productRepository.findById(requestDto.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException("Product", "id",
                                                requestDto.getProductId()));

                Integer currentOrder = product.getProductOrder();
                Integer newOrder = requestDto.getNewOrder();

                if (currentOrder == null) {
                        currentOrder = product.getId();
                        product.setProductOrder(currentOrder);
                        productRepository.save(product);
                }

                if (currentOrder.equals(newOrder)) {
                        return convertToProductResponseDto(product);
                }

                if (newOrder < currentOrder) {
                        productRepository.shiftOrdersUp(newOrder, currentOrder);
                } else {
                        productRepository.shiftOrdersDown(currentOrder, newOrder);
                }

                product.setProductOrder(newOrder);
                productRepository.save(product);

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
                                .productOrder(product.getProductOrder())
                                .packaging(product.getPackaging())
                                .isNew(product.getIsNew())
                                .isHidden(product.getIsHidden());

                // Get image URL from S3
                if (product.getImageS3Key() != null) {
                        String imageUrl = s3StorageService.getFileUrl(product.getImageS3Key());
                        builder.imageUrl(imageUrl);
                }

                return builder.build();
        }
}
