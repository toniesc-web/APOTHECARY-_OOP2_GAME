import javax.sound.sampled.*;
import java.io.File;

public class AudioTest {
    public static void main(String[] args) {
        try {
            // ✅ Use relative path from current directory
            File f = new File("audio/Click.wav");
            System.out.println("File exists: " + f.exists());
            System.out.println("Full path: " + f.getAbsolutePath());
            
            if (f.exists()) {
                AudioInputStream audio = AudioSystem.getAudioInputStream(f);
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                clip.start();
                System.out.println("✅ Playing sound...");
                Thread.sleep(2000);
                System.out.println("Done.");
            } else {
                System.out.println("❌ File not found!");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}