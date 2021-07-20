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
FROM marketcompleted
ORDER BY TransactionDate ASC;

END$$
DELIMITER ;


USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `InsertToMarketPending`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `InsertToMarketPending`(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In createdDate DATETIME
)
BEGIN

INSERT INTO marketpending (StockId, SellerId, BuyerId, Quantity, Price, CreatedDate)
VALUES(stockId,sellerId, buyerId, quantity,price, createdDate);

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
DROP PROCEDURE IF EXISTS `getOrderBookByStockId`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `getOrderBookByStockId`(IN inputStockId int)
BEGIN

SELECT "BUY" as "Type", sum(Quantity) as "Quantity", Price
FROM hkstockmarket.marketpending
WHERE SellerId IS NULL AND StockId = inputStockId
GROUP BY Price
UNION 
SELECT "SELL" as "Type", sum(Quantity) as "Quantity", Price
FROM hkstockmarket.marketpending
WHERE BuyerId IS NULL AND StockId = inputStockId
GROUP BY Price
ORDER BY type, Price asc;

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
where StockId = inputStockId
ORDER BY TransactionDate ASC;

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
AND BuyerId is null 
AND Price <= inputPrice
ORDER BY Price DESC;


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
AND SellerId is null 
AND Price >= inputPrice
ORDER BY Price;

END$$
DELIMITER ;

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `closeSellMarketPendingOrders`;

DELIMITER $$

USE `HKStockMarket`$$
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

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `closeBuyMarketPendingOrders`;

DELIMITER $$

USE `HKStockMarket`$$
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

USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `getTop5CompletedOrderByStockId`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE `getTop5CompletedOrderByStockId`(IN inputStockId int)
BEGIN

SELECT FORMAT(Avg(Price),2) as averagePrice
FROM  (Select Price
FROM marketcompleted 
where StockId = inputStockId
ORDER BY TransactionDate desc limit 5) P;

END$$
DELIMITER ;

-- USE this to generate dummy data after create the table
USE `HKStockMarket`;
DROP PROCEDURE IF EXISTS `InsertMarketCompletedData`;

DELIMITER $$
USE `HKStockMarket`$$

CREATE PROCEDURE InsertMarketCompletedData(
)
BEGIN
	DECLARE counterid INT DEFAULT 1;
    DECLARE insertStockId int;
    DECLARE insertPrice double;
    DECLARE InsertQuantity int DEFAULT 20;
    DECLARE insertId int;
    DECLARE insertBuyerId int DEFAULT 0;
    DECLARE insertSellerId int default 0;
    DECLARE getTime timestamp;
    
    
	WHILE counterid <=(SELECT COUNT(*) FROM stock) do
		SELECT @insertStockId := StockId
        from stock
        WHERE StockId = counterid;
        
        SELECT @insertPrice := CurrentValue
        from stock
        WHERE StockId = counterid;
		SET insertSellerId = 0;
        SET insertBuyerId = 11;
        SET insertId = 1;
        SET getTime = now();
        WHILE insertId <=10 do
        SET insertSellerId = insertSellerId + 1;
        SET insertBuyerId = insertBuyerId - 1;
        SET InsertQuantity = InsertQuantity + 1;
        SET @insertPrice = @insertPrice + 0.1;
		if insertId < 4 then 
        INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
		VALUES(@insertStockId,insertSellerId, insertBuyerId, InsertQuantity,@insertPrice, getTime);
        else
        SET getTime = getTime + INTERVAL 1 minute;
        INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
		VALUES(@insertStockId,insertSellerId, insertBuyerId, InsertQuantity,@insertPrice, getTime);
        END IF;
        SET insertId = insertId +1;
        END WHILE;
        SET counterid = counterid + 1;
	END WHILE;
END$$

-- Uncomment if first time run the stored procedure
-- CALL InsertMarketCompletedData();





