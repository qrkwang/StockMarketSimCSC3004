#USE AccountDetailsServer;  
DROP procedure IF EXISTS `getAccountDetailsByUsername`;

DELIMITER $$

#USE AccountDetailsServer;
CREATE PROCEDURE getAccountDetailsByUsername(IN inputUsername varchar (50))
BEGIN
	SELECT * FROM account WHERE userName = inputUsername;
END $$

DELIMITER ;


#USE AccountDetailsServer;
DROP procedure IF EXISTS `getAccountHoldingsById`;

DELIMITER $$

#USE AccountDetailsServer;  uncomment if need

CREATE PROCEDURE getAccountHoldingsById(IN accountInputId INT)
BEGIN
	SELECT * FROM account WHERE accountId = accountInputId;
END $$

DELIMITER ;