-- ======================================================================================================
--   GENEIOUS Shared Database SCHEMA 4.7.20 TO 4.8.0 UPGRADE SCRIPT (PostgreSQL)
--
--   Author:            Matthew Cheung
--   Date:              2009-09-17
--
--   Copyright (C):     Biomatters Ltd
--
-- ======================================================================================================
DROP TABLE document_file_data;
DROP TABLE folder_view;

CREATE TABLE document_file_data (
    id INTEGER NOT NULL PRIMARY KEY,
    document_urn VARCHAR(255) NOT NULL,
    data oid,
    FOREIGN KEY(document_urn) REFERENCES annotated_document(urn) ON DELETE CASCADE
);

CREATE TABLE folder_view (
  folder_id integer NOT NULL,
  document_urn VARCHAR(255) NOT NULL,
  modified TIMESTAMP NOT NULL,
  PRIMARY KEY(folder_id, document_urn),
  FOREIGN KEY (folder_id) REFERENCES folder(id) ON DELETE CASCADE
);