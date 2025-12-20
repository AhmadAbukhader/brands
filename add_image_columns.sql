-- Migration script to add image columns to brands and products tables
-- Run this script in your PostgreSQL database

-- Add image column to brands table
ALTER TABLE brands_schema.brands 
ADD COLUMN IF NOT EXISTS image BYTEA;

-- Add image column to products table
ALTER TABLE brands_schema.products 
ADD COLUMN IF NOT EXISTS image BYTEA;

-- Optional: Add comments to document the columns
COMMENT ON COLUMN brands_schema.brands.image IS 'Brand logo/image stored as binary data (BYTEA)';
COMMENT ON COLUMN brands_schema.products.image IS 'Product image stored as binary data (BYTEA)';

