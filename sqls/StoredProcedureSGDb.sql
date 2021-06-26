USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `InsertToMarketCompleted`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `InsertToMarketCompleted`(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In transactionDate DATETIME
)
BEGIN

INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(stockId,sellerId, buyerId, quantity,price, transactionDate);

END$$

DELIMITER ;


USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllMarketCompleted`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `GetAllMarketCompleted`()
BEGIN

SELECT * 
FROM marketcompleted;

END$$
DELIMITER ;

USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `InsertToMarketPending`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `InsertToMarketPending`(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In transactionDate DATETIME
)
BEGIN

INSERT INTO marketPending (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(stockId,sellerId, buyerId, quantity,price, transactionDate);

END$$
DELIMITER ;

USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `DeleteMarketPending`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `DeleteMarketPending`(In marketPendingId int)
BEGIN

DELETE FROM marketpending where MarketPendingId = marketPendingId;

END$$
DELIMITER ;

USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllMarketPending`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `GetAllMarketPending`()
BEGIN

SELECT * FROM marketpending;

END$$
DELIMITER ;

USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `UpdateMarketPendingQuantity`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `UpdateMarketPendingQuantity`(IN marketPendingId int, In quantity int)
BEGIN

UPDATE marketpending 
SET Quantity = quantity
WHERE MarketPendingId = marketPendingId;

END$$
DELIMITER ;

USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllStocks`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `GetAllStocks`()
BEGIN

SELECT *
FROM stock;

END$$
DELIMITER ;

USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `GetTotalStockCount`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `GetTotalStockCount`()
BEGIN

SELECT Count(*)
FROM stock;

END$$
DELIMITER ;


USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getTotalHoldingsByAccountId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getTotalHoldingsByAccountId`(IN buyerId int)
BEGIN

SELECT s.CompanyName,s.StockId, sum(m.Quantity), avg(m.Price)
FROM stock s
INNER JOIN marketcompleted m 
ON s.StockId = m.StockId
Where m.BuyerId = buyerId
group by m.StockId;



END$$
DELIMITER ;

USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getCurrentValueByStockId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getCurrentValueByStockId`(IN stockId int)
BEGIN

SELECT CurrentValue 
FROM stock
where StockId = stockId;

END$$
DELIMITER ;

USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getOrdersByStockId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getOrdersByStockId`(IN stockId int)
BEGIN

SELECT * 
FROM marketpending
where StockId = stockId;

END$$
DELIMITER ;


USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getOrdersCompletedByStockId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getOrdersCompletedByStockId`(IN stockId int)
BEGIN

SELECT * 
FROM marketcompleted
where StockId = stockId;

END$$
DELIMITER ;





