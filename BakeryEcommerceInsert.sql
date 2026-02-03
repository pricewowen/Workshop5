-- INSERT dummy data into BakeryEcommerce database.

USE BakeryEcommerce;
GO


-- TABLE: Address
INSERT INTO Address (addressLine1, addressLine2, addressCity, addressProvince, addressPostalCode)
VALUES
('1208 4 Ave SW', 'Suite 210', 'Calgary', 'AB', 'T2P 0H3'),
('33 10 St NW', NULL, 'Calgary', 'AB', 'T2N 1V4'),
('455 7 Ave SE', NULL, 'Calgary', 'AB', 'T2G 0J8'),
('9805 12 Ave SW', NULL, 'Calgary', 'AB', 'T2W 1K1'),
('2100 16 Ave NW', 'Unit 14', 'Calgary', 'AB', 'T2M 0M5'),
('8715 Macleod Trail SE', 'Unit 120', 'Calgary', 'AB', 'T2H 0M3'),
('101 9 Ave SW', 'Floor 6', 'Calgary', 'AB', 'T2P 1J9'),
('560 2 St SW', NULL, 'Calgary', 'AB', 'T2P 0S6'),
('1180 7 St SW', NULL, 'Calgary', 'AB', 'T2R 1A5'),
('4020 4 St NW', NULL, 'Calgary', 'AB', 'T2K 1A2'),
('12 100 St NW', NULL, 'Edmonton', 'AB', 'T5J 1L6'),
('815 104 Ave NW', 'Unit 5', 'Edmonton', 'AB', 'T5H 0L1'),
('10425 Jasper Ave', 'Suite 300', 'Edmonton', 'AB', 'T5J 1Z7'),
('8900 99 St NW', NULL, 'Edmonton', 'AB', 'T6E 3T9'),
('150 109 St NW', 'Unit 2', 'Edmonton', 'AB', 'T5J 2X6'),
('200 Granville St', 'Unit 110', 'Vancouver', 'AB', 'V6C 1S4'),
('845 Burrard St', NULL, 'Vancouver', 'AB', 'V6Z 2K6'),
('1155 W Georgia St', 'Suite 900', 'Vancouver', 'AB', 'V6E 4T6'),
('777 Hornby St', NULL, 'Vancouver', 'AB', 'V6Z 1S4'),
('10 King St W', 'Floor 12', 'Toronto', 'AB', 'M5H 1A1'),
('220 Bloor St W', 'Unit 7', 'Toronto', 'AB', 'M5S 1T8'),
('30 Wellington St W', NULL, 'Toronto', 'AB', 'M5L 1E2'),
('1555 Rue Sainte-Catherine O', 'Suite 400', 'Montréal', 'AB', 'H3G 1P2'),
('1000 Rue De La Gauchetière O', NULL, 'Montréal', 'AB', 'H3B 4W5'),
('300 Prince of Wales Dr', 'Unit 18', 'Ottawa', 'AB', 'K2C 3T2'),
('99 Bank St', 'Suite 500', 'Ottawa', 'AB', 'K1P 5N2'),
('75 Queen St', NULL, 'Ottawa', 'AB', 'K1P 1N2'),
('2500 5 Ave NE', NULL, 'Calgary', 'AB', 'T2A 6K6'),
('4400 4 Ave SE', NULL, 'Calgary', 'AB', 'T2G 4X3'),
('601 12 Ave SW', 'Unit 3', 'Calgary', 'AB', 'T2R 1H7'),
('920 17 Ave SW', NULL, 'Calgary', 'AB', 'T2T 0A8'),
('350 8 Ave SE', NULL, 'Calgary', 'AB', 'T2G 0K6'),
('65 97 St NW', NULL, 'Edmonton', 'AB', 'T5K 1L5'),
('730 7 Ave SW', 'Suite 240', 'Calgary', 'AB', 'T2P 0Z9'),
('888 3 St SW', 'Unit 105', 'Calgary', 'AB', 'T2P 5C5'),
('145 5 Ave SE', NULL, 'Calgary', 'AB', 'T2G 2X1'),
('940 6 Ave SW', 'Suite 180', 'Calgary', 'AB', 'T2P 3T1'),
('222 4 Ave SE', NULL, 'Calgary', 'AB', 'T2G 4X7'),
('1300 1 St SE', 'Unit 12', 'Calgary', 'AB', 'T2G 0G8'),
('410 10 St NW', NULL, 'Calgary', 'AB', 'T2N 1V7'),
('5800 2 St SW', 'Unit 8', 'Calgary', 'AB', 'T2H 0H2');
GO


