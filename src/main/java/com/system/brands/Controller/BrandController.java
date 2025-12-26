package com.system.brands.Controller;

import com.system.brands.Dto.BrandRequestDto;
import com.system.brands.Dto.BrandResponseDto;
import com.system.brands.Exception.BadRequestException;
import com.system.brands.Service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "APIs for managing brands")
@SecurityRequirement(name = "bearerAuth")
public class BrandController {

        private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
                        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp");
        private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

        private final BrandService brandService;

        @GetMapping
        @Operation(summary = "Get all brands", description = "Retrieve a list of all brands")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<List<BrandResponseDto>> getAllBrands() {
                List<BrandResponseDto> brands = brandService.getAllBrands();
                return ResponseEntity.ok(brands);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get brand by ID", description = "Retrieve a specific brand by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved brand"),
                        @ApiResponse(responseCode = "404", description = "Brand not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<BrandResponseDto> getBrandById(@PathVariable Integer id) {
                BrandResponseDto brand = brandService.getBrandById(id);
                return ResponseEntity.ok(brand);
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Create a new brand", description = "Create a new brand with the provided information and optional image")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Brand created successfully"),
                        @ApiResponse(responseCode = "409", description = "Brand already exists"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<BrandResponseDto> createBrand(
                        @Parameter(description = "Brand name", required = true) @RequestParam("name") String name,
                        @Parameter(description = "Brand name in English") @RequestParam(value = "nameEnglish", required = false) String nameEnglish,
                        @Parameter(description = "Brand image file") @RequestPart(value = "image", required = false) MultipartFile image)
                        throws IOException {
                if (name == null || name.trim().isEmpty()) {
                        throw new BadRequestException("Brand name is required");
                }

                // Validate image if provided
                if (image != null && !image.isEmpty()) {
                        validateImageFile(image);
                        log.info("Received image file: name={}, size={} bytes, contentType={}",
                                        image.getOriginalFilename(), image.getSize(), image.getContentType());
                } else {
                        log.info("No image file provided for brand: {}", name);
                }

                BrandRequestDto requestDto = BrandRequestDto.builder()
                                .name(name.trim())
                                .nameEnglish(nameEnglish != null ? nameEnglish.trim() : null)
                                .build();
                BrandResponseDto brand = brandService.createBrand(requestDto, image);
                return ResponseEntity.status(HttpStatus.CREATED).body(brand);
        }

        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Update a brand", description = "Update an existing brand by its ID. All fields are optional - only provided fields will be updated.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Brand updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Brand not found"),
                        @ApiResponse(responseCode = "409", description = "Brand name already exists"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<BrandResponseDto> updateBrand(
                        @PathVariable Integer id,
                        @Parameter(description = "Brand name (optional)") @RequestParam(value = "name", required = false) String name,
                        @Parameter(description = "Brand name in English (optional)") @RequestParam(value = "nameEnglish", required = false) String nameEnglish,
                        @Parameter(description = "Brand image file (optional)") @RequestPart(value = "image", required = false) MultipartFile image)
                        throws IOException {
                // Validate image if provided
                if (image != null && !image.isEmpty()) {
                        validateImageFile(image);
                        log.info("Received image file for update: name={}, size={} bytes, contentType={}",
                                        image.getOriginalFilename(), image.getSize(), image.getContentType());
                }

                BrandRequestDto requestDto = BrandRequestDto.builder()
                                .name(name != null ? name.trim() : null)
                                .nameEnglish(nameEnglish != null ? nameEnglish.trim() : null)
                                .build();
                BrandResponseDto brand = brandService.updateBrand(id, requestDto, image);
                return ResponseEntity.ok(brand);
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete a brand", description = "Delete a brand by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Brand deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Brand not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<Void> deleteBrand(@PathVariable Integer id) {
                brandService.deleteBrand(id);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{id}/image")
        @Operation(summary = "Delete brand image", description = "Delete only the image of a brand from S3 storage without deleting the brand itself")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Brand image deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Brand not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<BrandResponseDto> deleteBrandImage(@PathVariable Integer id) {
                BrandResponseDto brand = brandService.deleteBrandImage(id);
                return ResponseEntity.ok(brand);
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
