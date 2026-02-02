-- DROP and CREATE BakeryEcommerce database.

USE master;
GO

DROP DATABASE IF EXISTS BakeryEcommerce;
GO

CREATE DATABASE BakeryEcommerce;
GO

USE BakeryEcommerce;
GO


-- TABLE: Address
CREATE TABLE Address (
    addressId INT IDENTITY(1,1) NOT NULL,
    addressLine1 NVARCHAR(120) NOT NULL,
    addressLine2 NVARCHAR(120) NULL,
    addressCity NVARCHAR(120) NULL,
    addressProvince NVARCHAR(80) NOT NULL,
    addressPostalCode NVARCHAR(10) NOT NULL,
    CONSTRAINT PK_Address PRIMARY KEY (addressId)
);
GO


-- TABLE: [User]
CREATE TABLE [User] (
    userId INT IDENTITY(1,1) NOT NULL,
    userUsername NVARCHAR(50) NOT NULL,
    userEmail NVARCHAR(254) NOT NULL,
    userPasswordHash NVARCHAR(255) NOT NULL,
    userRole NVARCHAR(30) NOT NULL,
    userCreatedAt DATETIME2(0) NOT NULL,
    CONSTRAINT PK_User PRIMARY KEY (userId),
    CONSTRAINT UQ_User_Username UNIQUE (userUsername),
    CONSTRAINT UQ_User_Email UNIQUE (userEmail)
);
GO


-- TABLE: RewardTier
CREATE TABLE RewardTier (
    rewardTierId INT IDENTITY(1,1) NOT NULL,
    rewardTierName NVARCHAR(30) NOT NULL,
    rewardTierMinPoints INT NOT NULL,
    rewardTierMaxPoints INT NULL,
    rewardTierDiscountRate DECIMAL(5,2) NULL,
    CONSTRAINT PK_RewardTier PRIMARY KEY (rewardTierId),
    CONSTRAINT UQ_RewardTier_Name UNIQUE (rewardTierName),
    CONSTRAINT CK_RewardTier_MinPoints CHECK (rewardTierMinPoints >= 0),
    CONSTRAINT CK_RewardTier_MaxPoints CHECK (rewardTierMaxPoints IS NULL OR rewardTierMaxPoints >= rewardTierMinPoints)
);
GO


-- TABLE: Bakery
CREATE TABLE Bakery (
    bakeryId INT IDENTITY(1,1) NOT NULL,
    addressId INT NOT NULL,
    bakeryName NVARCHAR(100) NOT NULL,
    bakeryPhone NVARCHAR(20) NOT NULL,
    bakeryEmail NVARCHAR(254) NOT NULL,
    CONSTRAINT PK_Bakery PRIMARY KEY (bakeryId),
    CONSTRAINT UQ_Bakery_Email UNIQUE (bakeryEmail),
    CONSTRAINT FK_Bakery_Address FOREIGN KEY (addressId)
        REFERENCES Address(addressId)
);
GO


-- TABLE: BakeryHours
CREATE TABLE BakeryHours (
    bakeryHoursId INT IDENTITY(1,1) NOT NULL,
    bakeryId INT NOT NULL,
    dayOfWeek TINYINT NOT NULL,
    openTime TIME(0) NULL,
    closeTime TIME(0) NULL,
    isClosed BIT NOT NULL,
    CONSTRAINT PK_BakeryHours PRIMARY KEY (bakeryHoursId),
    CONSTRAINT FK_BakeryHours_Bakery FOREIGN KEY (bakeryId)
        REFERENCES Bakery(bakeryId),
    CONSTRAINT CK_BakeryHours_Day CHECK (dayOfWeek BETWEEN 1 AND 7)
);
GO

-- TABLE: Supplier
CREATE TABLE Supplier (
    supplierId INT IDENTITY(1,1) NOT NULL,
    addressId INT NOT NULL,
    supplierName NVARCHAR(120) NOT NULL,
    supplierPhone NVARCHAR(20) NULL,
    supplierEmail NVARCHAR(254) NULL,
    CONSTRAINT PK_Supplier PRIMARY KEY (supplierId),
    CONSTRAINT FK_Supplier_Address FOREIGN KEY (addressId)
        REFERENCES Address(addressId)
);
GO


-- TABLE: Tag
CREATE TABLE Tag (
    tagId INT IDENTITY(1,1) NOT NULL,
    tagName NVARCHAR(50) NOT NULL,
    CONSTRAINT PK_Tag PRIMARY KEY (tagId),
    CONSTRAINT UQ_Tag_Name UNIQUE (tagName)
);
GO


-- TABLE: Product
CREATE TABLE Product (
    productId INT IDENTITY(1,1) NOT NULL,
    productName NVARCHAR(120) NOT NULL,
    productDescription NVARCHAR(1000) NULL,
    productBasePrice DECIMAL(10,2) NOT NULL,
    CONSTRAINT PK_Product PRIMARY KEY (productId),
    CONSTRAINT CK_Product_Price CHECK (productBasePrice >= 0)
);
GO


