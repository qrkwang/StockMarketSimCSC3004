USE HKStockMarket;
DROP PROCEDURE IF EXISTS InsertToMarketCompleted;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE InsertToMarketCompleted(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In transactionDate DATETIME
)
BEGIN

INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(stockId,sellerId, buyerId, quantity,price, transactionDate);

END$$
DELIMITER =;

USE HKStockMarket;
DROP PROCEDURE IF EXISTS GetAllMarketCompleted

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE GetAllMarketCompleted()
BEGIN

SELECT * 
FROM marketcompleted;

END$$
DELIMITER =;


USE HKStockMarket;
DROP PROCEDURE IF EXISTS InsertToMarketPending;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE InsertToMarketPending(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In transactionDate DATETIME
)
BEGIN

INSERT INTO marketPending (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(stockId,sellerId, buyerId, quantity,price, transactionDate);

END$$
DELIMITER =;

USE HKStockMarket;
DROP PROCEDURE IF EXISTS DeleteMarketPending;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE DeleteMarketPending(In marketPendingId int)
BEGIN

DELETE FROM marketpending where MarketPendingId = marketPendingId;

END$$
DELIMITER =;

USE HKStockMarket;
DROP PROCEDURE IF EXISTS GetAllMarketPending;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE GetAllMarketPending()
BEGIN

SELECT * FROM marketpending;

END$$
DELIMITER =;

USE HKStockMarket;
DROP PROCEDURE IF EXISTS UpdateMarketPendingQuantity;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE UpdateMarketPendingQuantity(IN marketPendingId int, In quantity int)
BEGIN

UPDATE marketpending 
SET Quantity = quantity
WHERE MarketPendingId = marketPendingId;

END$$
DELIMITER =;




