USE AccountDetailsServer;  
DROP procedure IF EXISTS `getAccountDetailsByUsernameAndPw`;

DELIMITER $$

CREATE PROCEDURE getAccountDetailsByUsernameAndPw(IN inputUsername varchar(50), IN inputPassword varchar(50))
BEGIN
	SELECT * FROM account WHERE userName = inputUsername AND password = inputPassword;
END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;  
DROP procedure IF EXISTS `getAccountDetailsById`;

DELIMITER $$

CREATE PROCEDURE getAccountDetailsById(IN inputId INT)
BEGIN
	SELECT * FROM account WHERE accountId = inputId;
END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;
DROP procedure IF EXISTS `getAccountHoldingsById`;

DELIMITER $$

CREATE PROCEDURE getAccountHoldingsById(IN accountInputId INT)
BEGIN
	SELECT * FROM account WHERE accountId = accountInputId;
END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;
DROP procedure IF EXISTS `updatePurchaseInAccount`;

DELIMITER $$

CREATE PROCEDURE updatePurchaseInAccount(IN accountInputId INT , IN purchaseValue INT)
BEGIN
	Update account SET availableCash = availableCash - purchaseValue WHERE accountId = accountInputId;
	Update account SET totalSecurityValue = totalSecurityValue + purchaseValue WHERE accountId = accountInputId;

END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;
DROP procedure IF EXISTS `updateSaleInAccount`;

DELIMITER $$

CREATE PROCEDURE updateSaleInAccount(IN accountInputId INT , IN purchaseValue INT)
BEGIN
	Update account SET availableCash = availableCash + purchaseValue WHERE accountId = accountInputId;
	Update account SET totalSecurityValue = totalSecurityValue - purchaseValue WHERE accountId = accountInputId;

END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;
DROP procedure IF EXISTS `addAccountAvailableCash`;

DELIMITER $$

CREATE PROCEDURE addAccountAvailableCash(IN accountInputId INT , IN accountValueAdd INT)
BEGIN
	Update account SET availableCash = availableCash + accountValueAdd WHERE accountId = accountInputId;
END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;
DROP procedure IF EXISTS `minusAccountAvailableCash`;

DELIMITER $$

CREATE PROCEDURE minusAccountAvailableCash(IN accountInputId INT , IN accountValueMinus INT)
BEGIN
	Update account SET availableCash = availableCash - accountValueMinus WHERE accountId = accountInputId;
END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;
DROP procedure IF EXISTS `addAccountSecurityValue`;

DELIMITER $$

CREATE PROCEDURE addAccountSecurityValue(IN accountInputId INT , IN accountValueAdd INT)
BEGIN
	Update account SET totalSecurityValue = totalSecurityValue + accountValueAdd WHERE accountId = accountInputId;
END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;
DROP procedure IF EXISTS `minusAccountSecurityValue`;

DELIMITER $$

CREATE PROCEDURE minusAccountSecurityValue(IN accountInputId INT , IN accountValueMinus INT)
BEGIN
	Update account SET totalSecurityValue = totalSecurityValue - accountValueMinus WHERE accountId = accountInputId;
END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;
DROP procedure IF EXISTS `addAccountValue`;

DELIMITER $$

CREATE PROCEDURE addAccountValue(IN accountInputId INT , IN accountValueAdd INT)
BEGIN
	Update account SET totalAccountValue = totalAccountValue + accountValueAdd WHERE accountId = accountInputId;
END $$

DELIMITER ;

-- ----------------------------------------------------------
USE AccountDetailsServer;
DROP procedure IF EXISTS `minusAccountValue`;

DELIMITER $$

CREATE PROCEDURE minusAccountValue(IN accountInputId INT , IN accountValueMinus INT)
BEGIN
	Update account SET totalAccountValue = totalAccountValue - accountValueMinus WHERE accountId = accountInputId;
END $$

DELIMITER ;