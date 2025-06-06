-- First, delete any notifications with null related_entity_id
DELETE FROM notifications WHERE related_entity_id IS NULL;

-- Then alter the column to be NOT NULL
ALTER TABLE notifications ALTER COLUMN related_entity_id SET NOT NULL; 