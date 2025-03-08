-- src/main/resources/data.sql
-- Initial data for books table
INSERT INTO books (title, author, price, stock_quantity, image_url) VALUES
    ('Effective Java', 'Joshua Bloch', 45.99, 10, '/images/effective-java.jpg'),
    ('Clean Code', 'Robert C. Martin', 39.99, 11, '/images/clean-code.jpg'),
    ('The Pragmatic Programmer', 'Andrew Hunt', 42.50, 12, '/images/pragmatic-programmer.jpg'),
    ('Design Patterns', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 54.99, 13, '/images/design-patterns.jpg'),
    ('Eloquent JavaScript', 'Marijn Haverbeke', 31.99, 14, '/images/eloquent-javascript.jpg'),
    ('Domain-Driven Design', 'Eric Evans', 59.99, 15, '/images/domain-driven-design.jpg')
ON CONFLICT (title) DO NOTHING;

-- For later development: Switch to AWS S3 URLs for image storage
-- INSERT INTO books (title, author, price, stock_quantity, image_url) VALUES
--     ('Effective Java', 'Joshua Bloch', 45.99, 10, 'https://s3.amazonaws.com/my-bucket/effective-java.jpg'),
--     ('Clean Code', 'Robert C. Martin', 39.99, 11, 'https://s3.amazonaws.com/my-bucket/clean-code.jpg'),
--     ('The Pragmatic Programmer', 'Andrew Hunt', 42.50, 12, 'https://s3.amazonaws.com/my-bucket/pragmatic-programmer.jpg'),
--     ('Design Patterns', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 54.99, 13, 'https://s3.amazonaws.com/my-bucket/design-patterns.jpg'),
--     ('Eloquent JavaScript', 'Marijn Haverbeke', 31.99, 14, 'https://s3.amazonaws.com/my-bucket/eloquent-javascript.jpg'),
--     ('Domain-Driven Design', 'Eric Evans', 59.99, 15, 'https://s3.amazonaws.com/my-bucket/domain-driven-design.jpg')
-- ON CONFLICT (title) DO NOTHING;