-- TABLE: [User]
INSERT INTO [User] (userUsername, userEmail, userPasswordHash, userRole, userCreatedAt)
VALUES
('alicia.nguyen', 'alicia.nguyen@northharbourmail.ca', 'HASHEDPW_001', 'Admin', DATEADD(day, -120, GETDATE())),
('mason.clark', 'mason.clark@northharbourmail.ca', 'HASHEDPW_002', 'Employee', DATEADD(day, -95, GETDATE())),
('sophia.patel', 'sophia.patel@northharbourmail.ca', 'HASHEDPW_003', 'Employee', DATEADD(day, -90, GETDATE())),
('ethan.wright', 'ethan.wright@northharbourmail.ca', 'HASHEDPW_004', 'Employee', DATEADD(day, -82, GETDATE())),
('isabella.chen', 'isabella.chen@northharbourmail.ca', 'HASHEDPW_005', 'Employee', DATEADD(day, -80, GETDATE())),
('noah.martin', 'noah.martin@northharbourmail.ca', 'HASHEDPW_006', 'Employee', DATEADD(day, -76, GETDATE())),
('ava.roberts', 'ava.roberts@northharbourmail.ca', 'HASHEDPW_007', 'Employee', DATEADD(day, -70, GETDATE())),
('logan.scott', 'logan.scott@northharbourmail.ca', 'HASHEDPW_008', 'Employee', DATEADD(day, -66, GETDATE())),
('mia.kim', 'mia.kim@northharbourmail.ca', 'HASHEDPW_009', 'Employee', DATEADD(day, -62, GETDATE())),
('jackson.hall', 'jackson.hall@northharbourmail.ca', 'HASHEDPW_010', 'Employee', DATEADD(day, -60, GETDATE())),
('olivia.brown', 'olivia.brown@northharbourmail.ca', 'HASHEDPW_011', 'Customer', DATEADD(day, -58, GETDATE())),
('liam.thompson', 'liam.thompson@northharbourmail.ca', 'HASHEDPW_012', 'Customer', DATEADD(day, -55, GETDATE())),
('emma.wilson', 'emma.wilson@northharbourmail.ca', 'HASHEDPW_013', 'Customer', DATEADD(day, -50, GETDATE())),
('benjamin.lee', 'benjamin.lee@northharbourmail.ca', 'HASHEDPW_014', 'Customer', DATEADD(day, -45, GETDATE())),
('amelia.johnson', 'amelia.johnson@northharbourmail.ca', 'HASHEDPW_015', 'Customer', DATEADD(day, -42, GETDATE())),
('lucas.anderson', 'lucas.anderson@northharbourmail.ca', 'HASHEDPW_016', 'Customer', DATEADD(day, -40, GETDATE())),
('charlotte.miller', 'charlotte.miller@northharbourmail.ca', 'HASHEDPW_017', 'Customer', DATEADD(day, -38, GETDATE())),
('henry.davis', 'henry.davis@northharbourmail.ca', 'HASHEDPW_018', 'Customer', DATEADD(day, -35, GETDATE())),
('evelyn.moore', 'evelyn.moore@northharbourmail.ca', 'HASHEDPW_019', 'Customer', DATEADD(day, -30, GETDATE())),
('daniel.taylor', 'daniel.taylor@northharbourmail.ca', 'HASHEDPW_020', 'Customer', DATEADD(day, -28, GETDATE())),
('harper.jackson', 'harper.jackson@northharbourmail.ca', 'HASHEDPW_021', 'Customer', DATEADD(day, -24, GETDATE())),
('sebastian.white', 'sebastian.white@northharbourmail.ca', 'HASHEDPW_022', 'Customer', DATEADD(day, -22, GETDATE())),
('nora.harris', 'nora.harris@northharbourmail.ca', 'HASHEDPW_023', 'Customer', DATEADD(day, -20, GETDATE())),
('wyatt.martinez', 'wyatt.martinez@northharbourmail.ca', 'HASHEDPW_024', 'Customer', DATEADD(day, -18, GETDATE()));
GO


-- TABLE: RewardTier
INSERT INTO RewardTier (rewardTierName, rewardTierMinPoints, rewardTierMaxPoints, rewardTierDiscountRate)
VALUES
('Bronze', 0, 99999, 0.00),
('Silver', 100000, 249999, 5.00),
('Gold', 250000, 499999, 10.00),
('Platinum', 500000, NULL, 15.00);
GO


-- TABLE: Bakery
INSERT INTO Bakery (addressId, bakeryName, bakeryPhone, bakeryEmail)
VALUES
(1, 'North Harbour Bakery - Downtown', '(403) 555-2101', 'downtown@northharbourbakery.ca'),
(11, 'North Harbour Bakery - Edmonton Central', '(780) 555-4302', 'edmonton@northharbourbakery.ca'),
(20, 'North Harbour Bakery - Toronto Financial', '(416) 555-9012', 'toronto@northharbourbakery.ca');
GO


-- TABLE: BakeryHours
INSERT INTO BakeryHours (bakeryId, dayOfWeek, openTime, closeTime, isClosed)
VALUES
(1, 1, '07:30', '18:00', 0),
(1, 2, '07:30', '18:00', 0),
(1, 3, '07:30', '18:00', 0),
(1, 4, '07:30', '18:00', 0),
(1, 5, '07:30', '18:00', 0),
(1, 6, '08:30', '16:30', 0),
(1, 7, NULL, NULL, 1),

(2, 1, '08:00', '17:30', 0),
(2, 2, '08:00', '17:30', 0),
(2, 3, '08:00', '17:30', 0),
(2, 4, '08:00', '17:30', 0),
(2, 5, '08:00', '17:30', 0),
(2, 6, '09:00', '16:00', 0),
(2, 7, NULL, NULL, 1),

