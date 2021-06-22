Drop database if exists SGStockMarket;
CREATE database SGStockMarket;
USE SGStockMarket;

DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
  `StockId` int(11) NOT NULL AUTO_INCREMENT,
  `CompanyName` varchar(100) NOT NULL,
  `TickerSymbol` varchar(50) NOT NULL,
  `CurrentValue` decimal(10,0) NOT NULL,
  `Status`  BOOLEAN NOT NULL,
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
INSERT INTO `stock` VALUES (1,'UOB','SGX: U11',25.69,true,'GMT+8 ',current_timestamp());
INSERT INTO `stock` VALUES (null,'Singapore Airline','SGX: C6L',4.98,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'Sheng Shiong','SGX: OV8',1.57,false,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'Oversea-Chinese Banking Corp. Limited','SGX: O39',11.80,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'Singapore Telecommunications Limited','SGX: Z74',2.30,true,'GMT+8',current_timestamp());
Select * from stock;

INSERT INTO `marketdata` VALUES (1,1,4,null,12,25.69,current_timestamp());
INSERT INTO `marketdata` VALUES (null,1,3,null,12,25.69,current_timestamp());
INSERT INTO `marketdata` VALUES (null,2,3,null,12,4.98,current_timestamp());
INSERT INTO `marketdata` VALUES (null,3,4,null,12,11.80,current_timestamp());
INSERT INTO `marketdata` VALUES (null,3,4,5,12,11.81,current_timestamp());
Select * from marketdata;