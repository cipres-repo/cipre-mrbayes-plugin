-- =====================================
--   SERVER PLUGIN DATABASE DEFINITION
--
--   Author:            Bruce Ashton
--   Date:              2007-05-28
--
--   Last Modified By:  Matthew Cheung
--   Last Modified On:  2013-12-11
--
--   Copyright (C):     Biomatters Ltd
--
--
-- To set up a user "user" with access to this DB, do the following
-- (with "user" and "somepassword" replace with appropriate values):
-- CREATE USER user WITH PASSWORD 'somepassword';
-- GRANT SELECT, UPDATE, INSERT, DELETE ON document_read, boolean_search_field_value, integer_search_field_value, float_search_field_value, date_search_field_value, long_search_field_value, double_search_field_value, string_search_field_value, search_field, annotated_document, special_element, folder, g_user_group_role, g_user, g_role, g_group, next_table_id, metadata, hidden_folder_to_user, document_to_file_data, document_file_data, folder_view, additional_document_xml, additional_xml_timestamp, indexing_queue TO user;
-- =====================================

-- =========================================================================
--   POSTGRESQL NOTES
--
--   This script has been tested on PostgreSQL version 8.1.
-- =========================================================================


-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- +
-- +   The transaction isolation level must be set to at least
-- +   java.sql.Connection.TRANSACTION_READ_COMMITTED
-- +
-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


-- DROP TABLES --
DROP TABLE indexing_queue;
DROP TABLE metadata;
DROP TABLE document_read;
DROP TABLE boolean_search_field_value;
DROP TABLE integer_search_field_value;
DROP TABLE float_search_field_value;
DROP TABLE date_search_field_value;
DROP TABLE long_search_field_value;
DROP TABLE double_search_field_value;
DROP TABLE string_search_field_value;
DROP TABLE search_field;
DROP TABLE folder_view;
DROP TABLE document_to_file_data;
DROP TABLE document_file_data;
DROP TABLE additional_xml_timestamp;
DROP TABLE additional_document_xml;
DROP TABLE annotated_document;
DROP TABLE special_element;
DROP TABLE hidden_folder_to_user;
DROP TABLE folder;
DROP TABLE g_user_group_role;
DROP TABLE g_user;
DROP TABLE g_role;
DROP TABLE g_group;
DROP TABLE next_table_id;

-- END DROP TABLES --

-- META DATA --

CREATE TABLE metadata (
    identifier     VARCHAR(80)     PRIMARY KEY,
    value          VARCHAR(255)
);


-- USER / DATA G_USERS AND G_ROLES --

-- Each folder record will have a g_group as owner so that access rights can be
-- determined.  All other records will inherit ownership from the folder.  The
-- 'g_' prefix is to avoid name conflicts with system tables.  (e.g. in MySQL.)
CREATE TABLE g_group (
 id                      INTEGER        PRIMARY KEY,
 name                    VARCHAR(255)   NOT NULL
);


-- Defines the type of role / permissions as user can have within a group.
-- Currently the roles are 'Admin', 'Edit' and 'View'.
CREATE TABLE g_role (
    id                      INTEGER        PRIMARY KEY,
    name                    VARCHAR(255)   NOT NULL
);


-- The Geneious user.  Used to discover the groups, and hence the permissions,
-- that a user has in relation to records in the database.
-- Note: the primary_group_id column is unused as at 2007-12-14
CREATE TABLE g_user (
    id                      INTEGER        PRIMARY KEY,
    primary_group_id        INTEGER        NOT NULL   REFERENCES g_group(id),
    username                VARCHAR(255)   NOT NULL
);
CREATE INDEX GU_primary_group_id_FK ON g_user(primary_group_id);