-- TABLE: ProductTag
CREATE TABLE ProductTag (
    productId INT NOT NULL,
    tagId INT NOT NULL,
    CONSTRAINT PK_ProductTag PRIMARY KEY (productId, tagId),
    CONSTRAINT FK_ProductTag_Product FOREIGN KEY (productId)
        REFERENCES Product(productId),
    CONSTRAINT FK_ProductTag_Tag FOREIGN KEY (tagId)
        REFERENCES Tag(tagId)
);
GO


-- TABLE: Employee
CREATE TABLE Employee (
    employeeId INT IDENTITY(1,1) NOT NULL,
    userId INT NOT NULL,
    addressId INT NOT NULL,
    employeeFirstName NVARCHAR(50) NOT NULL,
    employeeMiddleInitial NCHAR(2) NULL,
    employeeLastName NVARCHAR(50) NOT NULL,
    employeeRole NVARCHAR(40) NOT NULL,
    employeePhone NVARCHAR(20) NOT NULL,
    employeeBusinessPhone NVARCHAR(20) NULL,
    employeeEmail NVARCHAR(254) NOT NULL,
    CONSTRAINT PK_Employee PRIMARY KEY (employeeId),
    CONSTRAINT UQ_Employee_User UNIQUE (userId),
    CONSTRAINT FK_Employee_User FOREIGN KEY (userId)
        REFERENCES [User](userId),
    CONSTRAINT FK_Employee_Address FOREIGN KEY (addressId)
        REFERENCES Address(addressId)
);
GO


-- TABLE: Customer
CREATE TABLE Customer (
    customerId INT IDENTITY(1,1) NOT NULL,
    userId INT NULL,
    addressId INT NOT NULL,
    rewardTierId INT NOT NULL,
    customerFirstName NVARCHAR(50) NOT NULL,
    customerMiddleInitial NCHAR(2) NULL,
    customerLastName NVARCHAR(50) NOT NULL,
    customerRole NVARCHAR(40) NOT NULL,
    customerPhone NVARCHAR(20) NOT NULL,
    customerBusinessPhone NVARCHAR(20) NULL,
    customerRewardBalance INT NOT NULL,
    customerTierAssignedDate DATE NULL,
    customerEmail NVARCHAR(254) NOT NULL,
    CONSTRAINT PK_Customer PRIMARY KEY (customerId),
    CONSTRAINT FK_Customer_User FOREIGN KEY (userId)
        REFERENCES [User](userId),
    CONSTRAINT FK_Customer_Address FOREIGN KEY (addressId)
        REFERENCES Address(addressId),
    CONSTRAINT FK_Customer_RewardTier FOREIGN KEY (rewardTierId)
        REFERENCES RewardTier(rewardTierId)
);
GO


-- TABLE: Inventory
CREATE TABLE Inventory (
    inventoryId INT IDENTITY(1,1) NOT NULL,
    bakeryId INT NOT NULL,
    supplierId INT NOT NULL,
    inventoryItemName NVARCHAR(120) NOT NULL,
    inventoryItemType NVARCHAR(40) NOT NULL,
    inventoryQuantityOnHand DECIMAL(12,3) NOT NULL,
    inventoryUnitOfMeasure NVARCHAR(20) NOT NULL,
    CONSTRAINT PK_Inventory PRIMARY KEY (inventoryId),
    CONSTRAINT FK_Inventory_Bakery FOREIGN KEY (bakeryId)
        REFERENCES Bakery(bakeryId),
    CONSTRAINT FK_Inventory_Supplier FOREIGN KEY (supplierId)
        REFERENCES Supplier(supplierId)
);
GO


-- TABLE: Batch
CREATE TABLE Batch (
    batchId INT IDENTITY(1,1) NOT NULL,
    bakeryId INT NOT NULL,
    productId INT NOT NULL,
    employeeId INT NOT NULL,
    batchProductionDate DATE NOT NULL,
    batchExpiryDate DATE NULL,
    batchQuantityProduced INT NOT NULL,
    CONSTRAINT PK_Batch PRIMARY KEY (batchId),
    CONSTRAINT FK_Batch_Bakery FOREIGN KEY (bakeryId)
        REFERENCES Bakery(bakeryId),
    CONSTRAINT FK_Batch_Product FOREIGN KEY (productId)
        REFERENCES Product(productId),
    CONSTRAINT FK_Batch_Employee FOREIGN KEY (employeeId)
        REFERENCES Employee(employeeId)
);
GO


-- TABLE: BatchInventory
CREATE TABLE BatchInventory (
    batchId INT NOT NULL,
    inventoryId INT NOT NULL,
    quantityUsed DECIMAL(12,3) NOT NULL,
    unitOfMeasureAtTime NVARCHAR(20) NOT NULL,
    rewardTransactionDate DATETIME2(0) NOT NULL,
    CONSTRAINT PK_BatchInventory PRIMARY KEY (batchId, inventoryId),
    CONSTRAINT FK_BatchInventory_Batch FOREIGN KEY (batchId)
        REFERENCES Batch(batchId),
    CONSTRAINT FK_BatchInventory_Inventory FOREIGN KEY (inventoryId)
        REFERENCES Inventory(inventoryId)
);
GO


