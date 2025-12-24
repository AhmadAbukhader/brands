-- Migration script: Add isNew and isHidden boolean fields to products table
-- Run this against your PostgreSQL database

-- Step 1: Add new boolean columns to products table
ALTER TABLE brands_schema.products ADD COLUMN IF NOT EXISTS is_new BOOLEAN;
ALTER TABLE brands_schema.products ADD COLUMN IF NOT EXISTS is_hidden BOOLEAN;

-- Step 2: Set default values for existing records - set to false for all null values
UPDATE brands_schema.products SET is_new = false WHERE is_new IS NULL;
UPDATE brands_schema.products SET is_hidden = false WHERE is_hidden IS NULL;

-- Verify the changes
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'brands_schema'
AND table_name = 'products'
AND column_name IN ('is_new', 'is_hidden')
ORDER BY column_name;

