-- Test Data for Phase 3 Customer Portal Testing
-- Insert sample products for browsing and shopping

USE bakeryecommerce;

-- Insert some bakery products
INSERT INTO Product (productName, productDescription, productBasePrice) VALUES
('Chocolate Croissant', 'Buttery croissant filled with rich dark chocolate', 3.99),
('Blueberry Muffin', 'Fresh baked muffin bursting with blueberries', 2.49),
('Sourdough Bread', 'Artisan sourdough loaf with crispy crust', 5.99),
('Apple Danish', 'Flaky pastry topped with cinnamon apples', 3.49),
('Cinnamon Roll', 'Warm cinnamon roll with cream cheese frosting', 4.29),
('Bagel - Plain', 'Fresh baked plain bagel', 1.99),
('Bagel - Everything', 'Bagel with sesame, poppy seeds, onion, and garlic', 2.29),
('Baguette', 'Traditional French baguette, crusty and golden', 4.49),
('Banana Bread', 'Moist banana bread with walnuts', 5.49),
('Chocolate Chip Cookie', 'Classic chocolate chip cookie, soft and chewy', 1.29),
('Carrot Cake Slice', 'Slice of moist carrot cake with cream cheese frosting', 4.99),
('Red Velvet Cupcake', 'Rich red velvet cupcake with cream cheese frosting', 3.99),
('Tiramisu', 'Italian classic dessert with espresso and mascarpone', 6.99),
('Eclair', 'French pastry filled with vanilla cream and chocolate glaze', 4.49),
('Macaron - Assorted', 'Pack of 6 assorted French macarons', 8.99),
('Pumpkin Pie Slice', 'Seasonal pumpkin pie with whipped cream', 4.49),
('Lemon Tart', 'Tangy lemon tart with buttery crust', 5.29),
('Butter Tart', 'Canadian classic butter tart', 2.99),
('Pecan Pie Slice', 'Rich pecan pie with caramel filling', 5.49),
('Cheesecake Slice - NY Style', 'Classic New York style cheesecake', 6.49);

-- Create some product tags/categories
INSERT INTO Tag (tagName) VALUES
('Pastries'),
('Bread'),
('Cookies'),
('Cakes'),
('Pies'),
('French'),
('Breakfast'),
('Desserts');

-- Tag the products (connect products to categories)
-- Get the product IDs and tag IDs, then create associations
INSERT INTO ProductTag (productId, tagId)
SELECT p.productId, t.tagId
FROM Product p, Tag t
WHERE
    (p.productName = 'Chocolate Croissant' AND t.tagName IN ('Pastries', 'French', 'Breakfast')) OR
    (p.productName = 'Blueberry Muffin' AND t.tagName IN ('Pastries', 'Breakfast')) OR
    (p.productName = 'Sourdough Bread' AND t.tagName = 'Bread') OR
    (p.productName = 'Apple Danish' AND t.tagName IN ('Pastries', 'Breakfast')) OR
    (p.productName = 'Cinnamon Roll' AND t.tagName IN ('Pastries', 'Breakfast', 'Desserts')) OR
    (p.productName LIKE 'Bagel%' AND t.tagName IN ('Bread', 'Breakfast')) OR
    (p.productName = 'Baguette' AND t.tagName IN ('Bread', 'French')) OR
    (p.productName = 'Banana Bread' AND t.tagName IN ('Bread', 'Breakfast')) OR
    (p.productName = 'Chocolate Chip Cookie' AND t.tagName IN ('Cookies', 'Desserts')) OR
    (p.productName LIKE '%Cake%' AND t.tagName IN ('Cakes', 'Desserts')) OR
    (p.productName LIKE '%Cupcake%' AND t.tagName IN ('Cakes', 'Desserts')) OR
    (p.productName = 'Tiramisu' AND t.tagName IN ('Desserts', 'French')) OR
    (p.productName = 'Eclair' AND t.tagName IN ('Pastries', 'French', 'Desserts')) OR
    (p.productName LIKE 'Macaron%' AND t.tagName IN ('Cookies', 'French', 'Desserts')) OR
    (p.productName LIKE '%Pie%' AND t.tagName IN ('Pies', 'Desserts')) OR
    (p.productName LIKE '%Tart%' AND t.tagName IN ('Pies', 'Desserts')) OR
    (p.productName LIKE 'Cheesecake%' AND t.tagName IN ('Cakes', 'Desserts'));

-- Create a test customer user if not exists
INSERT IGNORE INTO User (username, email, userPasswordHash, userRole)
VALUES ('customer1', 'customer@test.com', '$2a$10$YourHashedPasswordHere', 'CUSTOMER');

-- Note: For actual testing, you should use the registration feature or
-- use the PasswordHashGenerator to create a proper BCrypt hash

SELECT 'Test products inserted successfully!' as Status;
SELECT COUNT(*) as ProductCount FROM Product;
SELECT COUNT(*) as TagCount FROM Tag;
SELECT COUNT(*) as ProductTagCount FROM ProductTag;

