import javax.sound.sampled.*;
import java.io.File;

public class SimpleAudioTest {
    public static void main(String[] args) {
        try {
            System.out.println("Java Version: " + System.getProperty("java.version"));
            System.out.println("OS: " + System.getProperty("os.name"));
            
            File f = new File("audio/Click.wav");
            System.out.println("File: " + f.getAbsolutePath());
            System.out.println("File exists: " + f.exists());
            System.out.println("File length: " + f.length() + " bytes");
            
            if (f.exists() && f.length() > 0) {
                AudioInputStream stream = AudioSystem.getAudioInputStream(f);
                AudioFormat format = stream.getFormat();
                System.out.println("Audio Format: " + format);
                
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                System.out.println("Clip opened successfully!");
                clip.start();
                System.out.println("Playing... (you should hear a click)");
                Thread.sleep(3000);
                clip.close();
                System.out.println("Test complete.");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}