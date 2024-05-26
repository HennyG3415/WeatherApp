import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Zeigt unser GUI an
              new WetterAppGUI().setVisible(true);
               // System.out.println(WetterApp.getLocationData("Tokyo"));
             //   System.out.println(WetterApp.getCurrentTime());


            }
        });
    }
}
