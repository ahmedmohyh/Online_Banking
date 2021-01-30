import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.*;

public class Connectionhandler implements Runnable {
    static String url = "jdbc:mysql://localhost:3306/banking";
    static String username = "root";
    static  String password = "253500";
    static Connection connection = null ;
    private Socket socket;
    public Connectionhandler(Socket socket) throws SQLException {
        connection = DriverManager.getConnection(url, username,password);
        this.socket = socket;
    }

    @Override
    public void run() {
        try(
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        ObjectInputStream ois = new ObjectInputStream(is)){
            while (true){
                My_actions.myactions actions =(My_actions.myactions)ois.readObject();
                if (actions.equals(My_actions.myactions.Loginrequest)){
                    String msg = (String)ois.readObject();
                    if (msg.equals("wrong")){
                        break;
                    }
                    else {
                        System.out.println("Request Login Received");
                        String user = (String) ois.readObject();
                        String pass = (String) ois.readObject();
                        PreparedStatement psmt = connection.prepareStatement("select * from banking.data where CustomerID = " + user + " ;");
                        ResultSet rs = psmt.executeQuery();
                        int id = 0;
                        String passrd = "";
                        String s = "";
                        while (rs.next()) {
                            id = rs.getInt("CustomerID");
                            passrd = rs.getString("Password");
                        }
                        oos.writeObject(My_actions.myactions.loginresponse);
                        if (id != 0 && passrd.equals(pass)) {
                            s = "You are logged in Successfully";
                            oos.writeObject(s);

                        } else {
                            s = "Unathorithed Cardinalties";
                            oos.writeObject(s);
                        }
                        break;
                    }
                }
                else if (actions.equals(My_actions.myactions.Balancerequest)){
                    double sal = 0;
                    System.out.println("I got here");
                    String user = (String)ois.readObject();
                    PreparedStatement psmt = connection.prepareStatement("select Saldo from banking.balance where Custid = "+user+ " ;");
                    ResultSet rs= psmt.executeQuery();
                    while (rs.next()){
                        sal = rs.getDouble("Saldo");
                        System.out.println(sal);
                    }
                    String re = Double.toString(sal);
                    oos.writeObject(My_actions.myactions.Balanceresponse);
                    oos.writeObject(re);
                    break;

                }
                else if (actions.equals(My_actions.myactions.logoutrequest)){
                    System.out.println("Request Logout Received");
                    oos.writeObject(My_actions.myactions.logoutresponse);
                    String s = "You are logged out successfully";
                    oos.writeObject(s);
                    break;
                }
                else if (actions.equals(My_actions.myactions.Sendmoneyrequest)){
                    String first = (String)ois.readObject();
                    if (first.equals("wrong")){
                        break;
                    }
                    String final_message = "";
                    System.out.println("Request Sending money Received");
                    String rece = (String)ois.readObject();
                    String sender = (String)ois.readObject();
                    double to_trans = 0;
                    String to_t = (String)ois.readObject();
                    to_trans = Double.parseDouble(to_t);
                    PreparedStatement psmt  = connection.prepareStatement("select * from banking.data where CustomerID = "+rece+ " ;");
                    ResultSet rs= psmt.executeQuery();
                    int id = 0;
                    String passrd ="";
                    while (rs.next()){
                        id     =   rs.getInt("CustomerID");
                        passrd = rs.getString("Password");
                    }
                    oos.writeObject(My_actions.myactions.Sendmoneyresponse);
                    if (id != 0){
                        psmt = connection.prepareStatement("update balance SET Saldo = Saldo + ? where Custid = "+rece+" ;");
                        psmt.setString(1,to_t);
                        psmt.executeUpdate();
                        psmt = connection.prepareStatement("update balance SET Saldo = Saldo - ? where Custid = "+sender+" ;");
                        psmt.setString(1,to_t);
                        psmt.executeUpdate();
                        final_message = "The money was sent successfully";
                        oos.writeObject(final_message);
                    }else{
                        final_message = "You are entering a wrong receiver number";
                        oos.writeObject(final_message);
                    }
                    break;
                }
                else {
                    break;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
