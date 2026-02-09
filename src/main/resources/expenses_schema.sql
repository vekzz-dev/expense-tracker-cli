CREATE TABLE IF NOT EXISTS expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT NOT NULL,
    amount INTEGER NOT NULL,
    created_at TEXT NOT NULL, -- Cents
    updated_at TEXT NOT NULL -- ISO-8601
);