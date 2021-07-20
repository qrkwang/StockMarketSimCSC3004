create database if not exists AccountDetailsServer;
USE AccountDetailsServer;

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `accountId` int NOT NULL AUTO_INCREMENT,
  `userName` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `totalAccountValue` decimal(10,0) NOT NULL,
  `totalSecurityValue` decimal(10,0) NOT NULL,
  `availableCash` decimal(10,0) NOT NULL,
PRIMARY KEY (`accountId`),
  UNIQUE KEY `AccountId_UNIQUE` (`accountId`)
) ;

INSERT INTO `account` VALUES (1,'testing','password','testing@hotmail.com',6000,0,6000);
INSERT INTO `account` VALUES (null, 'demo','password','demo@hotmail.com',6000,0,6000);
INSERT INTO `account` VALUES (null, 'demo1','password','demo1@hotmail.com',6000,0,6000);
INSERT INTO `account` VALUES (null, 'demo2','password','demo2@hotmail.com',6000,0,6000);
INSERT INTO `account` VALUES (null, 'demo3','password','demo3@hotmail.com',6000,0,6000);
