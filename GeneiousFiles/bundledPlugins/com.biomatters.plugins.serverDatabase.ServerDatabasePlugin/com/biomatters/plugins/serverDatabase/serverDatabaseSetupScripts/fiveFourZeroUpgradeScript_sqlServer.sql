-- ======================================================================================================
--   GENEIOUS Shared Database SCHEMA 4.8.0 TO 5.3.999 UPGRADE SCRIPT (SQL Server)
--
--   Geneious will need to migrate data from the old version to the new version in order to be 5.4.0 compatible
--
--   Author:            Matthew Cheung
--   Date:              2011-05-05
--
--   Copyright (C):     Biomatters Ltd
--
-- ======================================================================================================

CREATE TABLE document_to_file_data (
  document_urn VARCHAR(255) NOT NULL,
  file_data_id INTEGER NOT NULL,
  PRIMARY KEY(document_urn, file_data_id),
  FOREIGN KEY (document_urn) REFERENCES annotated_document (urn) ON DELETE CASCADE,
  FOREIGN KEY(file_data_id) REFERENCES document_file_data(id) ON DELETE CASCADE
);

ALTER TABLE document_file_data ADD local_file_path TEXT;
ALTER TABLE document_file_data ADD local_file_size INTEGER;
ALTER TABLE document_file_data ADD last_needed DATETIME;

UPDATE metadata SET value = '5.3.999' WHERE identifier = 'version_of_geneious_that_introduced_this_schema';
INSERT INTO metadata VALUES('store_document_file_data_on_server_file_system_rather_than_database', 'false');
