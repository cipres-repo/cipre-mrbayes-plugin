-- ======================================================================================================
--   GENEIOUS Shared Database SCHEMA 3.8.0 TO 4.7.20 UPGRADE SCRIPT (PostgreSQL and Microsoft SQL Server)
--
--   Author:            Matthew Cheung
--   Date:              2009-07-29
--
--   Copyright (C):     Biomatters Ltd
--
-- ======================================================================================================
DROP TABLE IF EXISTS additional_document_xml;
CREATE TABLE additional_document_xml(
    document_urn VARCHAR(255) NOT NULL,
    element_key VARCHAR(255) NOT NULL,
    g_user_id INTEGER NOT NULL,
    xml_element LONGTEXT NOT NULL,
    PRIMARY KEY(document_urn, element_key, g_user_id),
    FOREIGN KEY(document_urn) REFERENCES annotated_document(urn) ON DELETE CASCADE,
    FOREIGN KEY(g_user_id) REFERENCES g_user(id) ON DELETE CASCADE
) TYPE INNODB;
INSERT INTO g_user VALUES(-1, 1, 'Global');