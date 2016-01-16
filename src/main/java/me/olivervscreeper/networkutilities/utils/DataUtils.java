package me.olivervscreeper.networkutilities.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import me.olivervscreeper.networkutilities.NetworkUtilities;

/**
 * A modified clone of the SLAPI API available on GitHub. Essentially, logging to the system and the
 * removal of unnecessary methods to result in a cleaner file.
 *
 * @author OliverVsCreeper
 */
public class DataUtils {

    /**
     * Attempts to write an object to the provided path, creating the file if it does not already
     * exist. Uses the default serialization methods, and as result, it is recommended that in
     * Bukkit applications, it be used in conjunction with the Serialization package
     *
     * @param objectToSave The object which should be saved
     * @param pathToSaveTo The path in which to save the object
     */
    public static <T extends Object> void saveObjectToPath(T objectToSave, String pathToSaveTo) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pathToSaveTo));
            oos.writeObject(objectToSave);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            NetworkUtilities
                    .log("Attempted to save file to " + pathToSaveTo + " - sadly, an error occurred.");
        }
    }

    public static void saveStringToPath(String stringToSave, String pathToSaveTo) {
        try {
            OutputStreamWriter oos = new OutputStreamWriter(new FileOutputStream(pathToSaveTo));
            oos.write(stringToSave);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            NetworkUtilities
                    .log("Attempted to save file to " + pathToSaveTo + " - sadly, an error occurred.");
        }
    }

    /**
     * Loads an object saved using the saveObjectToPath method. Returns an object which can be
     * casted, after checking to the expected Type.
     *
     * @param pathToLoadFrom The path in which the object file is located
     * @return An object which has been loaded from the file, or null if the file does not exist
     */
    public static <T extends Object> T loadObjectFromPath(String pathToLoadFrom) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pathToLoadFrom));
            T result = (T) ois.readObject();
            ois.close();
            return result;
        } catch (IOException | ClassNotFoundException ex) {
            NetworkUtilities
                    .log("Attempted to load file from " + pathToLoadFrom + " - sadly, an error occurred.");
            return null;
        }
    }

}