(3, 1, '07:00', '18:30', 0),
(3, 2, '07:00', '18:30', 0),
(3, 3, '07:00', '18:30', 0),
(3, 4, '07:00', '18:30', 0),
(3, 5, '07:00', '18:30', 0),
(3, 6, '08:00', '17:00', 0),
(3, 7, '09:00', '14:00', 0);
GO


-- TABLE: Supplier
INSERT INTO Supplier (addressId, supplierName, supplierPhone, supplierEmail)
VALUES
(29, 'Prairie Wholesale Ingredients', '(403) 555-7001', 'orders@prairiewholesale.ca'),
(30, 'Summit Packaging Supply', '(403) 555-7002', 'support@summitpackaging.ca'),
(33, 'Riverbend Dairy Co.', '(780) 555-7003', 'sales@riverbenddairy.ca'),
(16, 'Coastal Produce Distributors', '(604) 555-7004', 'info@coastalproduce.ca'),
(24, 'St. Lawrence Dry Goods', '(514) 555-7005', 'service@stlawrencedrygoods.ca');
GO


-- TABLE: Tag
INSERT INTO Tag (tagName)
VALUES
('Bread'),
('Cake'),
('Pastry'),
('Cookie'),
('Gluten-Free'),
('Dairy-Free'),
('Seasonal'),
('Vegan'),
('Breakfast'),
('Dessert'),
('Nut-Free'),
('Whole Grain');
GO


-- TABLE: Product
INSERT INTO Product (productName, productDescription, productBasePrice)
VALUES
('Sourdough Loaf', 'Naturally leavened sourdough bread', 6.49),
('Multigrain Sandwich Bread', 'Whole grain sandwich loaf', 5.99),
('Baguette', 'Classic French-style baguette', 3.49),
('Cinnamon Roll', 'Soft roll with cinnamon filling and glaze', 4.25),
('Butter Croissant', 'Flaky butter croissant', 3.95),
('Blueberry Muffin', 'Muffin with blueberries', 3.25),
('Banana Bread Slice', 'Moist banana bread slice', 2.95),
('Chocolate Chip Cookie', 'Cookie with chocolate chips', 2.25),
('Oatmeal Raisin Cookie', 'Oatmeal cookie with raisins', 2.25),
('Vanilla Cupcake', 'Vanilla cupcake with buttercream', 3.50),
('Chocolate Cupcake', 'Chocolate cupcake with buttercream', 3.50),
('Carrot Cake Slice', 'Carrot cake slice with cream cheese icing', 6.95),
('Chocolate Layer Cake', 'Chocolate cake with ganache', 29.99),
('Cheesecake Slice', 'Classic cheesecake slice', 7.25),
('Apple Turnover', 'Puff pastry turnover with apple filling', 4.10),
('Spinach Feta Danish', 'Danish pastry with spinach and feta', 4.75),
('Lemon Tart', 'Tart with lemon curd filling', 6.50),
('Brownie', 'Fudgy chocolate brownie', 3.75),
('Vegan Chocolate Brownie', 'Dairy-free brownie', 4.25),
('Gluten-Free Banana Muffin', 'Gluten-free banana muffin', 3.95),
('Seasonal Pumpkin Muffin', 'Pumpkin spice muffin', 3.75),
('Strawberry Shortcake Cup', 'Layered shortcake with strawberries', 6.95),
('Almond Biscotti', 'Twice-baked almond biscotti', 2.75),
('Whole Wheat Scone', 'Scone made with whole wheat flour', 3.25),
('Raspberry Danish', 'Danish pastry with raspberry filling', 4.75),
('Chocolate Eclair', 'Choux pastry with cream and chocolate topping', 5.25);
GO


-- TABLE: ProductTag
INSERT INTO ProductTag (productId, tagId)
VALUES
(1, 1),
(2, 1),
(2, 12),
(3, 1),
(4, 3),
(4, 9),
(5, 3),
(5, 9),
(6, 9),
(6, 3),
(7, 9),
(7, 3),
(8, 4),
(8, 10),
(9, 4),
(9, 10),
(10, 2),
(10, 10),
(11, 2),
(11, 10),
(12, 2),
(12, 10),
(13, 2),
(13, 10),
(14, 2),
(14, 10),
(15, 3),
(15, 10),
(16, 3),
(17, 10),
(18, 10),
(19, 8),
(19, 6),
(20, 5),
(21, 7),
(22, 10),
(23, 4),
(24, 12),
(25, 3),
(26, 3);
GO


