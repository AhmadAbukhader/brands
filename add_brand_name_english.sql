-- Migration script: Add nameEnglish field to brands table
-- Run this against your PostgreSQL database

-- Step 1: Add new name_english column to brands table
ALTER TABLE brands_schema.brands ADD COLUMN IF NOT EXISTS name_english VARCHAR(255);

-- Verify the changes
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_schema = 'brands_schema'
AND table_name = 'brands'
AND column_name = 'name_english'
ORDER BY column_name;

