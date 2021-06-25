USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `InsertToMarketCompleted`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `InsertToMarketCompleted`(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In transactionDate DATETIME
)
BEGIN

INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(stockId,sellerId, buyerId, quantity,price, transactionDate);

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllMarketCompleted`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `GetAllMarketCompleted`()
BEGIN

SELECT * 
FROM marketcompleted;

END$$
DELIMITER ;


USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `InsertToMarketPending`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `InsertToMarketPending`(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In transactionDate DATETIME
)
BEGIN

INSERT INTO marketPending (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(stockId,sellerId, buyerId, quantity,price, transactionDate);

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `DeleteMarketPending`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `DeleteMarketPending`(In marketPendingId int)
BEGIN

DELETE FROM marketpending where MarketPendingId = marketPendingId;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllMarketPending`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `GetAllMarketPending`()
BEGIN

SELECT * FROM marketpending;

END$$
DELIMITER ;

USE USStockMarket;
DROP PROCEDURE IF EXISTS `UpdateMarketPendingQuantity`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `UpdateMarketPendingQuantity`(IN marketPendingId int, In quantity int)
BEGIN

UPDATE marketpending 
SET Quantity = quantity
WHERE MarketPendingId = marketPendingId;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllStocks`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `GetAllStocks`()
BEGIN

SELECT *
FROM stock;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `GetTotalStockCount`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `GetTotalStockCount`()
BEGIN

SELECT Count(*)
FROM stock;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `getTotalHoldingsByAccountId`;

DELIMITER $$
USE `USStockMarket`$$

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






