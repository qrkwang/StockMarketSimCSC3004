Drop database if exists SGStockMarket;
CREATE database SGStockMarket;
USE SGStockMarket;

DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
  `StockId` int(11) NOT NULL AUTO_INCREMENT,
  `CompanyName` varchar(100) NOT NULL,
  `TickerSymbol` varchar(50) NOT NULL,
  `CurrentValue` double NOT NULL,
  `Status`  BOOLEAN NOT NULL,
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
INSERT INTO `stock` VALUES (1,'UOB','SGX: U11',25.69,true,'GMT+8 ',current_timestamp());
INSERT INTO `stock` VALUES (null,'Singapore Airline','SGX: C6L',4.98,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'Sheng Shiong','SGX: OV8',1.57,false,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'Oversea-Chinese Banking Corp. Limited','SGX: O39',11.80,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'Singapore Telecommunications Limited','SGX: Z74',2.30,true,'GMT+8',current_timestamp());
Select * from stock;

INSERT INTO `marketpending` VALUES (1,1,1,null,12,12.38,current_timestamp());
INSERT INTO `marketpending` VALUES (null,1,null,4,12,12.38,current_timestamp());
INSERT INTO `marketpending` VALUES (null,2,3,null,12,2.76,current_timestamp());
INSERT INTO `marketpending` VALUES (null,3,4,null,12,11.53,current_timestamp());
INSERT INTO `marketpending` VALUES (null,3,null,5,12,19.80,current_timestamp());
Select * from marketpending;

INSERT INTO `marketcompleted` VALUES (1,1,1,2,12,12.38,current_timestamp());
INSERT INTO `marketcompleted` VALUES (null,1,3,4,12,12.38,current_timestamp());
INSERT INTO `marketcompleted` VALUES (null,2,3,4,12,2.76,current_timestamp());
INSERT INTO `marketcompleted` VALUES (null,3,4,3,12,11.53,current_timestamp());
INSERT INTO `marketcompleted` VALUES (null,3,5,5,12,19.80,current_timestamp());
Select * from marketcompleted;