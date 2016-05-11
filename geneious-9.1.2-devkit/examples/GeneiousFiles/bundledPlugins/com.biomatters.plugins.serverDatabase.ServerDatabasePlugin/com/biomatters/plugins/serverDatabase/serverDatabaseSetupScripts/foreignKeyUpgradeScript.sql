-- ===========================================
--   GENEIOUS 3.5.6 FOREIGN KEY UPGRADE SCRIPT
--
--   Author:            Matthew Cheung
--   Date:              2007-12-13
--
--   Copyright (C):     Biomatters Ltd
--
--
-- This script is used to add foreign keys with "ON CASCADE DELETE" actions to
-- the Geneious 3.5.1 Schema.  This is to comply with the newer Geneious 3.6.0
-- schema.  For PostgreSQL, Microsoft SQL Server and Oracle databases the old
-- foreign keys should be removed before running this script to avoid conflicts.
-- ===========================================

-- Alter g_user_group_role
ALTER TABLE g_user_group_role ADD FOREIGN KEY(g_user_id) REFERENCES g_user(id) ON DELETE CASCADE;
ALTER TABLE g_user_group_role ADD FOREIGN KEY (g_group_id) REFERENCES g_group(id) ON DELETE CASCADE;
ALTER TABLE g_user_group_role ADD FOREIGN KEY (g_role_id) REFERENCES g_role(id) ON DELETE CASCADE;

-- Alter hidden_folder_to_user
ALTER TABLE hidden_folder_to_user ADD FOREIGN KEY(hidden_folder_id) REFERENCES folder(id) ON DELETE CASCADE;
ALTER TABLE hidden_folder_to_user ADD FOREIGN KEY(user_id) REFERENCES g_user(id) ON DELETE CASCADE;

-- Alter special_element
ALTER TABLE special_element ADD FOREIGN KEY(folder_id) REFERENCES folder(id) ON DELETE CASCADE;

-- Alter annotated_document
ALTER TABLE annotated_document ADD FOREIGN KEY(folder_id)  REFERENCES folder(id) ON DELETE CASCADE;

-- Alter search field values
ALTER TABLE boolean_search_field_value ADD FOREIGN KEY(annotated_document_id) REFERENCES annotated_document(id) ON DELETE CASCADE;
ALTER TABLE boolean_search_field_value ADD FOREIGN KEY(search_field_code) REFERENCES search_field(code) ON DELETE CASCADE;
ALTER TABLE integer_search_field_value ADD FOREIGN KEY(annotated_document_id) REFERENCES annotated_document(id) ON DELETE CASCADE;
ALTER TABLE integer_search_field_value ADD FOREIGN KEY(search_field_code) REFERENCES search_field(code) ON DELETE CASCADE;
ALTER TABLE float_search_field_value ADD FOREIGN KEY(annotated_document_id) REFERENCES annotated_document(id) ON DELETE CASCADE;
ALTER TABLE float_search_field_value ADD FOREIGN KEY(search_field_code) REFERENCES search_field(code) ON DELETE CASCADE;
ALTER TABLE date_search_field_value ADD FOREIGN KEY(annotated_document_id) REFERENCES annotated_document(id) ON DELETE CASCADE;
ALTER TABLE date_search_field_value ADD FOREIGN KEY(search_field_code) REFERENCES search_field(code) ON DELETE CASCADE;
ALTER TABLE long_search_field_value ADD FOREIGN KEY(annotated_document_id) REFERENCES annotated_document(id) ON DELETE CASCADE;
ALTER TABLE long_search_field_value ADD FOREIGN KEY(search_field_code) REFERENCES search_field(code) ON DELETE CASCADE;
ALTER TABLE double_search_field_value ADD FOREIGN KEY(annotated_document_id) REFERENCES annotated_document(id) ON DELETE CASCADE;
ALTER TABLE double_search_field_value ADD FOREIGN KEY(search_field_code) REFERENCES search_field(code) ON DELETE CASCADE;
ALTER TABLE string_search_field_value ADD FOREIGN KEY(annotated_document_id) REFERENCES annotated_document(id) ON DELETE CASCADE;
ALTER TABLE string_search_field_value ADD FOREIGN KEY(search_field_code) REFERENCES search_field(code) ON DELETE CASCADE;

-- Alter document_read
ALTER TABLE document_read ADD FOREIGN KEY(annotated_document_id) REFERENCES annotated_document(id) ON DELETE CASCADE;
ALTER TABLE document_read ADD FOREIGN KEY(g_user_id) REFERENCES g_user(id) ON DELETE CASCADE;