-- The lookup table relating user to group and role.
CREATE TABLE g_user_group_role (
    g_user_id               INTEGER        NOT NULL,
    g_group_id              INTEGER        NOT NULL,
    g_role_id               INTEGER        NOT NULL,
    PRIMARY KEY (g_user_id, g_group_id, g_role_id),
    FOREIGN KEY (g_user_id) REFERENCES g_user(id) ON DELETE CASCADE,
    FOREIGN KEY (g_group_id) REFERENCES g_group(id) ON DELETE CASCADE,
    FOREIGN KEY (g_role_id) REFERENCES g_role(id) ON DELETE CASCADE
);
CREATE INDEX UGR_g_user_id_FK ON g_user_group_role(g_user_id);
CREATE INDEX UGR_g_group_id_FK ON g_user_group_role(g_group_id);
CREATE INDEX UGR_g_role_id_FK ON g_user_group_role(g_role_id);

-- END USER / DATA G_USER --


-- FOLDERS, DOCUMENTS AND SEARCH FIELDS --

-- Corresponds to a folder in the local database.  The visible column indicates
-- whether the folder is displayed in the service tree.  The reason why we are not
-- using cascading deletes with folders, is because SQL Server does not support
-- cyclic references with cascade actions.
CREATE TABLE folder (
 -- Watch out for circular references
    id                      INTEGER        PRIMARY KEY,
    g_group_id              INTEGER        NOT NULL  REFERENCES g_group(id),
    parent_folder_id        INTEGER        REFERENCES folder(id),
    visible                 BOOLEAN        NOT NULL,
    modified                TIMESTAMP      NOT NULL,
    name                    VARCHAR(255),
    CHECK (id != parent_folder_id)
);
CREATE INDEX F_parent_folder_id_FK ON folder(parent_folder_id);
CREATE INDEX F_name_FK ON folder(name);


CREATE TABLE hidden_folder_to_user (
    hidden_folder_id        INTEGER     PRIMARY KEY,
    user_id                 INTEGER,
    FOREIGN KEY(hidden_folder_id)       REFERENCES folder(id) ON DELETE CASCADE,
    FOREIGN KEY(user_id)                REFERENCES g_user(id) ON DELETE CASCADE
);
CREATE INDEX HFU_user_id_FK ON hidden_folder_to_user(user_id);
-- no need to index hidden_folder_id because it is the primary key and is therefore already indexed.


-- Any arbitrary special XML element.
CREATE TABLE special_element (
    folder_id               INTEGER        NOT NULL,
    modified                TIMESTAMP      NOT NULL,
    xml                     TEXT           NOT NULL,   -- Any arbitrary xml
    name                    VARCHAR(255),               -- Can find elements by name
    PRIMARY KEY(folder_id, name),
    FOREIGN KEY(folder_id)                 REFERENCES folder(id) ON DELETE CASCADE
);
CREATE INDEX SE_folder_id_FK ON special_element(folder_id);


-- A document.
CREATE TABLE annotated_document (
    id                      INTEGER        PRIMARY KEY,
    folder_id               INTEGER        NOT NULL,
    modified                TIMESTAMP      NOT NULL,
    urn                     VARCHAR(255)   NOT NULL   UNIQUE,
    document_xml            TEXT           NOT NULL,   -- The annotated document
    plugin_document_xml     TEXT           NOT NULL,   -- Underlying plugin document
    reference_count         INTEGER        NOT NULL,
    FOREIGN KEY(folder_id)  REFERENCES folder(id) ON DELETE CASCADE
);
CREATE INDEX AD_folder_id_FK ON annotated_document(folder_id);

-- A search field
CREATE TABLE search_field (
    code                    VARCHAR(255)     NOT NULL,
    field_xml               TEXT             NOT NULL,
    PRIMARY KEY(code)
);


