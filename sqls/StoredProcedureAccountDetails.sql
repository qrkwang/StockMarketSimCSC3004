USE AccountDetailsServer;  
DROP procedure IF EXISTS `getAccountDetailsByUsernameAndPw`;

DELIMITER $$

-- USE AccountDetailsServer;
CREATE PROCEDURE getAccountDetailsByUsernameAndPw(IN inputUsername varchar(50), IN inputPassword varchar(50))
BEGIN
	SELECT * FROM account WHERE userName = inputUsername AND password = inputPassword;
END $$

DELIMITER ;


USE AccountDetailsServer;
DROP procedure IF EXISTS `getAccountHoldingsById`;

DELIMITER $$

#USE AccountDetailsServer;  uncomment if need

CREATE PROCEDURE getAccountHoldingsById(IN accountInputId INT)
BEGIN
	SELECT * FROM account WHERE accountId = accountInputId;
END $$

DELIMITER ;


USE AccountDetailsServer;
DROP procedure IF EXISTS `addAccountAvailableCash`;

DELIMITER $$

#USE AccountDetailsServer;  uncomment if need

CREATE PROCEDURE addAccountAvailableCash(IN accountInputId INT , IN accountValueAdd INT)
BEGIN
	Update account SET availableCash = availableCash + accountValueAdd WHERE accountId = accountInputId;
END $$

DELIMITER ;


USE AccountDetailsServer;
DROP procedure IF EXISTS `minusAccountAvailableCash`;

DELIMITER $$

#USE AccountDetailsServer;  uncomment if need

CREATE PROCEDURE minusAccountAvailableCash(IN accountInputId INT , IN accountValueMinus INT)
BEGIN
	Update account SET availableCash = availableCash - accountValueMinus WHERE accountId = accountInputId;
END $$

DELIMITER ;


USE AccountDetailsServer;
DROP procedure IF EXISTS `addAccountSecurityValue`;

DELIMITER $$

-- USE AccountDetailsServer;  

CREATE PROCEDURE addAccountSecurityValue(IN accountInputId INT , IN accountValueAdd INT)
BEGIN
	Update account SET totalSecurityValue = totalSecurityValue + accountValueAdd WHERE accountId = accountInputId;
END $$

DELIMITER ;


USE AccountDetailsServer;
DROP procedure IF EXISTS `minusAccountSecurityValue`;

DELIMITER $$

#USE AccountDetailsServer;  uncomment if need

CREATE PROCEDURE minusAccountSecurityValue(IN accountInputId INT , IN accountValueMinus INT)
BEGIN
	Update account SET totalSecurityValue = totalSecurityValue - accountValueMinus WHERE accountId = accountInputId;
END $$

DELIMITER ;


USE AccountDetailsServer;
DROP procedure IF EXISTS `addAccountValue`;

DELIMITER $$

#USE AccountDetailsServer;  uncomment if need

CREATE PROCEDURE addAccountValue(IN accountInputId INT , IN accountValueAdd INT)
BEGIN
	Update account SET totalAccountValue = totalAccountValue + accountValueAdd WHERE accountId = accountInputId;
END $$

DELIMITER ;


USE AccountDetailsServer;
DROP procedure IF EXISTS `minusAccountValue`;

DELIMITER $$

#USE AccountDetailsServer;  uncomment if need

CREATE PROCEDURE minusAccountValue(IN accountInputId INT , IN accountValueMinus INT)
BEGIN
	Update account SET totalAccountValue = totalAccountValue - accountValueMinus WHERE accountId = accountInputId;
END $$

DELIMITER ;