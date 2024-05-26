import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WetterAppGUI extends JFrame {

    private JSONObject weatherData;
    public WetterAppGUI()  {
        //SetUp unserers GUI + Titel
        super("Wetter App");

        // GUI Session beim schließen beenden
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Pixel Größe unseres GUI einstellen
        setSize(450,650);

        //GUI zentriert laden
        setLocationRelativeTo(null);

        //Layout Manager NULL um unsere Elemente manuell zu positionieren
        setLayout(null);

        //Veränderung der GUI Größe verhindern
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents()  {
        //Suchfeld
        JTextField searchTextField = new JTextField();

        //Ort + Groesse unserer Elemente
        searchTextField.setBounds(15,15,351,45);

        //Schriftart & Groesse ändern
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);

        //Wetter Bild
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        //Temperatur Text
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        //zentrier den Text
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        //Wetter Bedingung
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0,405,450,36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        //Feuchtigkeits Bild
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15,500,74,66);
        add(humidityImage);

        //Feuchtigkeits Text
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        //Wind Geschw. Bild
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220,500,74,66);
        add(windspeedImage);

        //Wind Geschw. Text
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windspeedText.setBounds(310, 500,85,55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN,16));
        add(windspeedText);

        //Suchen Knopf
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        //Cursor zur Hand beim Hovern über Suchen-Knopf
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375,13,47,45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Krieg die Region des Nutzers
                String userInput = searchTextField.getText();
                //validiere Input - leerzeichen entfernen fpr keine non empty texte
                if (userInput.replaceAll("\\s","").length() <= 0)  {
                   return;
                }
                //Ruf Wetter Daten ab
                weatherData = WetterApp.getWeatherData(userInput);
                //Update GUI

                //Update Wetter Bild
                String weatherCondition = (String) weatherData.get("weather_condition");
                //Bild in Abhängigkeit mit Wetterkondition
                switch (weatherCondition){
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }

                //Update Temperatur Text
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                //Update Wetterkondition Text
                weatherConditionDesc.setText(weatherCondition);

                //Update Feuchtigkeitstext
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity </b>" + humidity + "%</html>");

                //Update Windgeschwindigkeit Text
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Windspeed </b>" + windspeed + "km/h</html>");
            }
        });
        add(searchButton);


    }

    //Um Bilder in unserem GUI zu erstellen
    private ImageIcon loadImage(String resourcePath)  {
    try  {
        //Lies das Bild aus ggb. Pfad
        BufferedImage image = ImageIO.read(new File(resourcePath));

        //Gibt ein Bild Icon zurück, damit die Elemente es rendern können
        return new ImageIcon(image);
    } catch(IOException e)  {
        e.printStackTrace();
    }
    System.out.println("Could not find resource");
    return null;
    }
}