-- Values of searchable document fields.  These fields duplicate information in the
-- document_xml column in annotated_document, but allow for server-side
-- searching.
CREATE TABLE boolean_search_field_value (
    id                      INTEGER        PRIMARY KEY,
    annotated_document_id   INTEGER        NOT NULL,
    search_field_code       VARCHAR(255)   NOT NULL,
    value                   BOOLEAN        NOT NULL,
    FOREIGN KEY(annotated_document_id)     REFERENCES annotated_document(id) ON DELETE CASCADE,
    FOREIGN KEY(search_field_code)         REFERENCES search_field(code) ON DELETE CASCADE
);
CREATE INDEX BSFV_annotated_document_id_FK ON boolean_search_field_value(annotated_document_id);
CREATE INDEX BSFV_search_field_code_FK ON boolean_search_field_value(search_field_code);

CREATE TABLE integer_search_field_value (
    id                      INTEGER        PRIMARY KEY,
    annotated_document_id   INTEGER        NOT NULL,
    search_field_code       VARCHAR(255)   NOT NULL,
    value                   INTEGER        NOT NULL,
    FOREIGN KEY(annotated_document_id)     REFERENCES annotated_document(id) ON DELETE CASCADE,
    FOREIGN KEY(search_field_code)         REFERENCES search_field(code) ON DELETE CASCADE
);
CREATE INDEX ISFV_annotated_document_id_FK ON integer_search_field_value(annotated_document_id);
CREATE INDEX ISFV_search_field_code_FK ON integer_search_field_value(search_field_code);

CREATE TABLE float_search_field_value (
    id                      INTEGER        PRIMARY KEY,
    annotated_document_id   INTEGER        NOT NULL,
    search_field_code       VARCHAR(255)   NOT NULL,
    value                   REAL           NOT NULL,
    FOREIGN KEY(annotated_document_id)     REFERENCES annotated_document(id) ON DELETE CASCADE,
    FOREIGN KEY(search_field_code)         REFERENCES search_field(code) ON DELETE CASCADE
);
CREATE INDEX FSFV_annotated_document_id_FK ON float_search_field_value(annotated_document_id);
CREATE INDEX FSFV_search_field_code_FK ON float_search_field_value(search_field_code);

CREATE TABLE date_search_field_value (
    id                      INTEGER        PRIMARY KEY,
    annotated_document_id   INTEGER        NOT NULL,
    search_field_code       VARCHAR(255)   NOT NULL,
    value                   DATE           NOT NULL,
    FOREIGN KEY(annotated_document_id)     REFERENCES annotated_document(id) ON DELETE CASCADE,
    FOREIGN KEY(search_field_code)         REFERENCES search_field(code) ON DELETE CASCADE
);
CREATE INDEX DtSFV_annotated_document_id_FK ON date_search_field_value(annotated_document_id);
CREATE INDEX DtSFV_search_field_code_FK ON date_search_field_value(search_field_code);


CREATE TABLE long_search_field_value (
    id                      INTEGER        PRIMARY KEY,
    annotated_document_id   INTEGER        NOT NULL,
    search_field_code       VARCHAR(255)   NOT NULL,
    value                   BIGINT         NOT NULL,
    FOREIGN KEY(annotated_document_id)     REFERENCES annotated_document(id) ON DELETE CASCADE,
    FOREIGN KEY(search_field_code)         REFERENCES search_field(code) ON DELETE CASCADE
);
CREATE INDEX LSFV_annotated_document_id_FK ON long_search_field_value(annotated_document_id);
CREATE INDEX LSFV_search_field_code_FK ON long_search_field_value(search_field_code);

CREATE TABLE double_search_field_value (
    id                      INTEGER        PRIMARY KEY,
    annotated_document_id   INTEGER        NOT NULL,
    search_field_code       VARCHAR(255)   NOT NULL,
    value                   FLOAT          NOT NULL,
    FOREIGN KEY(annotated_document_id)     REFERENCES annotated_document(id) ON DELETE CASCADE,
    FOREIGN KEY(search_field_code)         REFERENCES search_field(code) ON DELETE CASCADE
);
CREATE INDEX DSFV_annotated_document_id_FK ON double_search_field_value(annotated_document_id);
CREATE INDEX DSFV_search_field_code_FK ON double_search_field_value(search_field_code);

