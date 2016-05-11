-- ======================================================================================================
--   GENEIOUS Shared Database SCHEMA 7.0.0 TO 7.0.2 UPGRADE SCRIPT
--
--   Author:            Matthew Cheung
--   Date:              2013-10-03
--
--   Copyright (C):     Biomatters Ltd
--
--   This script is unnecessary on MySQL databases
--
-- ======================================================================================================

CREATE INDEX GU_primary_group_id_FK ON g_user(primary_group_id);
CREATE INDEX F_parent_folder_id_FK ON folder(parent_folder_id);
CREATE INDEX ADX_document_urn_FK ON additional_document_xml(document_urn);
CREATE INDEX AXT_g_user_id_FK ON additional_xml_timestamp(g_user_id);
CREATE INDEX AXT_document_urn_FK ON additional_xml_timestamp(document_urn);
CREATE INDEX DTFD_document_urn_FK ON document_to_file_data(document_urn);
CREATE INDEX DTFD_file_data_id_FK ON document_to_file_data(file_data_id);
CREATE INDEX FV_folder_id_FK ON folder_view(folder_id);
CREATE INDEX IQ_g_user_id_FK ON indexing_queue(g_user_id);
