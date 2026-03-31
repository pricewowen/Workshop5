-- MySQL-compatible DDL
DROP DATABASE IF EXISTS BakeryEcommerce;

CREATE DATABASE IF NOT EXISTS BakeryEcommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE BakeryEcommerce;

SET
    FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS BakeryEcommerce;

CREATE DATABASE BakeryEcommerce;

USE BakeryEcommerce;

-- TABLE: Address
CREATE TABLE Address (
    addressId INT AUTO_INCREMENT NOT NULL,
    addressLine1 VARCHAR(120) NOT NULL,
    addressLine2 VARCHAR(120) NULL,
    addressCity VARCHAR(120) NULL,
    addressProvince VARCHAR(80) NOT NULL,
    addressPostalCode VARCHAR(10) NOT NULL,
    CONSTRAINT PK_Address PRIMARY KEY (addressId)
) ENGINE = InnoDB;

-- TABLE: `User`
CREATE TABLE `User` (
    userId INT AUTO_INCREMENT NOT NULL,
    userUsername VARCHAR(50) NOT NULL,
    userEmail VARCHAR(254) NOT NULL,
    userPasswordHash VARCHAR(255) NOT NULL,
    userRole VARCHAR(30) NOT NULL,
    userCreatedAt DATETIME(0) NOT NULL,
    CONSTRAINT PK_User PRIMARY KEY (userId),
    CONSTRAINT UQ_User_Username UNIQUE (userUsername),
    CONSTRAINT UQ_User_Email UNIQUE (userEmail)
) ENGINE = InnoDB;

-- TABLE: RewardTier
CREATE TABLE RewardTier (
    rewardTierId INT AUTO_INCREMENT NOT NULL,
    rewardTierName VARCHAR(30) NOT NULL,
    rewardTierMinPoints INT NOT NULL,
    rewardTierMaxPoints INT NULL,
    rewardTierDiscountRate DECIMAL(5, 2) NULL,
    CONSTRAINT PK_RewardTier PRIMARY KEY (rewardTierId),
    CONSTRAINT UQ_RewardTier_Name UNIQUE (rewardTierName),
    CONSTRAINT CK_RewardTier_MinPoints CHECK (rewardTierMinPoints >= 0),
    CONSTRAINT CK_RewardTier_MaxPoints CHECK (
        rewardTierMaxPoints IS NULL
        OR rewardTierMaxPoints >= rewardTierMinPoints
    )
) ENGINE = InnoDB;

-- TABLE: Bakery
CREATE TABLE Bakery (
    bakeryId INT AUTO_INCREMENT NOT NULL,
    addressId INT NOT NULL,
    bakeryName VARCHAR(100) NOT NULL,
    bakeryPhone VARCHAR(20) NOT NULL,
    bakeryEmail VARCHAR(254) NOT NULL,
    CONSTRAINT PK_Bakery PRIMARY KEY (bakeryId),
    CONSTRAINT UQ_Bakery_Email UNIQUE (bakeryEmail),
    CONSTRAINT FK_Bakery_Address FOREIGN KEY (addressId) REFERENCES Address(addressId)
) ENGINE = InnoDB;

-- TABLE: BakeryHours
CREATE TABLE BakeryHours (
    bakeryHoursId INT AUTO_INCREMENT NOT NULL,
    bakeryId INT NOT NULL,
    dayOfWeek TINYINT NOT NULL,
    openTime TIME(0) NULL,
    closeTime TIME(0) NULL,
    isClosed TINYINT(1) NOT NULL,
    CONSTRAINT PK_BakeryHours PRIMARY KEY (bakeryHoursId),
    CONSTRAINT FK_BakeryHours_Bakery FOREIGN KEY (bakeryId) REFERENCES Bakery(bakeryId),
    CONSTRAINT CK_BakeryHours_Day CHECK (
        dayOfWeek BETWEEN 1
        AND 7
    )
) ENGINE = InnoDB;

