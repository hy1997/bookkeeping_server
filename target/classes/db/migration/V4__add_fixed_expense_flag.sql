-- Add is_fixed column to bills table
ALTER TABLE bills ADD COLUMN is_fixed BOOLEAN DEFAULT FALSE;

-- Add index for faster filtering
CREATE INDEX idx_bills_is_fixed ON bills(is_fixed); 