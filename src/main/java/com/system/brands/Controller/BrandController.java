package com.system.brands.Controller;

import com.system.brands.Dto.BrandRequestDto;
import com.system.brands.Dto.BrandResponseDto;
import com.system.brands.Model.Brand;
import com.system.brands.Service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "APIs for managing brands")
@SecurityRequirement(name = "bearerAuth")
public class BrandController {

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
            @Parameter(description = "Brand name", required = true) @RequestPart("name") String name,
            @Parameter(description = "Brand image file") @RequestPart(value = "image", required = false) MultipartFile image)
            throws IOException {
        BrandRequestDto requestDto = BrandRequestDto.builder()
                .name(name)
                .build();
        BrandResponseDto brand = brandService.createBrand(requestDto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(brand);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a brand", description = "Update an existing brand by its ID with optional new image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand updated successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found"),
            @ApiResponse(responseCode = "409", description = "Brand name already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<BrandResponseDto> updateBrand(
            @PathVariable Integer id,
            @Parameter(description = "Brand name", required = true) @RequestPart("name") String name,
            @Parameter(description = "Brand image file") @RequestPart(value = "image", required = false) MultipartFile image)
            throws IOException {
        BrandRequestDto requestDto = BrandRequestDto.builder()
                .name(name)
                .build();
        BrandResponseDto brand = brandService.updateBrand(id, requestDto, image);
        return ResponseEntity.ok(brand);
    }

    @GetMapping("/{id}/image")
    @Operation(summary = "Get brand image", description = "Retrieve the image for a specific brand")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found or no image available")
    })
    public ResponseEntity<byte[]> getBrandImage(@PathVariable Integer id) {
        Brand brand = brandService.getBrandEntityById(id);

        if (brand.getImage() == null || brand.getImage().length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Default to JPEG, you can enhance this
                .body(brand.getImage());
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
}