-- TABLE: Supplier
CREATE TABLE Supplier (
    supplierId INT AUTO_INCREMENT NOT NULL,
    addressId INT NOT NULL,
    supplierName VARCHAR(120) NOT NULL,
    supplierPhone VARCHAR(20) NULL,
    supplierEmail VARCHAR(254) NULL,
    CONSTRAINT PK_Supplier PRIMARY KEY (supplierId),
    CONSTRAINT FK_Supplier_Address FOREIGN KEY (addressId) REFERENCES Address(addressId)
) ENGINE = InnoDB;

-- TABLE: Tag
CREATE TABLE Tag (
    tagId INT AUTO_INCREMENT NOT NULL,
    tagName VARCHAR(50) NOT NULL,
    CONSTRAINT PK_Tag PRIMARY KEY (tagId),
    CONSTRAINT UQ_Tag_Name UNIQUE (tagName)
) ENGINE = InnoDB;

-- TABLE: Product
CREATE TABLE Product (
    productId INT AUTO_INCREMENT NOT NULL,
    productName VARCHAR(120) NOT NULL,
    productDescription VARCHAR(1000) NULL,
    productBasePrice DECIMAL(10, 2) NOT NULL,
    CONSTRAINT PK_Product PRIMARY KEY (productId),
    CONSTRAINT CK_Product_Price CHECK (productBasePrice >= 0)
) ENGINE = InnoDB;

-- TABLE: ProductTag
CREATE TABLE ProductTag (
    productId INT NOT NULL,
    tagId INT NOT NULL,
    CONSTRAINT PK_ProductTag PRIMARY KEY (productId, tagId),
    CONSTRAINT FK_ProductTag_Product FOREIGN KEY (productId) REFERENCES Product(productId),
    CONSTRAINT FK_ProductTag_Tag FOREIGN KEY (tagId) REFERENCES Tag(tagId)
) ENGINE = InnoDB;

-- TABLE: Employee
CREATE TABLE Employee (
    employeeId INT AUTO_INCREMENT NOT NULL,
    userId INT NOT NULL,
    addressId INT NOT NULL,
    employeeFirstName VARCHAR(50) NOT NULL,
    employeeMiddleInitial CHAR(2) NULL,
    employeeLastName VARCHAR(50) NOT NULL,
    employeeRole VARCHAR(40) NOT NULL,
    employeePhone VARCHAR(20) NOT NULL,
    employeeBusinessPhone VARCHAR(20) NULL,
    employeeEmail VARCHAR(254) NOT NULL,
    CONSTRAINT PK_Employee PRIMARY KEY (employeeId),
    CONSTRAINT UQ_Employee_User UNIQUE (userId),
    CONSTRAINT FK_Employee_User FOREIGN KEY (userId) REFERENCES `User`(userId),
    CONSTRAINT FK_Employee_Address FOREIGN KEY (addressId) REFERENCES Address(addressId)
) ENGINE = InnoDB;

-- TABLE: Customer
CREATE TABLE Customer (
    customerId INT AUTO_INCREMENT NOT NULL,
    userId INT NULL,
    addressId INT NOT NULL,
    rewardTierId INT NOT NULL,
    customerFirstName VARCHAR(50) NOT NULL,
    customerMiddleInitial CHAR(2) NULL,
    customerLastName VARCHAR(50) NOT NULL,
    customerRole VARCHAR(40) NOT NULL,
    customerPhone VARCHAR(20) NOT NULL,
    customerBusinessPhone VARCHAR(20) NULL,
    customerRewardBalance INT NOT NULL,
    customerTierAssignedDate DATETIME NULL,
    customerEmail VARCHAR(254) NOT NULL,
    CONSTRAINT PK_Customer PRIMARY KEY (customerId),
    CONSTRAINT FK_Customer_User FOREIGN KEY (userId) REFERENCES `User`(userId),
    CONSTRAINT FK_Customer_Address FOREIGN KEY (addressId) REFERENCES Address(addressId),
    CONSTRAINT FK_Customer_RewardTier FOREIGN KEY (rewardTierId) REFERENCES RewardTier(rewardTierId)
) ENGINE = InnoDB;

