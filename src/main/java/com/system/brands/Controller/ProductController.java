package com.system.brands.Controller;

import com.system.brands.Dto.ProductOrderRequestDto;
import com.system.brands.Dto.ProductRequestDto;
import com.system.brands.Dto.ProductResponseDto;
import com.system.brands.Model.Product;
import com.system.brands.Service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

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
    public ResponseEntity<ProductResponseDto> reorderProduct(@Valid @RequestBody ProductOrderRequestDto requestDto) {
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
            @Parameter(description = "Brand ID", required = true) @RequestPart("brandId") Integer brandId,
            @Parameter(description = "Category ID") @RequestPart(value = "categoryId", required = false) Integer categoryId,
            @Parameter(description = "Product name", required = true) @RequestPart("name") String name,
            @Parameter(description = "Product image file") @RequestPart(value = "image", required = false) MultipartFile image)
            throws IOException {
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .brandId(brandId)
                .categoryId(categoryId)
                .name(name)
                .build();
        ProductResponseDto product = productService.createProduct(requestDto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a product", description = "Update an existing product by its ID with optional new image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product or Brand not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Integer id,
            @Parameter(description = "Brand ID", required = true) @RequestPart("brandId") Integer brandId,
            @Parameter(description = "Category ID") @RequestPart(value = "categoryId", required = false) Integer categoryId,
            @Parameter(description = "Product name", required = true) @RequestPart("name") String name,
            @Parameter(description = "Product image file") @RequestPart(value = "image", required = false) MultipartFile image)
            throws IOException {
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .brandId(brandId)
                .categoryId(categoryId)
                .name(name)
                .build();
        ProductResponseDto product = productService.updateProduct(id, requestDto, image);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id:\\d+}/image")
    @Operation(summary = "Get product image", description = "Retrieve the image for a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found or no image available")
    })
    public ResponseEntity<byte[]> getProductImage(@PathVariable Integer id) {
        Product product = productService.getProductEntityById(id);

        if (product.getImage() == null || product.getImage().length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Default to JPEG, you can enhance this
                .body(product.getImage());
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
}