CREATE TABLE string_search_field_value (
    id                      INTEGER        PRIMARY KEY,
    annotated_document_id   INTEGER        NOT NULL,
    search_field_code       VARCHAR(255)   NOT NULL,
    value                   TEXT           NOT NULL,
    FOREIGN KEY(annotated_document_id)     REFERENCES annotated_document(id) ON DELETE CASCADE,
    FOREIGN KEY(search_field_code)         REFERENCES search_field(code) ON DELETE CASCADE
);
CREATE INDEX SSFV_annotated_document_id_FK ON string_search_field_value(annotated_document_id);
CREATE INDEX SSFV_search_field_code_FK ON string_search_field_value(search_field_code);

-- Records in this table indicate the document has been read by _this_ user.
CREATE TABLE document_read (
    g_user_id               INTEGER        NOT NULL,
    annotated_document_id   INTEGER        NOT NULL,
    FOREIGN KEY(annotated_document_id)     REFERENCES annotated_document(id) ON DELETE CASCADE,
    FOREIGN KEY(g_user_id)                 REFERENCES g_user(id) ON DELETE CASCADE,
    PRIMARY KEY (g_user_id, annotated_document_id)
);
CREATE INDEX DR_annotated_document_id_FK ON document_read(annotated_document_id);
CREATE INDEX DR_g_user_id_FK ON document_read(g_user_id);

CREATE TABLE additional_document_xml (
    document_urn VARCHAR(255) NOT NULL,
    element_key VARCHAR(255) NOT NULL,
    g_user_id INTEGER NOT NULL,
    xml_element TEXT NOT NULL,
    geneious_major_version_1 INTEGER DEFAULT 0 NOT NULL,
    geneious_major_version_2 INTEGER DEFAULT 0 NOT NULL,
    PRIMARY KEY(document_urn, element_key, g_user_id, geneious_major_version_1, geneious_major_version_2),
    FOREIGN KEY(document_urn) REFERENCES annotated_document(urn) ON DELETE CASCADE,
    FOREIGN KEY(g_user_id) REFERENCES g_user(id) ON DELETE CASCADE
);
CREATE INDEX ADX_g_user_id_FK ON additional_document_xml(g_user_id);
CREATE INDEX ADX_document_urn_FK ON additional_document_xml(document_urn);

CREATE TABLE additional_xml_timestamp(
  document_urn VARCHAR(255) NOT NULL,
  g_user_id INTEGER NOT NULL,
  modified TIMESTAMP,
  PRIMARY KEY (document_urn, g_user_id),
  FOREIGN KEY (document_urn) REFERENCES annotated_document(urn) ON DELETE CASCADE,
  FOREIGN KEY (g_user_id) REFERENCES g_user(id) ON DELETE CASCADE
);
CREATE INDEX AXT_g_user_id_FK ON additional_xml_timestamp(g_user_id);
CREATE INDEX AXT_document_urn_FK ON additional_xml_timestamp(document_urn);

CREATE TABLE document_file_data (
  id integer NOT NULL PRIMARY KEY,
  data oid,
  local_file_path TEXT,
  local_file_size BIGINT,
  last_needed TIMESTAMP
);

CREATE TABLE document_to_file_data (
  document_urn VARCHAR(255) NOT NULL,
  file_data_id INTEGER NOT NULL,
  PRIMARY KEY(document_urn, file_data_id),
  FOREIGN KEY (document_urn) REFERENCES annotated_document (urn) ON DELETE CASCADE,
  FOREIGN KEY(file_data_id) REFERENCES document_file_data(id) ON DELETE CASCADE
);
CREATE INDEX DTFD_document_urn_FK ON document_to_file_data(document_urn);
CREATE INDEX DTFD_file_data_id_FK ON document_to_file_data(file_data_id);

