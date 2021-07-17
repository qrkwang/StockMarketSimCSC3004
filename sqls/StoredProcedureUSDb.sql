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
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In createdDate DATETIME
)
BEGIN

INSERT INTO marketpending (StockId, SellerId, BuyerId, Quantity, Price, CreatedDate)
VALUES(stockId,sellerId, buyerId, quantity,price, createDate);

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `DeleteMarketPending`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `DeleteMarketPending`(In inputMarketPendingId int)
BEGIN

DELETE FROM marketpending where MarketPendingId = inputMarketPendingId;

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

CREATE PROCEDURE `UpdateMarketPendingQuantity`(IN inputMarketPendingId int, In inputQuantity int)
BEGIN

UPDATE marketpending 
SET Quantity = inputQuantity
WHERE MarketPendingId = inputMarketPendingId;

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

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `getCurrentValueByStockId`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `getCurrentValueByStockId`(IN inputStockId int)
BEGIN

SELECT CurrentValue 
FROM stock
where StockId = inputStockId;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `getOrdersByStockId`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `getOrdersByStockId`(IN inputStockId int)
BEGIN

SELECT * 
FROM marketpending
where StockId = inputStockId;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `getOrdersCompletedByStockId`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `getOrdersCompletedByStockId`(IN inputStockId int)
BEGIN

SELECT * 
FROM marketcompleted
where StockId = inputStockId;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `getPendingSellOrdersRequiredForNewInsertion`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `getPendingSellOrdersRequiredForNewInsertion`(IN inputStockId int, In inputPrice double)
BEGIN

SELECT * 
FROM marketpending
where StockId = inputStockId
AND BuyerId is null 
AND Price <= inputPrice
ORDER BY Price DESC;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `getPendingBuyOrdersRequiredForNewInsertion`;

DELIMITER $$
USE `USStockMarket`$$

CREATE PROCEDURE `getPendingBuyOrdersRequiredForNewInsertion`(IN inputStockId int, In inputPrice double)
BEGIN

SELECT * 
FROM marketpending
where StockId = inputStockId
AND SellerId is null 
AND Price >= inputPrice
ORDER BY Price;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `closeSellMarketPendingOrders`;

DELIMITER $$

USE `USStockMarket`$$
CREATE PROCEDURE `closeSellMarketPendingOrders` (In inputMarketPendingId int, In inputBuyerId int)
BEGIN
SELECT @sellerId := SellerId 
FROM marketpending 
WHERE MarketPendingId = inputMarketPendingId;

SELECT @stockId := StockId 
FROM marketpending 
WHERE MarketPendingId = inputMarketPendingId;

SELECT @quantity := Quantity 
FROM marketpending 
WHERE MarketPendingId = inputMarketPendingId;

SELECT @price := Price 
FROM marketpending
WHERE MarketPendingId = inputMarketPendingId;

DELETE FROM marketpending 
where MarketPendingId = inputMarketPendingId;

INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(@stockId,@sellerId, inputBuyerId, @quantity,@price, now());


UPDATE stock SET CurrentValue = @price WHERE StockId = @stockId;

END$$
DELIMITER ;

USE `USStockMarket`;
DROP PROCEDURE IF EXISTS `closeBuyMarketPendingOrders`;

DELIMITER $$

USE `USStockMarket`$$
CREATE PROCEDURE `closeBuyMarketPendingOrders` (In inputMarketPendingId int, In inputSellerId int)
BEGIN
SELECT @buyerId := BuyerId 
FROM marketpending 
WHERE MarketPendingId = inputMarketPendingId;

SELECT @stockId := StockId 
FROM marketpending 
WHERE MarketPendingId = inputMarketPendingId;

SELECT @quantity := Quantity 
FROM marketpending 
WHERE MarketPendingId = inputMarketPendingId;

SELECT @price := Price 
FROM marketpending
WHERE MarketPendingId = inputMarketPendingId;

DELETE FROM marketpending 
where MarketPendingId = inputMarketPendingId;

INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
VALUES(@stockId, inputSellerId, @buyerId, @quantity,@price, now());


UPDATE stock SET CurrentValue = @price WHERE StockId = @stockId;

END$$
DELIMITER ;



