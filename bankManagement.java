package bankingApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;

public class bankManagement {


	private static final int NULL=0;
	private static final int DEPOSIT_LIMIT = 2_000_000;
	private static final int WITHDRAW_LIMIT = 50_000;
	static Connection con=connection.getConnection();


	public static boolean createAccount(String name,int passCode) {

		if(name.isEmpty()||passCode==NULL) {
			System.out.println("The Details are missing Please Enter the All required details");
			return false;
		}

		try {
			String query="INSERT INTO customer (cname,balance,pass_code) values(?,1000,?)";
			PreparedStatement ps=con.prepareStatement(query);
			ps.setString(1, name);
			ps.setInt(2, passCode);
			int row=ps.executeUpdate();
			if (row == 1) {
				System.out.println("Account created successfully!.");
				return true;
			}
		}
		catch (SQLIntegrityConstraintViolationException e) {
			System.out.println("Username already exists! Try another one.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean loginAccount(String name,int passCode) {
		if (name.isEmpty() || passCode == NULL) {
			System.out.println("All fields are required!");
			return false;
		}
		try {
			String qury=" select ac_no,cname,balance from CUSTOMER WHERE cname=? AND pass_code=?";
			PreparedStatement ps=con.prepareStatement(qury);
			ps.setString(1,name);
			ps.setInt(2,passCode);
			ResultSet rs=ps.executeQuery();

			BufferedReader sc = new BufferedReader(new InputStreamReader(System.in));

			if (rs.next()) {
				int senderAc = rs.getInt("ac_no");
				int ch;

				while (true) {
					System.out.println("\n Hello, " + rs.getString("cname") + "! What would you like to do?");
					System.out.println("1) Transfer Money");
					System.out.println("2) View Balance");
					System.out.println("3) Deposit");
					System.out.println("4) WithDraw Amount");
					System.out.println("5) Logout");

					System.out.print("Enter Choice: ");
					ch = Integer.parseInt(sc.readLine());

					if (ch == 1) {
						System.out.print("Enter Receiver A/c No: ");
						int receiverAc = Integer.parseInt(sc.readLine());
						System.out.print("Enter Amount: ");
						int amt = Integer.parseInt(sc.readLine());

						if (transferMoney(senderAc, receiverAc, amt)) {
							System.out.println("Transaction successful!");
						} else {
							System.out.println("Transaction failed! Please try again.");
						}
					} else if (ch == 2) {
						getBalance(senderAc);
					} else if (ch == 3) {
						 System.out.print("Enter Amount to Deposit: ");
	                        int amount = Integer.parseInt(sc.readLine());
	                        if (deposit(senderAc, amount)) {
	                            System.out.println("₹" + amount + " deposited successfully.");
	                        }
					} 
					else if (ch == 4) {
						 System.out.print("Enter Amount to Withdraw: ");
	                        int amount = Integer.parseInt(sc.readLine());
	                        if (withdraw(senderAc, amount)) {
	                            System.out.println("₹" + amount + " withdrawn successfully.");
	                        }

					} 
					else if (ch == 5) {

						System.out.println("Logged out successfully. Returning to main menu.");
						break;
					} 
					else {
						System.out.println("Invalid choice! Try again.");
					}
				}
				return true;
			} else {
				System.out.println("Invalid username or password!");
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public static boolean withdraw(int acNo, int amount) {
	    if (amount <= 0) {
	        System.out.println("Withdrawal amount must be positive!");
	        return false;
	    }

	    if (amount > WITHDRAW_LIMIT) {
	        System.out.println("Withdrawal limit exceeded! Maximum allowed: ₹" + WITHDRAW_LIMIT);
	        return false;
	    }

	    try {
	        con.setAutoCommit(false);  

	      
	        CallableStatement csCheck = con.prepareCall("{call Check_amount(?, ?)}");
	        csCheck.setInt(1, acNo);
	        csCheck.registerOutParameter(2, Types.INTEGER);
	        csCheck.execute();
	        int balance = csCheck.getInt(2);

	        if (balance < amount) {
	            System.out.println("Insufficient balance!");
	            return false;
	        }

	       
	        CallableStatement csUpdate = con.prepareCall("{call update_bal(?, ?)}");
	        csUpdate.setInt(1, acNo);
	        csUpdate.setInt(2, -amount); 
	        csUpdate.execute();

	        con.commit();  
	        return true;

	    } catch (Exception e) {
	        try {
	            con.rollback();  
	            System.out.println("Withdrawal cancelled due to an error. Transaction rolled back.");
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	        e.printStackTrace();
	    } finally {
	        try {
	            con.setAutoCommit(true);  
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    return false;
	}



	 public static boolean deposit(int acNo, int amount) {
	        if (amount <= 0) {
	            System.out.println("Deposit amount must be positive!");
	            return false;
	        }

	        if (amount > DEPOSIT_LIMIT) {
	            System.out.println("Deposit limit exceeded! Maximum allowed: ₹" + DEPOSIT_LIMIT);
	            return false;
	        }

	        try {
	        	con.setAutoCommit(false);
	            CallableStatement cs = con.prepareCall("{call update_bal(?, ?)}");
	            cs.setInt(1, acNo);
	            cs.setInt(2, amount);
	            cs.execute();
	            con.commit();
	            return true;

	        }catch (Exception e) {
				try {
					con.rollback();
					System.out.println("Deposit is Cancaled due to error.");
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
				e.printStackTrace();
			} finally {
				try {
					con.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			return false;
	    }


	public static void getBalance(int acNo) {
		try {
			String sql = "SELECT * FROM customer WHERE ac_no = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setInt(1, acNo);
			ResultSet rs = ps.executeQuery();

			System.out.println("\n-------------------------------------------------");
			System.out.printf("%12s %15s %10s\n", "Account No", "Customer Name", "Balance");

			while (rs.next()) {
				System.out.printf("%12d %15s %10d.00\n",
						rs.getInt("ac_no"),
						rs.getString("cname"),
						rs.getInt("balance"));
			}
			System.out.println("-------------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public static boolean transferMoney(int sender_ac, int receiver_ac, int amount) {
		if (receiver_ac == NULL || amount == NULL) {
			System.out.println("All fields are required!");
			return false;
		}

		try {
			con.setAutoCommit(false);


			CallableStatement cs = con.prepareCall("{call Check_amount(?, ?)}");
			cs.setInt(1, sender_ac);
			cs.registerOutParameter(2, Types.INTEGER);
			cs.execute();
			int balance = cs.getInt(2);

			if (balance < amount) {
				System.out.println("Insufficient Balance!");
				return false;
			}

			CallableStatement debit = con.prepareCall("{call update_bal(?, ?)}");
			debit.setInt(1, sender_ac);
			debit.setInt(2, -amount); 
			debit.execute();

			CallableStatement credit = con.prepareCall("{call update_bal(?, ?)}");
			credit.setInt(1, receiver_ac);
			credit.setInt(2, amount); 
			credit.execute();


			con.commit();
			System.out.println("Transfer successful!");
			return true;

		} catch (Exception e) {
			try {
				con.rollback();
				System.out.println("Transaction rolled back due to error.");
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return false;
	} 


}