-- TABLE: [Order]
CREATE TABLE [Order] (
    orderId INT IDENTITY(1,1) NOT NULL,
    customerId INT NOT NULL,
    bakeryId INT NOT NULL,
    addressId INT NULL,
    orderPlacedDateTime DATETIME2(0) NOT NULL,
    orderScheduledDateTime DATETIME2(0) NULL,
    orderDeliveredDateTime DATETIME2(0) NULL,
    orderMethod NVARCHAR(20) NOT NULL,
    orderComment NVARCHAR(500) NULL,
    orderTotal DECIMAL(10,2) NOT NULL,
    orderDiscount DECIMAL(10,2) NOT NULL,
    orderStatus NVARCHAR(30) NOT NULL,
    CONSTRAINT PK_Order PRIMARY KEY (orderId),
    CONSTRAINT FK_Order_Customer FOREIGN KEY (customerId)
        REFERENCES Customer(customerId),
    CONSTRAINT FK_Order_Bakery FOREIGN KEY (bakeryId)
        REFERENCES Bakery(bakeryId),
    CONSTRAINT FK_Order_Address FOREIGN KEY (addressId)
        REFERENCES Address(addressId)
);
GO


-- TABLE: OrderItem
CREATE TABLE OrderItem (
    orderItemId INT IDENTITY(1,1) NOT NULL,
    orderId INT NOT NULL,
    productId INT NOT NULL,
    batchId INT NULL,
    orderItemQuantity INT NOT NULL,
    orderItemUnitPriceAtTime DECIMAL(10,2) NOT NULL,
    orderItemLineTotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT PK_OrderItem PRIMARY KEY (orderItemId),
    CONSTRAINT FK_OrderItem_Order FOREIGN KEY (orderId)
        REFERENCES [Order](orderId),
    CONSTRAINT FK_OrderItem_Product FOREIGN KEY (productId)
        REFERENCES Product(productId),
    CONSTRAINT FK_OrderItem_Batch FOREIGN KEY (batchId)
        REFERENCES Batch(batchId)
);
GO


-- TABLE: Payment
CREATE TABLE Payment (
    paymentId INT IDENTITY(1,1) NOT NULL,
    orderId INT NOT NULL,
    paymentAmount DECIMAL(10,2) NOT NULL,
    paymentMethod NVARCHAR(30) NOT NULL,
    paymentTransactionId NVARCHAR(100) NULL,
    paymentStatus NVARCHAR(30) NOT NULL,
    paymentPaidAt DATETIME2(0) NULL,
    CONSTRAINT PK_Payment PRIMARY KEY (paymentId),
    CONSTRAINT FK_Payment_Order FOREIGN KEY (orderId)
        REFERENCES [Order](orderId)
);
GO


-- TABLE: Reward
CREATE TABLE Reward (
    rewardId INT IDENTITY(1,1) NOT NULL,
    customerId INT NOT NULL,
    orderId INT NOT NULL,
    rewardPointsEarned INT NOT NULL,
    rewardTransactionDate DATETIME2(0) NOT NULL,
    CONSTRAINT PK_Reward PRIMARY KEY (rewardId),
    CONSTRAINT FK_Reward_Customer FOREIGN KEY (customerId)
        REFERENCES Customer(customerId),
    CONSTRAINT FK_Reward_Order FOREIGN KEY (orderId)
        REFERENCES [Order](orderId)
);
GO


-- TABLE: Review
CREATE TABLE Review (
    reviewId INT IDENTITY(1,1) NOT NULL,
    customerId INT NOT NULL,
    productId INT NOT NULL,
    employeeId INT NULL,
    reviewRating TINYINT NOT NULL,
    reviewComment NVARCHAR(2000) NULL,
    reviewSubmittedDate DATETIME2(0) NOT NULL,
    reviewStatus NVARCHAR(30) NOT NULL,
    reviewApprovalDate DATETIME2(0) NULL,
    CONSTRAINT PK_Review PRIMARY KEY (reviewId),
    CONSTRAINT FK_Review_Customer FOREIGN KEY (customerId)
        REFERENCES Customer(customerId),
    CONSTRAINT FK_Review_Product FOREIGN KEY (productId)
        REFERENCES Product(productId),
    CONSTRAINT FK_Review_Employee FOREIGN KEY (employeeId)
        REFERENCES Employee(employeeId)
);
GO


-- TABLE: CustomerPreferences
CREATE TABLE CustomerPreferences (
    customerId INT NOT NULL,
    tagId INT NOT NULL,
    preferenceType NVARCHAR(20) NOT NULL,
    preferenceStrength TINYINT NULL,
    CONSTRAINT PK_CustomerPreferences PRIMARY KEY (customerId, tagId),
    CONSTRAINT FK_CustomerPreferences_Customer FOREIGN KEY (customerId)
        REFERENCES Customer(customerId),
    CONSTRAINT FK_CustomerPreferences_Tag FOREIGN KEY (tagId)
        REFERENCES Tag(tagId)
);
GO