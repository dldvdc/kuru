CREATE TABLE uploaded_object (
    content_sha256 bytea NOT NULL PRIMARY KEY,
    object_key text NOT NULL,
    size_bytes bigint NOT NULL,
    original_filename text,
    created_at timestamptz NOT NULL DEFAULT now()
);
