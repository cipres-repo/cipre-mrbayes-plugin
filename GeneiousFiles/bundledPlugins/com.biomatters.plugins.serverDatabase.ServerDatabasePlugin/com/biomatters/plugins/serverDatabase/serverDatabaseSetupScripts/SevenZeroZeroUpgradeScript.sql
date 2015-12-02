-- ======================================================================================================
--   GENEIOUS Shared Database SCHEMA 5.6.1 TO 7.0.0 UPGRADE SCRIPT
--
--   Author:            Matthew Cheung
--   Date:              2013-08-07
--
--   Copyright (C):     Biomatters Ltd
--
--   Note: If run on Microsoft SQL Server, TIMESTAMP will need to be changed to DATETIME.
--
-- ======================================================================================================

CREATE TABLE indexing_queue(
 document_id INTEGER NOT NULL PRIMARY KEY,
 g_user_id INTEGER,
 reserved TIMESTAMP DEFAULT NULL,
 FOREIGN KEY (document_id) REFERENCES annotated_document(id) ON DELETE CASCADE,
 FOREIGN KEY (g_user_id) REFERENCES g_user(id) ON DELETE SET NULL
);