-- TABLE: Employee
INSERT INTO Employee (
    userId, addressId,
    employeeFirstName, employeeMiddleInitial, employeeLastName,
    employeeRole, employeePhone, employeeBusinessPhone, employeeEmail
)
VALUES
(2, 2, 'Mason', NULL, 'Clark', 'Baker', '(403) 555-3101', '(403) 555-4101', 'mason.clark@northharbourbakery.ca'),
(3, 3, 'Sophia', 'R', 'Patel', 'Baker', '(403) 555-3102', '(403) 555-4102', 'sophia.patel@northharbourbakery.ca'),
(4, 4, 'Ethan', NULL, 'Wright', 'Shift Lead', '(403) 555-3103', '(403) 555-4103', 'ethan.wright@northharbourbakery.ca'),
(5, 5, 'Isabella', 'M', 'Chen', 'Baker', '(403) 555-3104', '(403) 555-4104', 'isabella.chen@northharbourbakery.ca'),
(6, 6, 'Noah', NULL, 'Martin', 'Baker', '(403) 555-3105', '(403) 555-4105', 'noah.martin@northharbourbakery.ca'),
(7, 7, 'Ava', NULL, 'Roberts', 'Customer Support', '(403) 555-3106', '(403) 555-4106', 'ava.roberts@northharbourbakery.ca'),
(8, 8, 'Logan', 'J', 'Scott', 'Quality Control', '(403) 555-3107', '(403) 555-4107', 'logan.scott@northharbourbakery.ca'),
(9, 9, 'Mia', NULL, 'Kim', 'Baker', '(403) 555-3108', '(403) 555-4108', 'mia.kim@northharbourbakery.ca'),
(10, 10, 'Jackson', NULL, 'Hall', 'Baker', '(403) 555-3109', '(403) 555-4109', 'jackson.hall@northharbourbakery.ca');
GO


-- TABLE: Customer
INSERT INTO Customer (
    userId, addressId, rewardTierId,
    customerFirstName, customerMiddleInitial, customerLastName,
    customerRole, customerPhone, customerBusinessPhone,
    customerRewardBalance, customerTierAssignedDate, customerEmail
)
VALUES
(11, 21, 1, 'Olivia', NULL, 'Brown', 'Customer', '(416) 555-1201', NULL, 120000, DATEADD(day, -30, GETDATE()), 'olivia.brown@northharbourmail.ca'),
(12, 22, 1, 'Liam', NULL, 'Thompson', 'Customer', '(416) 555-1202', NULL, 240000, DATEADD(day, -28, GETDATE()), 'liam.thompson@northharbourmail.ca'),
(13, 23, 2, 'Emma', 'J', 'Wilson', 'Customer', '(514) 555-1203', NULL, 520000, DATEADD(day, -26, GETDATE()), 'emma.wilson@northharbourmail.ca'),
(14, 25, 1, 'Benjamin', NULL, 'Lee', 'Customer', '(613) 555-1204', NULL, 80000, DATEADD(day, -24, GETDATE()), 'benjamin.lee@northharbourmail.ca'),
(15, 26, 2, 'Amelia', NULL, 'Johnson', 'Customer', '(613) 555-1205', NULL, 740000, DATEADD(day, -22, GETDATE()), 'amelia.johnson@northharbourmail.ca'),
(16, 27, 1, 'Lucas', 'A', 'Anderson', 'Customer', '(613) 555-1206', NULL, 60000, DATEADD(day, -20, GETDATE()), 'lucas.anderson@northharbourmail.ca'),
(17, 31, 1, 'Charlotte', NULL, 'Miller', 'Customer', '(403) 555-1207', NULL, 210000, DATEADD(day, -18, GETDATE()), 'charlotte.miller@northharbourmail.ca'),
(18, 32, 3, 'Henry', NULL, 'Davis', 'Customer', '(403) 555-1208', NULL, 1120000, DATEADD(day, -16, GETDATE()), 'henry.davis@northharbourmail.ca'),
(19, 34, 2, 'Evelyn', NULL, 'Moore', 'Customer', '(403) 555-1209', NULL, 680000, DATEADD(day, -14, GETDATE()), 'evelyn.moore@northharbourmail.ca'),
(20, 35, 1, 'Daniel', NULL, 'Taylor', 'Customer', '(403) 555-1210', NULL, 140000, DATEADD(day, -12, GETDATE()), 'daniel.taylor@northharbourmail.ca'),
(21, 36, 2, 'Harper', NULL, 'Jackson', 'Customer', '(403) 555-1211', NULL, 810000, DATEADD(day, -10, GETDATE()), 'harper.jackson@northharbourmail.ca'),
(22, 37, 1, 'Sebastian', NULL, 'White', 'Customer', '(403) 555-1212', NULL, 95000, DATEADD(day, -9, GETDATE()), 'sebastian.white@northharbourmail.ca'),
(23, 38, 1, 'Nora', NULL, 'Harris', 'Customer', '(403) 555-1213', NULL, 260000, DATEADD(day, -8, GETDATE()), 'nora.harris@northharbourmail.ca'),
(24, 39, 1, 'Wyatt', NULL, 'Martinez', 'Customer', '(403) 555-1214', NULL, 180000, DATEADD(day, -7, GETDATE()), 'wyatt.martinez@northharbourmail.ca');
GO


-- TABLE: Inventory
INSERT INTO Inventory (
    bakeryId, supplierId,
    inventoryItemName, inventoryItemType,
    inventoryQuantityOnHand, inventoryUnitOfMeasure
)
VALUES
(1, 1, 'All-purpose flour', 'Ingredient', 450.000, 'kg'),
(1, 1, 'Granulated sugar', 'Ingredient', 220.000, 'kg'),
(1, 3, 'Unsalted butter', 'Ingredient', 180.000, 'kg'),
(1, 3, 'Whole milk', 'Ingredient', 600.000, 'L'),
(1, 4, 'Fresh lemons', 'Ingredient', 95.000, 'kg'),
(1, 2, 'Bakery boxes (10 inch)', 'Packaging', 800.000, 'count'),
(1, 2, 'Pastry bags', 'Packaging', 1200.000, 'count'),
(1, 5, 'Baking cocoa', 'Ingredient', 80.000, 'kg'),

