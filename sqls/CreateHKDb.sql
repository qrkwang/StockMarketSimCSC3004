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
INSERT INTO `stock` VALUES (1,'MGM China Holdings Ltd','HKG: 2282',12.38,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'The People Insurance Co (Group) of China Ord Shs H','HKG: 1339',2.76,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'HAITONG Securities Company Limited','SHA: 600837',11.53,false,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'Shandong Weigao Group Medical Polymer Co','HKG: 1066',19.80,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'AK Medical Holdings Ltd','HKG: 1789',15.70,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'WUXI BIOLOGICS (CAYMAN) INC','HKG: 2269',139.900,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'DONGYUE GROUP LTD.','HKG: 189',11.30,false,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'HSBC HOLDINGS PLCo','HKG: 5',43.750,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'XIAOMI CORPORATION - W','HKG: 1810',28.250,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'AIA GROUP LTD','HKG: 1299',96.150,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'GREAT WALL MOTOR CO. LTD. - H SHARES','HKG: 2333',27.850,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'CHINA EVERGRANDE GROUP','HKG: 3333',9.800,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'ZHUZHOU CRRC TIMES ELECTRIC CO., LTD','HKG: 3898',55.500,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'TENCENT HOLDINGS LTD','HKG: 700',564.000,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'CHINA MOBILE LTD','HKG: 941',48.050,false,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'SUNNY OPTICAL TECHNOLOGY (GROUP) CO. LTD.','HKG: 2382',226.000,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'KINDSTAR GLOBALGENE TECHNOLOGY','HKG: 9960',9.040,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'HAIDILAO INTERNATIONAL HOLDING LTDH','HKG: 6862',44.800,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'INDUSTRIAL AND COMMERCIAL BANK OF CHINA LTD','HKG: 1398',4.480,false,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'BANK OF CHINA LTD','HKG: 3988',2.760,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'KINGSOFT CORPORATION LTD.','HKG: 3888',42.650,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'CHINA TELECOM CORPORATION LTD','HKG: 728',2.980,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'WEIMOB INC.','HKG: 2013',12.740,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'ZHONGAN ONLINE P & C INSURANCE CO., LTD','HKG: 47.300',19.80,true,'GMT+8',current_timestamp());
INSERT INTO `stock` VALUES (null,'ALIBABA GROUP HOLDING LTD','HKG: 9988',209.400,true,'GMT+8',current_timestamp());

Select * from stock;

INSERT INTO `marketpending` VALUES (1,1,1,null,12,12.38,current_timestamp());
INSERT INTO `marketpending` VALUES (null,1,null,4,12,12.38,current_timestamp());
INSERT INTO `marketpending` VALUES (null,2,3,null,12,2.76,current_timestamp());
INSERT INTO `marketpending` VALUES (null,3,4,null,12,11.53,current_timestamp());
INSERT INTO `marketpending` VALUES (null,3,null,5,12,19.80,current_timestamp());
Select * from marketpending;

-- INSERT INTO `marketcompleted` VALUES (1,1,1,2,12,12.38,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,1,3,4,12,12.38,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,2,3,4,12,2.76,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,3,4,3,12,11.53,current_timestamp());
-- INSERT INTO `marketcompleted` VALUES (null,3,5,5,12,19.80,current_timestamp());
-- Select * from marketcompleted;