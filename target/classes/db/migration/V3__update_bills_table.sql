-- Add a default value for the categoryId field in the bills table
ALTER TABLE bills MODIFY COLUMN category_id BIGINT NOT NULL DEFAULT 1;
 
-- Make sure the category_id column is not nullable
UPDATE bills SET category_id = 1 WHERE category_id IS NULL; 