-- TABLE: Inventory
CREATE TABLE Inventory (
    inventoryId INT AUTO_INCREMENT NOT NULL,
    bakeryId INT NOT NULL,
    supplierId INT NOT NULL,
    inventoryItemName VARCHAR(120) NOT NULL,
    inventoryItemType VARCHAR(40) NOT NULL,
    inventoryQuantityOnHand DECIMAL(12, 3) NOT NULL,
    inventoryUnitOfMeasure VARCHAR(20) NOT NULL,
    CONSTRAINT PK_Inventory PRIMARY KEY (inventoryId),
    CONSTRAINT FK_Inventory_Bakery FOREIGN KEY (bakeryId) REFERENCES Bakery(bakeryId),
    CONSTRAINT FK_Inventory_Supplier FOREIGN KEY (supplierId) REFERENCES Supplier(supplierId)
) ENGINE = InnoDB;

-- TABLE: Batch
CREATE TABLE Batch (
    batchId INT AUTO_INCREMENT NOT NULL,
    bakeryId INT NOT NULL,
    productId INT NOT NULL,
    employeeId INT NOT NULL,
    batchProductionDate DATETIME NOT NULL,
    batchExpiryDate DATETIME NULL,
    batchQuantityProduced INT NOT NULL,
    CONSTRAINT PK_Batch PRIMARY KEY (batchId),
    CONSTRAINT FK_Batch_Bakery FOREIGN KEY (bakeryId) REFERENCES Bakery(bakeryId),
    CONSTRAINT FK_Batch_Product FOREIGN KEY (productId) REFERENCES Product(productId),
    CONSTRAINT FK_Batch_Employee FOREIGN KEY (employeeId) REFERENCES Employee(employeeId)
) ENGINE = InnoDB;

-- TABLE: BatchInventory
CREATE TABLE BatchInventory (
    batchId INT NOT NULL,
    inventoryId INT NOT NULL,
    quantityUsed DECIMAL(12, 3) NOT NULL,
    unitOfMeasureAtTime VARCHAR(20) NOT NULL,
    rewardTransactionDate DATETIME(0) NOT NULL,
    CONSTRAINT PK_BatchInventory PRIMARY KEY (batchId, inventoryId),
    CONSTRAINT FK_BatchInventory_Batch FOREIGN KEY (batchId) REFERENCES Batch(batchId),
    CONSTRAINT FK_BatchInventory_Inventory FOREIGN KEY (inventoryId) REFERENCES Inventory(inventoryId)
) ENGINE = InnoDB;

-- TABLE: `Order`
CREATE TABLE `Order` (
    orderId INT AUTO_INCREMENT NOT NULL,
    customerId INT NOT NULL,
    bakeryId INT NOT NULL,
    addressId INT NULL,
    orderPlacedDateTime DATETIME(0) NOT NULL,
    orderScheduledDateTime DATETIME(0) NULL,
    orderDeliveredDateTime DATETIME(0) NULL,
    orderMethod VARCHAR(20) NOT NULL,
    orderComment VARCHAR(500) NULL,
    orderTotal DECIMAL(10, 2) NOT NULL,
    orderDiscount DECIMAL(10, 2) NOT NULL,
    orderStatus VARCHAR(30) NOT NULL,
    CONSTRAINT PK_Order PRIMARY KEY (orderId),
    CONSTRAINT FK_Order_Customer FOREIGN KEY (customerId) REFERENCES Customer(customerId),
    CONSTRAINT FK_Order_Bakery FOREIGN KEY (bakeryId) REFERENCES Bakery(bakeryId),
    CONSTRAINT FK_Order_Address FOREIGN KEY (addressId) REFERENCES Address(addressId)
) ENGINE = InnoDB;

