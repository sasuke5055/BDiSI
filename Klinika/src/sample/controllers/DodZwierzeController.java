package sample.controllers;


import com.mysql.cj.jdbc.SuspendableXAConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;


public class DodZwierzeController {

    public DodZwierzeController(PanelController panelController, Stage tStage, Connection conn){
        this.panelController=panelController;
        this.tStage=tStage;
        this.conn=conn;
    }

    private int rok;
    private PanelController panelController;
    private Connection conn;
    private ObservableList<String> rasyList;
    private Stage tStage;
    private Statement stmt;
    private String query;
    private ResultSet res;
    private boolean isActualisation=false;
    private String idZwierza;


    @FXML
    private void initialize(){
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/klinika", "log@localhost", "pas");
            stmt = conn.createStatement();
            query="SELECT gatunek, rasa FROM rasy ORDER BY r_id";
            res = stmt.executeQuery(query);
            rasyList= FXCollections.observableArrayList();

            String list=new String();
            while (res.next()) {
                String temp = res.getString("gatunek")+" - "+ res.getString("rasa");
                rasyList.add(temp);
            }



            pickerGatunek.setItems(rasyList);
            pickerGatunek.setValue("Inna - Inna");


            btnZatwierdz.setDisable(true);
            fldRok.textProperty().addListener((observable, oldValue, newValue) -> {

                btnZatwierdz.setDisable(false);

                if (newValue.length() > 4)
                    try {
                        throw new Exception();
                    } catch (Exception e) {
                        fldRok.setText(oldValue);
                    }
                if (newValue.length() != 0)
                    try {
                        rok = Integer.parseInt(newValue);
                    }catch (NumberFormatException e){
                        Platform.runLater(() -> {
                            fldRok.setText(oldValue);

                        });
                    }

                if (rok > LocalDate.now().getYear())
                    btnZatwierdz.setDisable(true);

            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private TextField fldNazwa;

    @FXML
    private TextField fldUmaszczenie;

    @FXML
    private TextField fldRok;

    @FXML
    private TextField fldId;

    @FXML
    private ChoiceBox<String> pickerGatunek;


    @FXML
    private Button btnZatwierdz;

    @FXML
    void btnZatwierdzOnAction(ActionEvent event) {

        String imie=fldNazwa.getText();
        String umaszczenie=fldUmaszczenie.getText();
        String rok=fldRok.getText();
        String idRasy="";
        String idWlasciciela=fldId.getText();


        try {

            stmt = conn.createStatement();
            String query = "SELECT r_id FROM rasy WHERE CONCAT(gatunek,' - ',rasa)= '"+pickerGatunek.getValue()+"';";
            ResultSet res = stmt.executeQuery(query);
            if(res.next())
                idRasy=res.getString("r_id");

            //czy taki wlasciciel istnieje
            stmt = conn.createStatement();
            query = "SELECT count(*) AS c FROM wlasciciele WHERE w_id='"+idWlasciciela+"';";
            res = stmt.executeQuery(query);
            res.next();
            if(res.getString("c").equals("0")){
                fldId.setText("Złe id Właściciela");
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(imie.equals("")||idWlasciciela.equals("")){
            btnZatwierdz.setText("Brak danych");
            return;
        }

        try {
            stmt = conn.createStatement();
            if(!isActualisation) {
                query = "INSERT INTO pacjenci(nazwa,id_wlasciciela,id_rasy,rok_urodzenia,umaszczenie) " +
                        "VALUES('" + imie + "','" + idWlasciciela + "','" + idRasy + "','" + rok + "','" + umaszczenie + "');";
            }
            else{
                //todo: aktualizacja
            }
            stmt.executeUpdate(query);
            panelController.refresh();
            tStage.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setIsactualisation(boolean isActualisation) {
        this.isActualisation = isActualisation;
    }
    public void setActData(String idZwierza,String nazwa,String umaszczenie,String Rok, String IDwlasciciela, String Rasa, String Gatunek){
        this.idZwierza=idZwierza;
        fldNazwa.setText(nazwa);
        fldUmaszczenie.setText(umaszczenie);
        fldRok.setText(Rok);
        fldId.setText(IDwlasciciela);
        String temp=Rasa+" - "+Gatunek;
        pickerGatunek.setValue(temp);
    }
}