(2, 1, 'All-purpose flour', 'Ingredient', 380.000, 'kg'),
(2, 1, 'Granulated sugar', 'Ingredient', 210.000, 'kg'),
(2, 3, 'Unsalted butter', 'Ingredient', 165.000, 'kg'),
(2, 3, 'Whole milk', 'Ingredient', 520.000, 'L'),
(2, 4, 'Fresh berries (mixed)', 'Ingredient', 70.000, 'kg'),
(2, 2, 'Bakery boxes (10 inch)', 'Packaging', 650.000, 'count'),
(2, 2, 'Cupcake liners', 'Packaging', 5000.000, 'count'),
(2, 5, 'Vanilla extract', 'Ingredient', 18.000, 'L'),

(3, 1, 'All-purpose flour', 'Ingredient', 520.000, 'kg'),
(3, 1, 'Granulated sugar', 'Ingredient', 260.000, 'kg'),
(3, 3, 'Unsalted butter', 'Ingredient', 210.000, 'kg'),
(3, 3, 'Whole milk', 'Ingredient', 720.000, 'L'),
(3, 4, 'Apples (fresh)', 'Ingredient', 120.000, 'kg'),
(3, 2, 'Bakery boxes (10 inch)', 'Packaging', 900.000, 'count'),
(3, 2, 'Paper bags', 'Packaging', 3000.000, 'count'),
(3, 5, 'Baking powder', 'Ingredient', 65.000, 'kg');
GO


-- TABLE: Batch
INSERT INTO Batch (
    bakeryId, productId, employeeId,
    batchProductionDate, batchExpiryDate, batchQuantityProduced
)
VALUES
(1, 1, 1, DATEADD(day, -6, CAST(GETDATE() AS date)), DATEADD(day, -1, CAST(GETDATE() AS date)), 60),
(1, 3, 2, DATEADD(day, -3, CAST(GETDATE() AS date)), DATEADD(day, 2, CAST(GETDATE() AS date)), 90),
(1, 5, 3, DATEADD(day, -2, CAST(GETDATE() AS date)), DATEADD(day, 2, CAST(GETDATE() AS date)), 120),
(1, 8, 4, DATEADD(day, -4, CAST(GETDATE() AS date)), DATEADD(day, 6, CAST(GETDATE() AS date)), 200),
(1, 13, 3, DATEADD(day, -1, CAST(GETDATE() AS date)), DATEADD(day, 3, CAST(GETDATE() AS date)), 12),
(1, 21, 2, CAST(GETDATE() AS date), DATEADD(day, 5, CAST(GETDATE() AS date)), 80),

(2, 2, 5, DATEADD(day, -5, CAST(GETDATE() AS date)), DATEADD(day, 2, CAST(GETDATE() AS date)), 55),
(2, 6, 6, DATEADD(day, -2, CAST(GETDATE() AS date)), DATEADD(day, 3, CAST(GETDATE() AS date)), 140),
(2, 10, 7, DATEADD(day, -2, CAST(GETDATE() AS date)), DATEADD(day, 4, CAST(GETDATE() AS date)), 110),
(2, 14, 8, DATEADD(day, -1, CAST(GETDATE() AS date)), DATEADD(day, 3, CAST(GETDATE() AS date)), 40),
(2, 18, 9, CAST(GETDATE() AS date), DATEADD(day, 6, CAST(GETDATE() AS date)), 90),

(3, 4, 6, DATEADD(day, -3, CAST(GETDATE() AS date)), DATEADD(day, 2, CAST(GETDATE() AS date)), 70),
(3, 7, 7, DATEADD(day, -6, CAST(GETDATE() AS date)), DATEADD(day, 1, CAST(GETDATE() AS date)), 120),
(3, 12, 8, DATEADD(day, -2, CAST(GETDATE() AS date)), DATEADD(day, 5, CAST(GETDATE() AS date)), 30),
(3, 15, 9, DATEADD(day, -1, CAST(GETDATE() AS date)), DATEADD(day, 3, CAST(GETDATE() AS date)), 75),
(3, 16, 5, CAST(GETDATE() AS date), DATEADD(day, 4, CAST(GETDATE() AS date)), 65),
(3, 17, 4, CAST(GETDATE() AS date), DATEADD(day, 4, CAST(GETDATE() AS date)), 40),
(3, 26, 2, DATEADD(day, -1, CAST(GETDATE() AS date)), DATEADD(day, 2, CAST(GETDATE() AS date)), 50);
GO


