-- ===================================================
--   GENEIOUS 3.5.6 FOREIGN KEY INDEX CREATION SCRIPT
--
--   Author:            Matthew Cheung
--   Date:              2007-12-13
--
--   Copyright (C):     Biomatters Ltd
--
--
-- This script is used to add indexes to foreign keys with cascading actions to
-- the Geneious 3.5.1 database.  This is to comply with the newer Geneious 3.6.0
-- schema.  It is expected that the Foreign Key Upgrade Script has been run
-- on the database to create the foreign keys.
-- ===================================================

CREATE INDEX UGR_g_user_id_FK ON g_user_group_role(g_user_id);
CREATE INDEX UGR_g_group_id_FK ON g_user_group_role(g_group_id);
CREATE INDEX UGR_g_role_id_FK ON g_user_group_role(g_role_id);

CREATE INDEX HFU_user_id_FK ON hidden_folder_to_user(user_id);
-- no need to index hidden_folder_id because it is the primary key and is therefore already indexed.

CREATE INDEX SE_folder_id_FK ON special_element(folder_id);

CREATE INDEX AD_folder_id_FK ON annotated_document(folder_id);

CREATE INDEX BSFV_annotated_document_id_FK ON boolean_search_field_value(annotated_document_id);
CREATE INDEX BSFV_search_field_code_FK ON boolean_search_field_value(search_field_code);

CREATE INDEX ISFV_annotated_document_id_FK ON integer_search_field_value(annotated_document_id);
CREATE INDEX ISFV_search_field_code_FK ON integer_search_field_value(search_field_code);

CREATE INDEX FSFV_annotated_document_id_FK ON float_search_field_value(annotated_document_id);
CREATE INDEX FSFV_search_field_code_FK ON float_search_field_value(search_field_code);

CREATE INDEX DtSFV_annotated_document_id_FK ON date_search_field_value(annotated_document_id);
CREATE INDEX DtSFV_search_field_code_FK ON date_search_field_value(search_field_code);

CREATE INDEX LSFV_annotated_document_id_FK ON long_search_field_value(annotated_document_id);
CREATE INDEX LSFV_search_field_code_FK ON long_search_field_value(search_field_code);

CREATE INDEX DSFV_annotated_document_id_FK ON double_search_field_value(annotated_document_id);
CREATE INDEX DSFV_search_field_code_FK ON double_search_field_value(search_field_code);

CREATE INDEX SSFV_annotated_document_id_FK ON string_search_field_value(annotated_document_id);
CREATE INDEX SSFV_search_field_code_FK ON string_search_field_value(search_field_code);

CREATE INDEX DR_annotated_document_id_FK ON document_read(annotated_document_id);
CREATE INDEX DR_g_user_id_FK ON document_read(g_user_id);