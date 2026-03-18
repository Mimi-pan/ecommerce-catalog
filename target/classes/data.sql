-- =============================================
-- Seed Categories
-- =============================================
INSERT INTO categories (name, description) VALUES
  ('Electronics', 'Gadgets, devices, and electronic accessories'),
  ('Clothing', 'Apparel, shoes, and fashion accessories'),
  ('Books', 'Physical and digital books across all genres'),
  ('Home & Garden', 'Furniture, decor, and gardening supplies'),
  ('Sports', 'Equipment and apparel for sports and fitness');

-- =============================================
-- Seed Products
-- =============================================
INSERT INTO products (name, description, price, stock_quantity, sku, image_url, active, category_id, created_at, updated_at) VALUES
  ('Wireless Bluetooth Headphones', 'Over-ear noise-cancelling headphones with 30h battery life', 89.99, 50, 'ELEC-001', 'https://example.com/images/headphones.jpg', true, 1, NOW(), NOW()),
  ('Mechanical Keyboard', 'TKL mechanical keyboard with blue switches and RGB lighting', 129.99, 30, 'ELEC-002', 'https://example.com/images/keyboard.jpg', true, 1, NOW(), NOW()),
  ('USB-C Hub 7-in-1', 'Multiport adapter with HDMI, USB 3.0, SD card, and PD charging', 49.99, 100, 'ELEC-003', 'https://example.com/images/hub.jpg', true, 1, NOW(), NOW()),

  ('Classic White T-Shirt', '100% organic cotton unisex t-shirt, available in all sizes', 24.99, 200, 'CLTH-001', 'https://example.com/images/tshirt.jpg', true, 2, NOW(), NOW()),
  ('Running Shoes', 'Lightweight breathable mesh running shoes with cushioned sole', 79.99, 75, 'CLTH-002', 'https://example.com/images/shoes.jpg', true, 2, NOW(), NOW()),

  ('Clean Code by Robert Martin', 'A handbook of agile software craftsmanship — essential for every developer', 39.99, 60, 'BOOK-001', 'https://example.com/images/clean-code.jpg', true, 3, NOW(), NOW()),
  ('The Pragmatic Programmer', '20th anniversary edition, fully revised and updated', 44.99, 45, 'BOOK-002', 'https://example.com/images/pragmatic.jpg', true, 3, NOW(), NOW()),

  ('Ergonomic Office Chair', 'Adjustable lumbar support, mesh back, and armrests', 349.99, 20, 'HOME-001', 'https://example.com/images/chair.jpg', true, 4, NOW(), NOW()),
  ('Succulent Plant Set', 'Set of 6 small succulents in decorative ceramic pots', 34.99, 80, 'HOME-002', 'https://example.com/images/succulents.jpg', true, 4, NOW(), NOW()),

  ('Yoga Mat', 'Non-slip 6mm thick eco-friendly yoga mat with carrying strap', 29.99, 120, 'SPRT-001', 'https://example.com/images/yoga.jpg', true, 5, NOW(), NOW()),
  ('Resistance Band Set', 'Set of 5 resistance bands with varying tension levels', 19.99, 150, 'SPRT-002', 'https://example.com/images/bands.jpg', true, 5, NOW(), NOW());