-- TABLE: BatchInventory
INSERT INTO BatchInventory (batchId, inventoryId, quantityUsed, unitOfMeasureAtTime, rewardTransactionDate)
VALUES
(1, 1, 18.500, 'kg', DATEADD(day, -6, GETDATE())),
(1, 3, 6.000, 'kg', DATEADD(day, -6, GETDATE())),
(2, 1, 22.000, 'kg', DATEADD(day, -3, GETDATE())),
(2, 4, 18.000, 'L',  DATEADD(day, -3, GETDATE())),
(3, 1, 20.000, 'kg', DATEADD(day, -2, GETDATE())),
(3, 3, 9.000,  'kg', DATEADD(day, -2, GETDATE())),
(4, 1, 14.500, 'kg', DATEADD(day, -4, GETDATE())),
(4, 2, 7.500,  'kg', DATEADD(day, -4, GETDATE())),
(4, 8, 3.200,  'kg', DATEADD(day, -4, GETDATE())),
(5, 1, 25.000, 'kg', DATEADD(day, -1, GETDATE())),
(5, 2, 12.000, 'kg', DATEADD(day, -1, GETDATE())),
(5, 8, 4.500,  'kg', DATEADD(day, -1, GETDATE())),
(6, 1, 16.000, 'kg', GETDATE()),
(6, 2, 9.000,  'kg', GETDATE()),

(7, 9, 16.500, 'kg', DATEADD(day, -5, GETDATE())),
(7, 11, 5.500, 'kg', DATEADD(day, -5, GETDATE())),
(8, 9, 18.000, 'kg', DATEADD(day, -2, GETDATE())),
(8, 10, 8.000, 'kg', DATEADD(day, -2, GETDATE())),
(9, 9, 12.000, 'kg', DATEADD(day, -2, GETDATE())),
(9, 10, 6.000, 'kg', DATEADD(day, -2, GETDATE())),
(10, 9, 10.500, 'kg', DATEADD(day, -1, GETDATE())),
(10, 10, 4.500, 'kg', DATEADD(day, -1, GETDATE())),
(11, 9, 11.000, 'kg', GETDATE()),
(11, 10, 5.000, 'kg', GETDATE()),

(12, 17, 13.000, 'kg', DATEADD(day, -3, GETDATE())),
(12, 19, 6.500, 'kg', DATEADD(day, -3, GETDATE())),
(13, 17, 9.000,  'kg', DATEADD(day, -6, GETDATE())),
(13, 20, 4.000,  'kg', DATEADD(day, -6, GETDATE())),
(14, 17, 7.000,  'kg', DATEADD(day, -2, GETDATE())),
(14, 18, 3.500,  'kg', DATEADD(day, -2, GETDATE())),
(15, 21, 8.000,  'kg', DATEADD(day, -1, GETDATE())),
(15, 22, 900.000,'count', DATEADD(day, -1, GETDATE())),
(16, 21, 6.500,  'kg', GETDATE()),
(16, 23, 1200.000,'count', GETDATE()),
(17, 21, 4.800,  'kg', GETDATE()),
(17, 20, 3.000,  'kg', GETDATE()),
(18, 17, 5.500,  'kg', DATEADD(day, -1, GETDATE())),
(18, 18, 2.200,  'kg', DATEADD(day, -1, GETDATE()));
GO


-- TABLE: [Order]
INSERT INTO [Order] (
    customerId, bakeryId, addressId,
    orderPlacedDateTime, orderScheduledDateTime, orderDeliveredDateTime,
    orderMethod, orderComment, orderTotal, orderDiscount, orderStatus
)
VALUES
(1, 3, 21, DATEADD(day, -12, GETDATE()), DATEADD(day, -11, GETDATE()), DATEADD(day, -11, GETDATE()), 'Delivery', 'Ring buzzer upon arrival', 26.95, 0.00, 'Completed'),
(2, 3, NULL, DATEADD(day, -10, GETDATE()), DATEADD(day, -10, GETDATE()), DATEADD(day, -10, GETDATE()), 'Pickup', NULL, 12.98, 0.00, 'Completed'),
(3, 2, 23, DATEADD(day, -9, GETDATE()), DATEADD(day, -8, GETDATE()), DATEADD(day, -8, GETDATE()), 'Delivery', 'Leave with concierge', 34.20, 2.00, 'Completed'),
(4, 2, NULL, DATEADD(day, -8, GETDATE()), DATEADD(day, -8, GETDATE()), DATEADD(day, -8, GETDATE()), 'Pickup', NULL, 9.75, 0.00, 'Completed'),
(5, 1, 26, DATEADD(day, -7, GETDATE()), DATEADD(day, -6, GETDATE()), DATEADD(day, -6, GETDATE()), 'Delivery', 'Call on arrival', 41.90, 4.00, 'Completed'),
(6, 1, NULL, DATEADD(day, -6, GETDATE()), DATEADD(day, -6, GETDATE()), DATEADD(day, -6, GETDATE()), 'Pickup', NULL, 18.20, 0.00, 'Completed'),
(7, 1, 31, DATEADD(day, -5, GETDATE()), DATEADD(day, -5, GETDATE()), NULL, 'Delivery', 'Please ensure items are sealed', 22.45, 0.00, 'Scheduled'),
(8, 1, NULL, DATEADD(day, -5, GETDATE()), DATEADD(day, -5, GETDATE()), DATEADD(day, -5, GETDATE()), 'Pickup', NULL, 7.25, 0.00, 'Completed'),
(9, 3, 34, DATEADD(day, -4, GETDATE()), DATEADD(day, -3, GETDATE()), DATEADD(day, -3, GETDATE()), 'Delivery', NULL, 58.48, 5.00, 'Completed'),
(10, 3, NULL, DATEADD(day, -3, GETDATE()), DATEADD(day, -3, GETDATE()), NULL, 'Pickup', NULL, 6.49, 0.00, 'Placed'),
(11, 2, 36, DATEADD(day, -3, GETDATE()), DATEADD(day, -2, GETDATE()), DATEADD(day, -2, GETDATE()), 'Delivery', 'Front desk drop-off', 27.70, 0.00, 'Completed'),
(12, 2, NULL, DATEADD(day, -2, GETDATE()), DATEADD(day, -2, GETDATE()), DATEADD(day, -2, GETDATE()), 'Pickup', NULL, 14.50, 0.00, 'Completed'),
(13, 1, 38, DATEADD(day, -2, GETDATE()), DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()), 'Delivery', NULL, 19.95, 0.00, 'Completed'),
(14, 1, NULL, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()), NULL, 'Pickup', NULL, 29.99, 0.00, 'Placed');
GO


