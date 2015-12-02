-- ===================================================
--   GENEIOUS 3.8.0 CONVERT MEDIUMTEXT FIELDS TO LONGTEXT FIELDS
--
--   Author:            Matthew Cheung
--   Date:              2007-06-20
--
--   Copyright (C):     Biomatters Ltd
--
--
-- This script is used to convert MySQL fields of type MEDIUMTEXT as
-- defined in the Geneious 3.6.0 Shared Database Schema to LONGTEXT.
-- This was done because MEDIUMTEXT provded inadequate for storing
-- genome sized documents.
-- ===================================================

ALTER TABLE special_element MODIFY COLUMN xml LONGTEXT;
ALTER TABLE annotated_document MODIFY COLUMN document_xml LONGTEXT;
ALTER TABLE string_search_field_value MODIFY COLUMN value LONGTEXT;
ALTER TABLE search_field MODIFY COLUMN field_xml LONGTEXT;