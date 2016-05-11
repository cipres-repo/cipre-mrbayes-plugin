-- ======================================================================================================
--   GENEIOUS Shared Database SCHEMA 7.0.2 TO 7.0.6 UPGRADE SCRIPT
--
--   Author:            Matthew Cheung
--   Date:              2013-12-11
--
--   Copyright (C):     Biomatters Ltd
--
-- ======================================================================================================

CREATE INDEX F_name_FK ON folder(name);
