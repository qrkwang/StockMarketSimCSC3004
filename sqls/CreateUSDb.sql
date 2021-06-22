Drop database if exists USStockMarket;
CREATE database USStockMarket;
USE USStockMarket;

DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
  `StockId` int(11) NOT NULL AUTO_INCREMENT,
  `CompanyName` varchar(100) NOT NULL,
  `TickerSymbol` varchar(50) NOT NULL,
  `CurrentValue` decimal(10,0) NOT NULL,
  `Status`  BOOLEAN,
  `Timezone` varchar(50) NOT NULL,
  `CreatedDate` datetime NOT NULL,
  PRIMARY KEY (`StockId`),
  UNIQUE KEY `StockId_UNIQUE` (`StockId`)
);
DROP TABLE IF EXISTS `marketdata`;
CREATE TABLE `marketdata` (
  `MarketDataId` int(11) NOT NULL AUTO_INCREMENT,
  `StockId` int(11) NOT NULL,
  `SellerId` int(11) DEFAULT NULL,
  `BuyerId` int(11) DEFAULT NULL,
  `Quantity` int(11) NOT NULL,
  `Price` decimal(10,2) NOT NULL,
  `TransactionDate` datetime NOT NULL,
  PRIMARY KEY (`MarketDataId`),
  UNIQUE KEY `MarketDataId_UNIQUE` (`MarketDataId`),
  CONSTRAINT FK_StockId  FOREIGN KEY(StockId)  REFERENCES stock(StockId) ON DELETE CASCADE
);

/*data*/
/*Status true = open, false = closed */
INSERT INTO `stock` VALUES (1,'Apple','NASDAQ: AAPL',132.30,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'Twitter','NYSE: TWTR',61.96,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'Facebook','NASDAQ: FB',333.14,false,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'Netflix','NASDAQ: NFLX',498.01,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'Walt Disney Co','NYSE: DIS: AAPL',174.30,true,'GMT-4',current_timestamp());
Select * from stock;

INSERT INTO `marketdata` VALUES (1,1,4,null,12,132.2,current_timestamp());
INSERT INTO `marketdata` VALUES (null,1,3,null,12,132.2,current_timestamp());
INSERT INTO `marketdata` VALUES (null,2,3,null,12,62,current_timestamp());
INSERT INTO `marketdata` VALUES (null,3,4,null,12,333.14,current_timestamp());
INSERT INTO `marketdata` VALUES (null,3,4,5,12,333.14,current_timestamp());
Select * from marketdata;