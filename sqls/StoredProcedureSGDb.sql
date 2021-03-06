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

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllMarketCompleted`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `GetAllMarketCompleted`()
BEGIN

	SELECT * FROM marketcompleted
	ORDER BY TransactionDate ASC;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `InsertToMarketPending`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `InsertToMarketPending`(
IN stockId Int, In sellerId Int, In buyerId Int, In quantity Int, In price Double, In createdDate DATETIME
)
BEGIN

	INSERT INTO marketpending (StockId, SellerId, BuyerId, Quantity, Price, CreatedDate)
	VALUES(stockId,sellerId, buyerId, quantity,price, createdDate);

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `DeleteMarketPending`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `DeleteMarketPending`(In inputMarketPendingId int)
BEGIN

	DELETE FROM marketpending 
    WHERE MarketPendingId = inputMarketPendingId;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllMarketPending`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `GetAllMarketPending`()
BEGIN

SELECT * FROM marketpending;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `UpdateSellMarketPendingQuantity`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `UpdateSellMarketPendingQuantity`(IN inputMarketPendingId int, In inputQuantity int, In myQty int, In inputBuyerId int)

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

	UPDATE marketpending 
	SET Quantity = inputQuantity
	WHERE MarketPendingId = inputMarketPendingId;

	INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
	VALUES(@stockId,@sellerId, inputBuyerId, myQty,@price, now());

	UPDATE stock SET CurrentValue = @price WHERE StockId = @stockId;
    
END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `UpdateBuyMarketPendingQuantity`;

DELIMITER $$
USE `SGStockMarket`$$
CREATE PROCEDURE `UpdateBuyMarketPendingQuantity`(IN inputMarketPendingId int, In inputQuantity int, In myQty int, In inputSellerId int)

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

	UPDATE marketpending 
	SET Quantity = inputQuantity
	WHERE MarketPendingId = inputMarketPendingId;

	INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
	VALUES(@stockId,inputSellerId, @buyerId, myQty ,@price, now());

	UPDATE stock SET CurrentValue = @price WHERE StockId = @stockId;
    
END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `GetAllStocks`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `GetAllStocks`()
BEGIN

	SELECT *FROM stock;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `GetTotalStockCount`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `GetTotalStockCount`()
BEGIN

	SELECT Count(*) FROM stock;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getTotalHoldingsByAccountId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getTotalHoldingsByAccountId`(IN inputBuyerId int)
BEGIN

	SELECT s.CompanyName,s.StockId, s.TickerSymbol, (totalQuantity - IFNULL(soldQuantity,0)) AS Quantity, (totalPrice - IFNULL(soldPrice,0))/(totalQuantity - IFNULL(soldQuantity,0)) AS Price
	FROM stock s
	JOIN (SELECT m.StockId, SUM(m.Quantity) AS totalQuantity, SUM(m.Price * m.Quantity) AS totalPrice
		FROM marketcompleted m WHERE m.BuyerId = inputBuyerId GROUP BY m.StockId) total ON total.StockId = s.StockId
	LEFT JOIN (SELECT m.StockId, SUM(m.Quantity) AS soldQuantity, SUM(m.Price * m.Quantity) AS soldPrice
		FROM marketcompleted m WHERE m.SellerId = inputBuyerId GROUP BY m.StockId) sold ON sold.StockId = total.StockId;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getCurrentValueByStockId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getCurrentValueByStockId`(IN inputStockId int)
BEGIN

	SELECT CurrentValue FROM stock
	WHERE StockId = inputStockId;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getOrdersByStockId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getOrdersByStockId`(IN inputStockId int)
BEGIN

	SELECT * FROM marketpending
	WHERE StockId = inputStockId;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getOrderBookByStockId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getOrderBookByStockId`(IN inputStockId int)
