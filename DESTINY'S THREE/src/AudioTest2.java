import javax.sound.sampled.*;
import java.io.File;

public class AudioTest2 {
    public static void main(String[] args) {
        try {
            System.out.println("Current directory: " + System.getProperty("user.dir"));
            
            File f = new File("audio/Click.wav");
            System.out.println("File path: " + f.getAbsolutePath());
            System.out.println("File exists: " + f.exists());
            
            if (f.exists()) {
                AudioInputStream audio = AudioSystem.getAudioInputStream(f);
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                clip.start();
                System.out.println("✅ Playing Click.wav...");
                Thread.sleep(2000);
                clip.close();
                System.out.println("✅ Done!");
            } else {
                System.out.println("❌ File not found!");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}