-- TABLE: OrderItem
INSERT INTO OrderItem (orderId, productId, batchId, orderItemQuantity, orderItemUnitPriceAtTime, orderItemLineTotal)
VALUES
(1, 14, 10, 1, 7.25, 7.25),
(1, 8, 4, 2, 2.25, 4.50),
(1, 5, 3, 2, 3.95, 7.90),

(2, 1, 2, 2, 6.49, 12.98),

(3, 13, 5, 1, 29.99, 29.99),
(3, 8, 4, 1, 2.25, 2.25),
(3, 21, 6, 1, 3.75, 3.75),

(4, 18, 11, 1, 3.75, 3.75),
(4, 6, 8, 2, 3.00, 6.00),

(5, 12, 14, 2, 6.95, 13.90),
(5, 26, 18, 1, 5.25, 5.25),
(5, 5, 3, 2, 3.95, 7.90),
(5, 8, 4, 1, 2.25, 2.25),
(5, 17, 17, 1, 6.50, 6.50),

(6, 15, 15, 1, 4.10, 4.10),
(6, 16, 16, 1, 4.75, 4.75),
(6, 18, 11, 1, 3.75, 3.75),
(6, 8, 4, 2, 2.25, 4.50),
(6, 6, 8, 1, 3.10, 3.10),

(7, 10, 9, 2, 3.50, 7.00),
(7, 5, 3, 1, 3.95, 3.95),
(7, 1, 2, 1, 6.49, 6.49),
(7, 8, 4, 2, 2.25, 4.50),

(8, 14, 10, 1, 7.25, 7.25),

(9, 13, 5, 1, 29.99, 29.99),
(9, 12, 14, 1, 6.95, 6.95),
(9, 17, 17, 2, 6.50, 13.00),
(9, 26, 18, 1, 5.25, 5.25),
(9, 8, 4, 2, 2.25, 4.50),

(10, 1, 2, 1, 6.49, 6.49),

(11, 4, 12, 1, 4.25, 4.25),
(11, 5, 3, 1, 3.95, 3.95),
(11, 18, 11, 2, 3.75, 7.50),
(11, 14, 10, 1, 7.25, 7.25),
(11, 8, 4, 2, 2.25, 4.50),

(12, 6, 8, 2, 3.25, 6.50),
(12, 8, 4, 2, 2.25, 4.50),
(12, 9, 4, 1, 2.25, 2.25),
(12, 24, 7, 1, 3.25, 3.25),

(13, 21, 6, 2, 3.75, 7.50),
(13, 5, 3, 1, 3.95, 3.95),
(13, 16, 16, 1, 4.75, 4.75),
(13, 8, 4, 1, 2.25, 2.25),
(13, 6, 8, 1, 1.50, 1.50),

(14, 13, 5, 1, 29.99, 29.99);
GO


-- TABLE: Payment
INSERT INTO Payment (orderId, paymentAmount, paymentMethod, paymentTransactionId, paymentStatus, paymentPaidAt)
VALUES
(1, 26.95, 'Credit Card', 'TRX-98314501', 'Paid', DATEADD(day, -11, GETDATE())),
(2, 12.98, 'Debit', 'TRX-98314502', 'Paid', DATEADD(day, -10, GETDATE())),
(3, 32.20, 'Credit Card', 'TRX-98314503', 'Paid', DATEADD(day, -8, GETDATE())),
(4, 9.75, 'Credit Card', 'TRX-98314504', 'Paid', DATEADD(day, -8, GETDATE())),
(5, 37.90, 'Credit Card', 'TRX-98314505', 'Paid', DATEADD(day, -6, GETDATE())),
(6, 18.20, 'Debit', 'TRX-98314506', 'Paid', DATEADD(day, -6, GETDATE())),
(7, 22.45, 'Credit Card', 'TRX-98314507', 'Authorized', NULL),
(8, 7.25, 'Credit Card', 'TRX-98314508', 'Paid', DATEADD(day, -5, GETDATE())),
(9, 53.48, 'Credit Card', 'TRX-98314509', 'Paid', DATEADD(day, -3, GETDATE())),
(10, 6.49, 'Debit', 'TRX-98314510', 'Pending', NULL),
(11, 27.70, 'Credit Card', 'TRX-98314511', 'Paid', DATEADD(day, -2, GETDATE())),
(12, 14.50, 'Credit Card', 'TRX-98314512', 'Paid', DATEADD(day, -2, GETDATE())),
(13, 19.95, 'Debit', 'TRX-98314513', 'Paid', DATEADD(day, -1, GETDATE())),
(14, 29.99, 'Credit Card', 'TRX-98314514', 'Pending', NULL);
GO