CREATE TABLE folder_view (
  folder_id integer NOT NULL,
  document_urn VARCHAR(255) NOT NULL,
  modified TIMESTAMP NOT NULL,
  PRIMARY KEY(folder_id, document_urn),
  FOREIGN KEY (folder_id) REFERENCES folder(id) ON DELETE CASCADE
);
CREATE INDEX FV_folder_id_FK ON folder_view(folder_id);

-- A list of all documents that require indexing
CREATE TABLE indexing_queue(
 document_id INTEGER NOT NULL PRIMARY KEY,
 g_user_id INTEGER,
 reserved TIMESTAMP DEFAULT NULL,
 FOREIGN KEY (document_id) REFERENCES annotated_document(id) ON DELETE CASCADE,
 FOREIGN KEY (g_user_id) REFERENCES g_user(id) ON DELETE SET NULL
);
CREATE INDEX IQ_g_user_id_FK ON indexing_queue(g_user_id);
-- END FOLDERS, DOCUMENTS AND SEARCH FIELDS --


CREATE TABLE next_table_id (
    table_name              VARCHAR(50)    PRIMARY KEY      NOT NULL,
    next_id                 INTEGER
);


-- INSERT STATIC DATA --

-- Define root folder
INSERT INTO g_group VALUES(1, 'Everybody');
INSERT INTO g_group VALUES(2, 'Hidden');
INSERT INTO folder(id, g_group_id, visible, modified, name) VALUES(1, 1, '1', CURRENT_TIMESTAMP, 'Server Documents');

-- Define the possible roles users can have in relation to groups.
INSERT INTO g_role VALUES (0, 'Admin');
INSERT INTO g_role VALUES (1, 'Edit');
INSERT INTO g_role VALUES (2, 'View');

-- Define the global user used to identify resources available to all users when a user id is required
INSERT INTO g_user VALUES(-1, 1, 'Global');

-- Define the version of Geneious this schema requires
INSERT INTO metadata VALUES ('minimum_version_of_geneious_required_to_read_this_schema', '5.6.0');

-- Define the version of Geneious that introduced this schema
INSERT INTO metadata VALUES ('version_of_geneious_that_introduced_this_schema', '7.1.0');

-- Define whether or not to store document file data on the server file system.  Necessary for large files
INSERT INTO metadata VALUES('store_document_file_data_on_server_file_system_rather_than_database', 'false');

-- Fill out the current ids.  If these values do not exist then it is possible for two or more clients to try and
-- insert them at the same time.  This is because we cannot lock a row that does not exist.  When this happens, all
-- but one will fail.  They will then retry after catching the exception.  Inserting these values is to avoid this
-- retry, prior to Geneious version 3.6.0 these values were inserted by Geneious instead of at setup.
INSERT INTO next_table_id VALUES ('folder', 1);
INSERT INTO next_table_id VALUES ('g_group', 2);
INSERT INTO next_table_id VALUES ('annotated_document', 0);
INSERT INTO next_table_id VALUES ('special_element', 0);
INSERT INTO next_table_id VALUES ('g_user', 0);
INSERT INTO next_table_id VALUES ('g_role', 0);
INSERT INTO next_table_id VALUES ('boolean_search_field_value', 0);
INSERT INTO next_table_id VALUES ('integer_search_field_value', 0);
INSERT INTO next_table_id VALUES ('float_search_field_value', 0);
INSERT INTO next_table_id VALUES ('date_search_field_value', 0);
INSERT INTO next_table_id VALUES ('long_search_field_value', 0);
INSERT INTO next_table_id VALUES ('double_search_field_value', 0);
INSERT INTO next_table_id VALUES ('string_search_field_value', 0);
INSERT INTO next_table_id VALUES ('additional_document_xml', 0);
INSERT INTO next_table_id VALUES ('document_file_data', 0);
-- END INSERT STATIC DATA --
