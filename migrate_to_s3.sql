-- Migration script: Convert from BYTEA image storage to S3 key storage
-- Run this against your PostgreSQL database

-- Step 1: Add new image_s3_key columns
ALTER TABLE brands_schema.brands ADD COLUMN IF NOT EXISTS image_s3_key VARCHAR(500);
ALTER TABLE brands_schema.products ADD COLUMN IF NOT EXISTS image_s3_key VARCHAR(500);

-- Step 2: Drop old image columns (if they exist)
-- Note: This will delete any existing image data in the database
-- Make sure you have backed up any important images before running this
ALTER TABLE brands_schema.brands DROP COLUMN IF EXISTS image;
ALTER TABLE brands_schema.products DROP COLUMN IF EXISTS image;

-- Verify the changes
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_schema = 'brands_schema' 
AND table_name IN ('brands', 'products')
AND column_name IN ('image', 'image_s3_key')
ORDER BY table_name, column_name;

