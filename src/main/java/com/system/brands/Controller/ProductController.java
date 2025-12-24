package com.system.brands.Controller;

import com.system.brands.Dto.ProductOrderRequestDto;
import com.system.brands.Dto.ProductRequestDto;
import com.system.brands.Dto.ProductResponseDto;
import com.system.brands.Exception.BadRequestException;
import com.system.brands.Service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

        private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
                        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp");
        private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

        private final ProductService productService;

        @GetMapping
        @Operation(summary = "Get all products", description = "Retrieve a list of all products")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
        })
        public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
                List<ProductResponseDto> products = productService.getAllProducts();
                return ResponseEntity.ok(products);
        }

        @PutMapping("/reorder")
        @Operation(summary = "Reorder a product", description = "Change the order position of a product. Other products will be shifted accordingly.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product reordered successfully"),
                        @ApiResponse(responseCode = "404", description = "Product not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ProductResponseDto> reorderProduct(
                        @Valid @RequestBody ProductOrderRequestDto requestDto) {
                ProductResponseDto product = productService.reorderProduct(requestDto);
                return ResponseEntity.ok(product);
        }

        @GetMapping("/{id:\\d+}")
        @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
                        @ApiResponse(responseCode = "404", description = "Product not found")
        })
        public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Integer id) {
                ProductResponseDto product = productService.getProductById(id);
                return ResponseEntity.ok(product);
        }

        @GetMapping("/brand/{brandId}")
        @Operation(summary = "Get products by brand ID", description = "Retrieve all products belonging to a specific brand")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
        })
        public ResponseEntity<List<ProductResponseDto>> getProductsByBrandId(@PathVariable Integer brandId) {
                List<ProductResponseDto> products = productService.getProductsByBrandId(brandId);
                return ResponseEntity.ok(products);
        }

        @GetMapping("/category/{categoryId}")
        @Operation(summary = "Get products by category ID", description = "Retrieve all products belonging to a specific category")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
        })
        public ResponseEntity<List<ProductResponseDto>> getProductsByCategoryId(@PathVariable Integer categoryId) {
                List<ProductResponseDto> products = productService.getProductsByCategoryId(categoryId);
                return ResponseEntity.ok(products);
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Create a new product", description = "Create a new product with the provided information and optional image")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Product created successfully"),
                        @ApiResponse(responseCode = "404", description = "Brand not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ProductResponseDto> createProduct(
                        @Parameter(description = "Brand ID", required = true) @RequestParam("brandId") Integer brandId,
                        @Parameter(description = "Category ID") @RequestParam(value = "categoryId", required = false) Integer categoryId,
                        @Parameter(description = "Product name", required = true) @RequestParam("name") String name,
                        @Parameter(description = "Product packaging information") @RequestParam(value = "packaging", required = false) String packaging,
                        @Parameter(description = "Whether the product is new") @RequestParam(value = "isNew", required = false) Boolean isNew,
                        @Parameter(description = "Whether the product is hidden") @RequestParam(value = "isHidden", required = false) Boolean isHidden,
                        @Parameter(description = "Product image file") @RequestPart(value = "image", required = false) MultipartFile image)
                        throws IOException {
                // Validate image if provided
                if (image != null && !image.isEmpty()) {
                        validateImageFile(image);
                        log.info("Received image file: name={}, size={} bytes, contentType={}",
                                        image.getOriginalFilename(), image.getSize(), image.getContentType());
                }

                ProductRequestDto requestDto = ProductRequestDto.builder()
                                .brandId(brandId)
                                .categoryId(categoryId)
                                .name(name)
                                .packaging(packaging)
                                .isNew(isNew)
                                .isHidden(isHidden)
                                .build();
                ProductResponseDto product = productService.createProduct(requestDto, image);
                return ResponseEntity.status(HttpStatus.CREATED).body(product);
        }

        @PutMapping(value = "/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Update a product", description = "Update an existing product by its ID. All fields are optional - only provided fields will be updated.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Product or Brand not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ProductResponseDto> updateProduct(
                        @PathVariable Integer id,
                        @Parameter(description = "Brand ID (optional)") @RequestParam(value = "brandId", required = false) Integer brandId,
                        @Parameter(description = "Category ID (optional)") @RequestParam(value = "categoryId", required = false) Integer categoryId,
                        @Parameter(description = "Product name (optional)") @RequestParam(value = "name", required = false) String name,
                        @Parameter(description = "Product packaging information (optional)") @RequestParam(value = "packaging", required = false) String packaging,
                        @Parameter(description = "Whether the product is new (optional)") @RequestParam(value = "isNew", required = false) Boolean isNew,
                        @Parameter(description = "Whether the product is hidden (optional)") @RequestParam(value = "isHidden", required = false) Boolean isHidden,
                        @Parameter(description = "Product image file (optional)") @RequestPart(value = "image", required = false) MultipartFile image)
                        throws IOException {
                // Validate image if provided
                if (image != null && !image.isEmpty()) {
                        validateImageFile(image);
                        log.info("Received image file for update: name={}, size={} bytes, contentType={}",
                                        image.getOriginalFilename(), image.getSize(), image.getContentType());
                }

                ProductRequestDto requestDto = ProductRequestDto.builder()
                                .brandId(brandId)
                                .categoryId(categoryId)
                                .name(name)
                                .packaging(packaging)
                                .isNew(isNew)
                                .isHidden(isHidden)
                                .build();
                ProductResponseDto product = productService.updateProduct(id, requestDto, image);
                return ResponseEntity.ok(product);
        }

        @DeleteMapping("/{id:\\d+}")
        @Operation(summary = "Delete a product", description = "Delete a product by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Product not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
                productService.deleteProduct(id);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{id:\\d+}/image")
        @Operation(summary = "Delete product image", description = "Delete only the image of a product from S3 storage without deleting the product itself")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Product image deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Product not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ProductResponseDto> deleteProductImage(@PathVariable Integer id) {
                ProductResponseDto product = productService.deleteProductImage(id);
                return ResponseEntity.ok(product);
        }

        /**
         * Validates the uploaded image file for size and content type.
         */
        private void validateImageFile(MultipartFile image) {
                // Check file size
                if (image.getSize() > MAX_FILE_SIZE) {
                        throw new BadRequestException(
                                        String.format("File size exceeds maximum allowed size of %d MB. Your file: %.2f MB",
                                                        MAX_FILE_SIZE / (1024 * 1024),
                                                        image.getSize() / (1024.0 * 1024.0)));
                }

                // Check content type
                String contentType = image.getContentType();
                if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                        throw new BadRequestException(
                                        String.format("Invalid file type: %s. Allowed types: %s",
                                                        contentType, String.join(", ", ALLOWED_CONTENT_TYPES)));
                }

                log.debug("Image validation passed: size={} bytes, type={}", image.getSize(), contentType);
        }
}