-- TABLE: OrderItem
CREATE TABLE OrderItem (
    orderItemId INT AUTO_INCREMENT NOT NULL,
    orderId INT NOT NULL,
    productId INT NOT NULL,
    batchId INT NULL,
    orderItemQuantity INT NOT NULL,
    orderItemUnitPriceAtTime DECIMAL(10, 2) NOT NULL,
    orderItemLineTotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT PK_OrderItem PRIMARY KEY (orderItemId),
    CONSTRAINT FK_OrderItem_Order FOREIGN KEY (orderId) REFERENCES `Order`(orderId),
    CONSTRAINT FK_OrderItem_Product FOREIGN KEY (productId) REFERENCES Product(productId),
    CONSTRAINT FK_OrderItem_Batch FOREIGN KEY (batchId) REFERENCES Batch(batchId)
) ENGINE = InnoDB;

-- TABLE: Payment
CREATE TABLE Payment (
    paymentId INT AUTO_INCREMENT NOT NULL,
    orderId INT NOT NULL,
    paymentAmount DECIMAL(10, 2) NOT NULL,
    paymentMethod VARCHAR(30) NOT NULL,
    paymentTransactionId VARCHAR(100) NULL,
    paymentStatus VARCHAR(30) NOT NULL,
    paymentPaidAt DATETIME(0) NULL,
    CONSTRAINT PK_Payment PRIMARY KEY (paymentId),
    CONSTRAINT FK_Payment_Order FOREIGN KEY (orderId) REFERENCES `Order`(orderId)
) ENGINE = InnoDB;

-- TABLE: Reward
CREATE TABLE Reward (
    rewardId INT AUTO_INCREMENT NOT NULL,
    customerId INT NOT NULL,
    orderId INT NOT NULL,
    rewardPointsEarned INT NOT NULL,
    rewardTransactionDate DATETIME(0) NOT NULL,
    CONSTRAINT PK_Reward PRIMARY KEY (rewardId),
    CONSTRAINT FK_Reward_Customer FOREIGN KEY (customerId) REFERENCES Customer(customerId),
    CONSTRAINT FK_Reward_Order FOREIGN KEY (orderId) REFERENCES `Order`(orderId)
) ENGINE = InnoDB;

-- TABLE: Review
CREATE TABLE Review (
    reviewId INT AUTO_INCREMENT NOT NULL,
    customerId INT NOT NULL,
    productId INT NOT NULL,
    employeeId INT NULL,
    reviewRating TINYINT NOT NULL,
    reviewComment VARCHAR(2000) NULL,
    reviewSubmittedDate DATETIME(0) NOT NULL,
    reviewStatus VARCHAR(30) NOT NULL,
    reviewApprovalDate DATETIME(0) NULL,
    CONSTRAINT PK_Review PRIMARY KEY (reviewId),
    CONSTRAINT FK_Review_Customer FOREIGN KEY (customerId) REFERENCES Customer(customerId),
    CONSTRAINT FK_Review_Product FOREIGN KEY (productId) REFERENCES Product(productId),
    CONSTRAINT FK_Review_Employee FOREIGN KEY (employeeId) REFERENCES Employee(employeeId)
) ENGINE = InnoDB;

-- TABLE: CustomerPreferences
CREATE TABLE CustomerPreferences (
    customerId INT NOT NULL,
    tagId INT NOT NULL,
    preferenceType VARCHAR(20) NOT NULL,
    preferenceStrength TINYINT NULL,
    CONSTRAINT PK_CustomerPreferences PRIMARY KEY (customerId, tagId),
    CONSTRAINT FK_CustomerPreferences_Customer FOREIGN KEY (customerId) REFERENCES Customer(customerId),
    CONSTRAINT FK_CustomerPreferences_Tag FOREIGN KEY (tagId) REFERENCES Tag(tagId)
) ENGINE = InnoDB;

SET
    FOREIGN_KEY_CHECKS = 1;