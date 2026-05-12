CREATE TABLE uploaded_object (
    content_sha256 BLOB NOT NULL PRIMARY KEY,
    object_key TEXT NOT NULL,
    size_bytes INTEGER NOT NULL,
    original_filename TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);
