Drop database if exists HKStockMarket;
CREATE database HKStockMarket;
USE HKStockMarket;

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

DROP TABLE IF EXISTS `marketrow`;
CREATE TABLE `marketrow` (
  `MarketRowId` int(11) NOT NULL AUTO_INCREMENT,
  `StockId` int(11) NOT NULL,
  `IsBuying`  BOOLEAN NOT NULL,
  `Quantity` int(11) NOT NULL,
  `Price` double NOT NULL,
  PRIMARY KEY (`MarketRowId`),
  UNIQUE KEY `MarketRowId_UNIQUE` (`MarketRowId`)
 
);

DROP TABLE IF EXISTS `marketorders`;
CREATE TABLE `marketorders` (
  `MarketOrdersId` int(11) NOT NULL AUTO_INCREMENT,
  `StockId` int(11) NOT NULL,
  `SellerId` int(11) DEFAULT NULL,
  `BuyerId` int(11) DEFAULT NULL,
  `Quantity` int(11) NOT NULL,
  `MarketRowId` int(11) NOT NULL,
  `Price` double NOT NULL,
  `ConfirmedPrice` double NOT NULL,
  `IsClosed`  BOOLEAN NOT NULL,
  `TransactionDate` datetime NOT NULL,
  PRIMARY KEY (`MarketOrdersId`),
  UNIQUE KEY `MarketOrdersId_UNIQUE` (`MarketOrdersId`),
  CONSTRAINT FK_StockId  FOREIGN KEY(StockId)  REFERENCES stock(StockId) ON DELETE CASCADE,
  CONSTRAINT FK_MarketRowId  FOREIGN KEY(MarketRowId)  REFERENCES marketrow(MarketRowId) ON DELETE CASCADE
);



/*data*/
/*Status true = open, false = closed */
INSERT INTO `stock` VALUES (1,'MGM China Holdings Ltd','HKG: 2282',12.38,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'The People Insurance Co (Group) of China Ord Shs H','HKG: 1339',2.76,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'HAITONG Securities Company Limited','SHA: 600837',11.53,false,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'Shandong Weigao Group Medical Polymer Co','HKG: 1066',19.80,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'AK Medical Holdings Ltd','HKG: 1789',15.70,true,'GMT+8',current_timestamp());
Select * from stock;

INSERT INTO `marketrow` VALUES (1,1,true,12,12.12);
INSERT INTO `marketrow` VALUES (null,1,true,12,12.38);
INSERT INTO `marketrow` VALUES (null,2,false,14,14.48);
INSERT INTO `marketrow` VALUES (null,3,false,13,13.36);
INSERT INTO `marketrow` VALUES (null,3,false,13,13.47);
Select * from marketrow;


INSERT INTO `marketorders` VALUES (1,1,4,null,12,1,12.38,13.38,true,current_timestamp());
INSERT INTO `marketorders` VALUES (null,1,3,null,12,1,12.38,13.38,true,current_timestamp());
INSERT INTO `marketorders` VALUES (null,2,3,null,12,1,2.76,13.38,false,current_timestamp());
INSERT INTO `marketorders` VALUES (null,3,4,null,12,1,11.53,13.38,false,current_timestamp());
INSERT INTO `marketorders` VALUES (null,3,4,5,12,1,19.80,19.38,false,current_timestamp());
Select * from marketorders;