-- TABLE: Reward
INSERT INTO Reward (customerId, orderId, rewardPointsEarned, rewardTransactionDate)
VALUES
(1, 1, 26950, DATEADD(day, -11, GETDATE())),
(2, 2, 12980, DATEADD(day, -10, GETDATE())),
(3, 3, 32200, DATEADD(day, -8, GETDATE())),
(4, 4, 9750,  DATEADD(day, -8, GETDATE())),
(5, 5, 37900, DATEADD(day, -6, GETDATE())),
(6, 6, 18200, DATEADD(day, -6, GETDATE())),
(8, 8, 7250,  DATEADD(day, -5, GETDATE())),
(9, 9, 53480, DATEADD(day, -3, GETDATE())),
(11, 11, 27700, DATEADD(day, -2, GETDATE())),
(12, 12, 14500, DATEADD(day, -2, GETDATE())),
(13, 13, 19950, DATEADD(day, -1, GETDATE()));
GO


-- TABLE: Review
INSERT INTO Review (
    customerId, productId, employeeId,
    reviewRating, reviewComment, reviewSubmittedDate,
    reviewStatus, reviewApprovalDate
)
VALUES
(1, 5, 7, 5, 'Fresh and flaky, exactly what I hoped for.', DATEADD(day, -9, GETDATE()), 'Approved', DATEADD(day, -8, GETDATE())),
(2, 1, 7, 4, 'Good loaf with a nice crust.', DATEADD(day, -8, GETDATE()), 'Approved', DATEADD(day, -7, GETDATE())),
(3, 13, 7, 5, 'Excellent cake, rich and not overly sweet.', DATEADD(day, -7, GETDATE()), 'Approved', DATEADD(day, -6, GETDATE())),
(4, 6, NULL, 4, 'Muffin was soft and well-balanced.', DATEADD(day, -7, GETDATE()), 'Pending', NULL),
(5, 12, 7, 5, 'Great flavour and texture.', DATEADD(day, -6, GETDATE()), 'Approved', DATEADD(day, -5, GETDATE())),
(6, 16, 7, 3, 'Filling was good, pastry slightly dry.', DATEADD(day, -6, GETDATE()), 'Approved', DATEADD(day, -5, GETDATE())),
(7, 10, NULL, 4, 'Cupcake was moist and frosting was smooth.', DATEADD(day, -5, GETDATE()), 'Pending', NULL),
(8, 14, 7, 5, 'Very creamy slice and good crust.', DATEADD(day, -5, GETDATE()), 'Approved', DATEADD(day, -4, GETDATE())),
(9, 17, 7, 4, 'Bright flavour and a nice finish.', DATEADD(day, -4, GETDATE()), 'Approved', DATEADD(day, -3, GETDATE())),
(10, 3, NULL, 4, 'Crisp outside and soft inside.', DATEADD(day, -3, GETDATE()), 'Pending', NULL),
(11, 18, 7, 5, 'Perfect brownie, very fudgy.', DATEADD(day, -2, GETDATE()), 'Approved', DATEADD(day, -2, GETDATE())),
(12, 8, 7, 4, 'Classic cookie, good texture.', DATEADD(day, -2, GETDATE()), 'Approved', DATEADD(day, -1, GETDATE())),
(13, 21, NULL, 4, 'Nice seasonal option, would buy again.', DATEADD(day, -1, GETDATE()), 'Pending', NULL),
(14, 13, NULL, 5, 'Great for an occasion, everyone enjoyed it.', DATEADD(day, -1, GETDATE()), 'Pending', NULL);
GO


-- TABLE: CustomerPreferences
INSERT INTO CustomerPreferences (customerId, tagId, preferenceType, preferenceStrength)
VALUES
(1, 1, 'Like', 7),
(1, 10, 'Like', 6),

(2, 5, 'Dislike', 8),
(2, 9, 'Like', 6),

(3, 2, 'Like', 8),
(3, 10, 'Like', 7),

(4, 11, 'Allergic', 10),
(4, 4, 'Like', 6),

(5, 3, 'Like', 7),
(5, 7, 'Like', 6),

(6, 6, 'Dislike', 8),
(6, 8, 'Like', 6),

(7, 5, 'Allergic', 10),
(7, 9, 'Like', 6),

(8, 2, 'Like', 7),
(8, 10, 'Like', 8),

(9, 7, 'Like', 7),
(9, 9, 'Like', 6),

(10, 11, 'Allergic', 10),
(10, 4, 'Like', 6),

(11, 3, 'Dislike', 4),
(11, 9, 'Like', 6),

(12, 6, 'Dislike', 8),
(12, 10, 'Like', 7),

(13, 5, 'Allergic', 10),
(13, 7, 'Like', 6),

(14, 2, 'Like', 7),
(14, 10, 'Like', 6);
GO
