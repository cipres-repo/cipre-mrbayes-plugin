CREATE TABLE additional_xml_timestamp(
  document_urn VARCHAR(255) NOT NULL,
  g_user_id INTEGER NOT NULL,
  modified TIMESTAMP,
  PRIMARY KEY (document_urn, g_user_id),
  FOREIGN KEY (document_urn) REFERENCES annotated_document(urn) ON DELETE CASCADE,
  FOREIGN KEY (g_user_id) REFERENCES g_user(id) ON DELETE CASCADE
);