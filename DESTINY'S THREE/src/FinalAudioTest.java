

import javax.sound.sampled.*;
import java.io.File;

public class FinalAudioTest {
    public static void main(String[] args) {
        System.out.println("=== FINAL AUDIO TEST ===");
        
        // Test Click.wav
        testAudio("audio/Click.wav", "Click");
        
        // Test intro_music.wav (wait for it)
        testAudio("audio/intro_music.wav", "Background Music");
    }
    
    private static void testAudio(String path, String name) {
        try {
            System.out.println("\nTesting: " + name);
            File f = new File(path);
            System.out.println("File exists: " + f.exists());
            System.out.println("Full path: " + f.getAbsolutePath());
            System.out.println("File size: " + f.length() + " bytes");
            
            if (f.exists() && f.length() > 0) {
                AudioInputStream audio = AudioSystem.getAudioInputStream(f);
                AudioFormat format = audio.getFormat();
                System.out.println("Format: " + format);
                
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                System.out.println("Clip opened!");
                
                clip.start();
                System.out.println("Playing " + name + "...");
                
                // Let it play
                Thread.sleep(3000);
                clip.stop();
                clip.close();
                System.out.println("Done playing " + name);
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}