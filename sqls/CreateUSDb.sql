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
DROP TABLE IF EXISTS `marketpending`;
CREATE TABLE `marketpending` (
  `MarketPendingId` int(11) NOT NULL AUTO_INCREMENT,
  `StockId` int(11) NOT NULL,
  `SellerId` int(11) DEFAULT NULL,
  `BuyerId` int(11) DEFAULT NULL,
  `Quantity` int(11) NOT NULL,
  `Price` double NOT NULL,
  `CreatedDate` datetime NOT NULL,
  PRIMARY KEY (`MarketPendingId`),
  UNIQUE KEY `MarketPendingId_UNIQUE` (`MarketPendingId`),
  CONSTRAINT FK_PendingStockId  FOREIGN KEY(StockId)  REFERENCES stock(StockId) ON DELETE CASCADE
);

DROP TABLE IF EXISTS `marketcompleted`;
CREATE TABLE `marketcompleted` (
  `MarketCompletedId` int(11) NOT NULL AUTO_INCREMENT,
  `StockId` int(11) NOT NULL,
  `SellerId` int(11) DEFAULT NULL,
  `BuyerId` int(11) DEFAULT NULL,
  `Quantity` int(11) NOT NULL,
  `Price` double NOT NULL,
  `TransactionDate` datetime NOT NULL,
  PRIMARY KEY (`MarketCompletedId`),
  UNIQUE KEY `MarketCompletedId_UNIQUE` (`MarketCompletedId`),
  CONSTRAINT FK_CompletedStockId  FOREIGN KEY(StockId)  REFERENCES stock(StockId) ON DELETE CASCADE
);


/*data*/
/*Status true = open, false = closed */
INSERT INTO `stock` VALUES (1,'Apple','NASDAQ: AAPL',132.30,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'Twitter','NYSE: TWTR',61.96,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'Facebook','NASDAQ: FB',333.14,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'Netflix','NASDAQ: NFLX',498.01,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'Walt Disney Co','NYSE: DIS',174.30,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'ABSOLUTE SOFTWARE CORPORATION','NASDAQ: ABST',13.55,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'AMESITE INC','NASDAQ: AMST',2.11,false,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'ACCOLADE, INC','NASDAQ: ACCD',46.00,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'ACE GLOBAL BUSINESS ACQUISITION LIMITED','NASDAQ: ACBA',10.05,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'BILL.COM HOLDINGS, INC','NYSE: BILL',182.00,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'BLUECITY HOLDINGS LIMITED','NASDAQ: BLCT',7.16,false,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'BIO-KEY INTERNATIONAL, INC','NASDAQ: BKYI',3.13,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'BERKELEY LIGHTS, INC','NASDAQ: BLI',44.68,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'COGNYTE SOFTWARE LTD','NASDAQ: CGNT',23.96,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'CLEANSPARK, INC','NASDAQ: CLSK',13.75,false,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'COURSERA, INC','NYSE: NFLX',38.14,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'CREATIVE REALITIES, INC','NASDAQ:CREX',1.71,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'DOORDASH, INC','NYSE: DASH',167.36,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'DOCEBO INC','NASDAQ: DCBO',59.96,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'DHI GROUP INC','NYSE: DHX',498.01,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'DUN & BRADSTREET HOLDINGS, INC','NYSE: DNB',21.00,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'DOMO, INC','NASDAQ: DOMO',81.56,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'CHANNELADVISOR CORPORATION','NYSE: ECOM',22.77,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'DIGINEX LIMITED','NASDAQ: EQOS',5.48,true,'GMT-4',current_timestamp());
INSERT INTO `stock` VALUES (null,'EVERQUOTE, INC','NASDAQ: EVER',29.25,true,'GMT-4',current_timestamp());


Select * from stock;

INSERT INTO `marketpending` VALUES (1,1,1,null,12,12.38,current_timestamp());
INSERT INTO `marketpending` VALUES (null,1,null,4,12,12.38,current_timestamp());
INSERT INTO `marketpending` VALUES (null,2,3,null,12,2.76,current_timestamp());
INSERT INTO `marketpending` VALUES (null,3,4,null,12,11.53,current_timestamp());
INSERT INTO `marketpending` VALUES (null,3,null,5,12,19.80,current_timestamp());
Select * from marketpending;

-- INSERT INTO `marketcompleted` VALUES (1,1,1,2,12,12.38,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,1,3,4,12,12.38,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,1,2,4,15,12.40,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,1,2,4,16,12.21,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,1,3,4,19,12.42,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,1,2,3,16,12.36,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,1,1,2,14,12.50,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,1,4,5,14,12.70,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,2,5,4,12,2.76,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,2,3,4,12,2.76,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,2,3,4,12,2.76,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,2,3,4,12,2.76,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,2,3,4,12,2.76,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,2,3,4,12,2.76,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,2,3,4,12,2.76,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,3,4,3,12,11.53,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,3,5,5,12,19.80,current_timestamp());
-- Select * from marketcompleted;