package myproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

// Connection with database
public class DAO {
    private Connection con; // Connection object

    public DAO() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/polling_system", "root", "Sej2004");
            System.out.println("connected");
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("loaded");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Do not close the connection in every method; keep the connection open until the application ends
    public void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Connection closed");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Updated insertVoter method to specify column names, excluding the auto-increment 'id' column
    public void insertVoter(String fname, String lname, String gender, String pno, String prn, String password) throws VoteException {
        try {
            int id = 0;
            Statement st = con.createStatement();
            String q = "SELECT * FROM regsitration ORDER BY voter_id DESC LIMIT 1"; // Retrieving ID of the last voter
            ResultSet rs = st.executeQuery(q);
            if (rs.next()) {
                id = rs.getInt("voter_id");
                id++;
            }

            // Assuming 'id' is auto-increment, we exclude it from the insert query
            String query = "INSERT INTO regsitration (first_name, last_name, gender, phone_number, prn, password) VALUES(?,?,?,?,?,?)";
            PreparedStatement ps1 = con.prepareStatement(query);
            ps1.setString(1, fname);
            ps1.setString(2, lname);
            ps1.setString(3, gender);
            ps1.setString(4, pno);
            ps1.setString(5, prn);
            ps1.setString(6, password);
            int result= ps1.executeUpdate();
            if(result>0)
            {
            	System.out.println("User registered successfully! "+ result);
            }else
            {
            	System.out.println("*******************result"+result);
            }

        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new VoteException();
        }
    }

    // Updated selectUser method
    public void selectUser(String prn, String password) throws VoteException {
        try {
            // Verify that 'prn' exists as a valid column in the registration table
            String query = "SELECT * FROM Regsitration WHERE prn=? AND password=?";
            PreparedStatement ps1 = con.prepareStatement(query);
            ps1.setString(1, prn);
            ps1.setString(2, password);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) { // If record not present
                throw new VoteException();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new VoteException();
        }
    }

    public int selectAdmin(String prn, String password) throws VoteException {
        try {
            String query = "SELECT * FROM admin WHERE username=? AND password=?";
            PreparedStatement ps1 = con.prepareStatement(query);
            ps1.setString(1, prn);
            ps1.setString(2, password);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) { // If admin not found
                return 0;
            }
            return 1;
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public void insertVote(int vote) throws VoteException {
        try {
            String query = "INSERT INTO votes(optid) VALUES(?)";
            PreparedStatement ps1 = con.prepareStatement(query);
            ps1.setInt(1, vote);
            int result=ps1.executeUpdate();
            if(result>0)
            	System.out.println("vote added in DB successfully!!");
            
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new VoteException();
        }
    }

    public int getVotes(int opt_id) {
        try {
            String query = "SELECT COUNT(*) FROM votes WHERE optid=" + opt_id;
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public String getWinner() {
        try {
            String query = "SELECT optid, COUNT(*) AS vote_count FROM votes GROUP BY optid ORDER BY vote_count DESC LIMIT 1";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.next();
            int woptid = rs.getInt("optid");

            // Get the winner option name
            String query1 = "SELECT optname FROM options WHERE optid = " + woptid;
            ResultSet rs1 = st.executeQuery(query1);
            rs1.next();
            return rs1.getString("optname");

        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public int maxCount()
    {
    	int count=0;
    String query=  " select optid,count(optid) as count from votes group by optid order by 2 desc limit 1";
    
    Statement st;
	try {
		st = con.createStatement();
		ResultSet rs = st.executeQuery(query);
		while(rs.next())
		{
			
			count=rs.getInt(2);
		}
		
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   
    	return count;
    }
}
