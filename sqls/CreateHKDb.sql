Drop database if exists HKStockMarket;
CREATE database HKStockMarket;
USE HKStockMarket;

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
INSERT INTO `stock` VALUES (1,'MGM China Holdings Ltd','HKG: 2282',12.38,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'The People Insurance Co (Group) of China Ord Shs H','HKG: 1339',2.76,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'HAITONG Securities Company Limited','SHA: 600837',11.53,false,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'Shandong Weigao Group Medical Polymer Co','HKG: 1066',19.80,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'AK Medical Holdings Ltd','HKG: 1789',15.70,true,'GMT+8',current_timestamp());
Select * from stock;

INSERT INTO `marketdata` VALUES (1,1,4,null,12,12.38,current_timestamp());
INSERT INTO `marketdata` VALUES (null,1,3,null,12,12.38,current_timestamp());
INSERT INTO `marketdata` VALUES (null,2,3,null,12,2.76,current_timestamp());
INSERT INTO `marketdata` VALUES (null,3,4,null,12,11.53,current_timestamp());
INSERT INTO `marketdata` VALUES (null,3,4,5,12,19.80,current_timestamp());
Select * from marketdata;