BEGIN

	(SELECT "BUY" as "Type", sum(Quantity) as "Quantity", Price
		FROM marketpending
		WHERE SellerId IS NULL AND StockId = inputStockId
		GROUP BY Price
		ORDER BY Price DESC)
	UNION 
	(SELECT "SELL" as "Type", sum(Quantity) as "Quantity", Price
		FROM marketpending
		WHERE BuyerId IS NULL AND StockId = inputStockId
		GROUP BY Price
		ORDER BY Price ASC);

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getOrdersCompletedByStockId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getOrdersCompletedByStockId`(IN inputStockId int)
BEGIN

	SELECT * FROM marketcompleted
	WHERE StockId = inputStockId
	ORDER BY TransactionDate ASC;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getPendingSellOrdersRequiredForNewInsertion`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getPendingSellOrdersRequiredForNewInsertion`(IN inputStockId int, In inputPrice double)
BEGIN

	SELECT * FROM marketpending
	WHERE StockId = inputStockId
		AND BuyerId is null 
		AND Price <= inputPrice
	ORDER BY Price DESC;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getPendingBuyOrdersRequiredForNewInsertion`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getPendingBuyOrdersRequiredForNewInsertion`(IN inputStockId int, In inputPrice double)
BEGIN

	SELECT * FROM marketpending
	WHERE StockId = inputStockId
		AND SellerId is null 
		AND Price >= inputPrice
	ORDER BY Price;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `closeSellMarketPendingOrders`;

DELIMITER $$

USE `SGStockMarket`$$
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
	WHERE MarketPendingId = inputMarketPendingId;

	INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
	VALUES(@stockId,@sellerId, inputBuyerId, @quantity,@price, now());

	UPDATE stock SET CurrentValue = @price WHERE StockId = @stockId;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `closeBuyMarketPendingOrders`;

DELIMITER $$

USE `SGStockMarket`$$
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
	WHERE MarketPendingId = inputMarketPendingId;

	INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
	VALUES(@stockId, inputSellerId, @buyerId, @quantity,@price, now());

	UPDATE stock SET CurrentValue = @price WHERE StockId = @stockId;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getTop5CompletedOrderByStockId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getTop5CompletedOrderByStockId`(IN inputStockId int)
BEGIN

	SELECT FORMAT(Avg(Price),2) as averagePrice
	FROM  (Select Price
		FROM marketcompleted 
		WHERE StockId = inputStockId
		ORDER BY TransactionDate desc limit 5) P;

END$$
DELIMITER ;

-- ----------------------------------------------------------
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `getQuantityByAccountIdAndStockId`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE `getQuantityByAccountIdAndStockId`(IN inputBuyerId int, IN inputStockId int)
BEGIN

	SELECT (totalQuantity - IFNULL(soldQuantity,0)) AS Quantity
	FROM stock s
	JOIN (SELECT m.StockId, SUM(m.Quantity) AS totalQuantity
		FROM marketcompleted m WHERE m.BuyerId = inputBuyerId GROUP BY m.StockId) total 
			ON total.StockId = s.StockId
	LEFT JOIN (SELECT m.StockId, SUM(m.Quantity) AS soldQuantity
		FROM marketcompleted m WHERE m.SellerId = inputBuyerId GROUP BY m.StockId) sold 
			ON sold.StockId = total.StockId
	WHERE s.StockId = inputStockId;

END$$
DELIMITER ;

-- USE this to generate dummy data after create the table
USE `SGStockMarket`;
DROP PROCEDURE IF EXISTS `InsertMarketCompletedData`;

DELIMITER $$
USE `SGStockMarket`$$

CREATE PROCEDURE InsertMarketCompletedData(
)
BEGIN
	DECLARE counterid INT DEFAULT 1;
    DECLARE insertStockId int;
    DECLARE insertPrice double;
    DECLARE InsertQuantity int DEFAULT 20;
    DECLARE insertId int;
    DECLARE getTime timestamp;
    
	WHILE counterid <=(SELECT COUNT(*) FROM stock) do
		SELECT @insertStockId := StockId
        from stock
        WHERE StockId = counterid;
        
        SELECT @insertPrice := CurrentValue
        from stock
        WHERE StockId = counterid;
        SET insertId = 1;
        SET getTime = now();
        WHILE insertId <=100 do
        SET InsertQuantity = InsertQuantity + 1;
        SET @insertPrice = @insertPrice + 0.1;
		if insertId < 4 then 
        INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
		VALUES(@insertStockId,0,0,InsertQuantity,@insertPrice, getTime);
        else
        SET getTime = getTime + INTERVAL 1 minute;
        INSERT INTO marketcompleted (StockId, SellerId, BuyerId, Quantity, Price, TransactionDate)
		VALUES(@insertStockId,0,0,InsertQuantity,@insertPrice, getTime);
        END IF;
        SET insertId = insertId +1;
        END WHILE;
        SET counterid = counterid + 1;
	END WHILE;
END$$
DELIMITER ;