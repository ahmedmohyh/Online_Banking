
import javafx.application.Application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;


public class Clinet extends Application {
    boolean online = false;
    VBox vBox = new VBox();
    double mybalance  = 0;
    int myid = 0;
    HBox hBox = new HBox();
    HBox hBox1 = new HBox();
    HBox hBox2 = new HBox();
    Label lin  = new Label("");
    Label lout = new Label("");
    Label Sending = new Label("");
    Text CID = new Text("Custmer ID");
    TextField CIDF = new TextField("");
    Text password = new Text("Password");
    TextField PWF =  new TextField("");
    Button Login = new Button("Login");
    Button logout = new Button("Logout");
    Slider S= new Slider();
    Text Receiver = new Text("Receiver");
    TextField RevF = new TextField("");
    Button Send = new Button("Sendmoney");
    @Override
    public void start(Stage primaryStage) throws Exception {
        vBox.setSpacing(30);
        vBox.setAlignment(Pos.BASELINE_CENTER);
        vBox.setFillWidth(true);
        S.setMin(0);
        S.setMax(1500);
        S.setValue(500);
        S.setShowTickLabels(true);
        S.setShowTickMarks(true);
        S.showTickLabelsProperty();
        HBox.setMargin(CID,new Insets(0,20,0,0));
        HBox.setMargin(password,new Insets(0,20,0,0));
        HBox.setMargin(Receiver,new Insets(0,23,0,0));
        Login.setMaxWidth(600);
        logout.setMaxWidth(600);
        Send.setMaxWidth(600);
        Send.setLayoutX(300);
        Send.setLayoutY(400);
        hBox.getChildren().addAll(CID,CIDF);
        hBox1.getChildren().addAll(password,PWF);
        hBox2.getChildren().addAll(Receiver,RevF);
        vBox.getChildren().addAll(hBox,hBox1,Login,lin,logout,lout,S,hBox2,Send,Sending);
        handler();
        primaryStage.setTitle("Banking Client");
        primaryStage.setScene(new Scene(vBox, 600, 600));
        primaryStage.show();
    }
    void handler(){
      Login.setOnAction(event->{
          lout.setText("");
          if (online == true){
           lin.setText("You are already  logged in you have to log out first");
          } else {
              try (Socket socket = new Socket(InetAddress.getLocalHost(), 7773);
                   OutputStream os = socket.getOutputStream();
                   InputStream is = socket.getInputStream();
                   ObjectOutputStream oos = new ObjectOutputStream(os);
                   ObjectInputStream ois = new ObjectInputStream(is)) {
                  String user = CIDF.getText();
                  String pass = PWF.getText();
                  if (user.equals("") || pass.equals("")){
                     lin.setText("please enter password and/OR ID");
                  } else {
                      oos.writeObject(My_actions.myactions.Loginrequest);
                      oos.writeObject(user);
                      oos.writeObject(pass);
                      My_actions.myactions action = (My_actions.myactions) ois.readObject();
                      if (action.equals(My_actions.myactions.loginresponse)) {
                          String s = (String) ois.readObject();
                          lin.setText(s);
                          if (s.equals("You are logged in Successfully")) {
                              myid = Integer.parseInt(user);
                              online = true;
                              CIDF.setText(user);
                              PWF.setText(pass);
                             balance(user);
                          }
                      }
                  }

                //  oos.flush();
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      });
      logout.setOnAction(event -> {
          lin.setText("");
          Sending.setText("");
          if (online == false ){
              lout.setText("You are not logged in yet please log in first"); }

              else {
              try (Socket socket = new Socket(InetAddress.getLocalHost(), 7773);
                   OutputStream os = socket.getOutputStream();
                   InputStream is = socket.getInputStream();
                   ObjectOutputStream oos = new ObjectOutputStream(os);
                   ObjectInputStream ois = new ObjectInputStream(is)) {
                  oos.writeObject(My_actions.myactions.logoutrequest);
                  My_actions.myactions action = (My_actions.myactions) ois.readObject();
                  if (action.equals(My_actions.myactions.logoutresponse)) {
                      String ss = (String)ois.readObject();
                      if (ss.equals("You are logged out successfully")) {
                          online = false;
                          lout.setText("You are logged out successfully");
                          S.setValue(500);
                          CIDF.setText("");
                          PWF.setText("");
                      }
                  }

              }catch (Exception e){
                  e.printStackTrace();
              }

              }
      });
      Send.setOnAction(event ->{
          if (online == true){
              Sending.setText("");
              try (Socket socket = new Socket(InetAddress.getLocalHost(), 7773);
                   OutputStream os = socket.getOutputStream();
                   InputStream is = socket.getInputStream();
                   ObjectOutputStream oos = new ObjectOutputStream(os);
                   ObjectInputStream ois = new ObjectInputStream(is)) {
                  String first_msg = "";
                  oos.writeObject(My_actions.myactions.Sendmoneyrequest);
                  if (RevF.getText().equals("")){
                      Sending.setText("PLease enter a receiver");
                      first_msg = "wrong";
                      oos.writeObject(first_msg);
                  }
                  else{
                      double amnt = S.getValue();
                     String reveiver = RevF.getText();
                     String money="";
                     String sender ="";
                      if (amnt<= mybalance){
                          mybalance-=amnt;
                          first_msg = "right";
                          oos.writeObject(first_msg);
                          sender = Integer.toString(myid);
                          money = Double.toString(amnt);
                     //     oos.writeObject(My_actions.myactions.Sendmoneyrequest);
                          oos.writeObject(reveiver);
                          oos.writeObject(sender);
                          oos.writeObject(money);
                          My_actions.myactions action = (My_actions.myactions) ois.readObject();
                          if (action.equals(My_actions.myactions.Sendmoneyresponse)){
                              String message =  (String)ois.readObject();
                              if (message.equals("The money was sent successfully")){
                                  Sending.setText(message);
                                  S.setValue(mybalance);
                              }
                              else {
                                  Sending.setText(message);
                              }
                          }
                      }
                      else {
                          Sending.setText("You don't have sufficient money in your account");
                          first_msg = "wrong";
                          oos.writeObject(first_msg);
                      }
                  }


              }catch (Exception e){
                  e.printStackTrace();
              }

          }
          else {
              Sending.setText("You have to log in please");
          }


      });

    }
    public static void main (String[] args){
        launch(args);
    }
    public  void balance (String s) {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 7773);
             OutputStream os = socket.getOutputStream();
             InputStream is = socket.getInputStream();
             ObjectOutputStream oos = new ObjectOutputStream(os);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            oos.writeObject(My_actions.myactions.Balancerequest);
            oos.writeObject(s);
         My_actions.myactions   action = (My_actions.myactions) ois.readObject();
            if (action.equals(My_actions.myactions.Balanceresponse)) {
                double x= 0;
               String re = (String)ois.readObject();
               x= Double.parseDouble(re);
                S.setValue(x);
                mybalance = x;
            }
           // oos.flush();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
