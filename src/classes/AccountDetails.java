package classes;

public class AccountDetails {
	private int accountId;
	private String userName;
	private String password;
	private String email;
	private float totalAccountValue;
	private float totalSecurityValue;
	private float availableCash;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public float getTotalAccountValue() {
		return totalAccountValue;
	}

	public void setTotalAccountValue(float totalAccountValue) {
		this.totalAccountValue = totalAccountValue;
	}

	public float getTotalSecurityValue() {
		return totalSecurityValue;
	}

	public void setTotalSecurityValue(float totalSecurityValue) {
		this.totalSecurityValue = totalSecurityValue;
	}

	public float getAvailableCash() {
		return availableCash;
	}

	public void setAvailableCash(float availableCash) {
		this.availableCash = availableCash;
	}

}
