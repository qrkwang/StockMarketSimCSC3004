USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `InsertToMarketCompleted`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `InsertToMarketCompleted`(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In transactionDate DATETIME
)
BEGIN

INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(stockId,sellerId, buyerId, quantity,price, transactionDate);

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllMarketCompleted`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `GetAllMarketCompleted`()
BEGIN

SELECT * 
FROM marketcompleted;

END$$
DELIMITER ;


USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `InsertToMarketPending`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `InsertToMarketPending`(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In transactionDate DATETIME
)
BEGIN

INSERT INTO marketPending (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(stockId,sellerId, buyerId, quantity,price, transactionDate);

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `DeleteMarketPending`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `DeleteMarketPending`(In inputMarketPendingId int)
BEGIN

DELETE FROM marketpending where MarketPendingId = inputMarketPendingId;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllMarketPending`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `GetAllMarketPending`()
BEGIN

SELECT * FROM marketpending;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `UpdateMarketPendingQuantity`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `UpdateMarketPendingQuantity`(IN inputMarketPendingId int, In inputQuantity int)
BEGIN

UPDATE marketpending 
SET Quantity = inputQuantity
WHERE MarketPendingId = inputMarketPendingId;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllStocks`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `GetAllStocks`()
BEGIN

SELECT *
FROM stock;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `GetTotalStockCount`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `GetTotalStockCount`()
BEGIN

SELECT Count(*)
FROM stock;

END$$
DELIMITER ;


USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `getTotalHoldingsByAccountId`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `getTotalHoldingsByAccountId`(IN inputBuyerId int)
BEGIN

SELECT s.CompanyName,s.StockId, s.TickerSymbol, sum(m.Quantity), avg(m.Price)
FROM stock s
INNER JOIN marketcompleted m 
ON s.StockId = m.StockId
Where m.BuyerId = inputBuyerId
group by m.StockId;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `getCurrentValueByStockId`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `getCurrentValueByStockId`(IN inputStockId int)
BEGIN

SELECT CurrentValue 
FROM stock
where StockId = inputStockId;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `getOrdersByStockId`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `getOrdersByStockId`(IN inputStockId int)
BEGIN

SELECT * 
FROM marketpending
where StockId = inputStockId;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `getOrdersCompletedByStockId`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `getOrdersCompletedByStockId`(IN inputStockId int)
BEGIN

SELECT * 
FROM marketcompleted
where StockId = inputStockId;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `getPendingSellOrdersRequiredForNewInsertion`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `getPendingSellOrdersRequiredForNewInsertion`(IN inputStockId int, In inputPrice double)
BEGIN

SELECT * 
FROM marketpending
where StockId = inputStockId
AND SellerId is null 
AND Price <= inputPrice
ORDER BY Price;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `getPendingBuyOrdersRequiredForNewInsertion`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `getPendingBuyOrdersRequiredForNewInsertion`(IN inputStockId int, In inputPrice double)
BEGIN

SELECT * 
FROM marketpending
where StockId = inputStockId
AND BuyerId is null 
AND Price >= inputPrice
ORDER BY Price;

END$$
DELIMITER ;







