package org.ironsight.wpplugin.expandLayerTool.operations;

import org.ironsight.wpplugin.expandLayerTool.Gui.MappingEditorPanel;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;


public class LayerMappingContainer {
    public String filePath = "/home/klipper/Documents/worldpainter/mappings.txt";
    public static LayerMappingContainer INSTANCE = new LayerMappingContainer();
    private final ArrayList<Runnable> genericNotifies = new ArrayList<>();
    private final HashMap<Integer, ArrayList<Runnable>> uidNotifies = new HashMap<>();
    private final HashMap<Integer, LayerMapping> mappings = new HashMap<>();
    private int nextUid = 1;

    public LayerMappingContainer() {
        addMapping(new LayerMapping(new SlopeProvider(), new TestInputOutput(), new MappingPoint[0], ActionType.SET,
                "paint mountainsides", "", -1));
        addMapping(new LayerMapping(new HeightProvider(), new TestInputOutput(), new MappingPoint[0], ActionType.SET,
                "frost mountain tops", "", -1));
        addMapping(new LayerMapping(new SlopeProvider(), new TestInputOutput(), new MappingPoint[0], ActionType.SET,
                "no steep pines", "", -1));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("TEST PANEL");

        INSTANCE.addMapping(new LayerMapping(new SlopeProvider(), new TestInputOutput(), new MappingPoint[0],
                ActionType.SET, "paint mountainsides", "", -1));
        INSTANCE.addMapping(new LayerMapping(new HeightProvider(), new TestInputOutput(), new MappingPoint[0],
                ActionType.SET, "frost mountain tops", "", -1));
        INSTANCE.addMapping(new LayerMapping(new SlopeProvider(), new TestInputOutput(), new MappingPoint[0],
                ActionType.SET, "no steep pines", "", -1));

        JDialog log = MappingEditorPanel.createDialog(frame, f -> {
        });
        log.setVisible(true);


        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void updateMapping(LayerMapping mapping) {
        //filter for identity
        if (!mappings.containsKey(mapping.uid) || mappings.get(mapping.uid).equals(mapping)) return;
        mappings.put(mapping.uid, mapping);
        notify(mapping);
    }

    public void deleteMapping(int uid) {
        LayerMapping removed = mappings.remove(uid);
        if (removed != null) {
            notify(removed);
        }
    }

    public int addMapping(LayerMapping mapping) {
        assert mapping != null;
        if (mappings.containsKey(mapping.uid)) return -1;
        this.mappings.put(nextUid, mapping);
        mapping.uid = nextUid;
        nextUid++;
        notify(mapping);
        return mapping.uid;
    }

    public void subscribe(Runnable runnable) {
        genericNotifies.add(runnable);
    }

    public void unsubscribe(Runnable runnable) {
        genericNotifies.remove(runnable);
    }

    public void subscribeToMapping(int uid, Runnable runnable) {
        ArrayList<Runnable> listeners = uidNotifies.getOrDefault(uid, new ArrayList<>());
        listeners.add(runnable);
        uidNotifies.put(uid, listeners);
    }

    public void unsubscribeToMapping(int uid, Runnable runnable) {
        ArrayList<Runnable> listeners = uidNotifies.getOrDefault(uid, new ArrayList<>());
        listeners.remove(runnable);
    }

    public LayerMapping queryMappingById(int uid) {
        return mappings.get(uid);
    }

    public LayerMapping[] queryMappingsAll() {
        LayerMapping[] arr = mappings.values().toArray(new LayerMapping[0]);
        Arrays.sort(arr, new Comparator<LayerMapping>() {
            @Override
            public int compare(LayerMapping o1, LayerMapping o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return arr;
    }

    private void notify(LayerMapping mapping) {
        for (Runnable r : genericNotifies)
            r.run();
        if (mapping != null && uidNotifies.containsKey(mapping.uid)) {
            for (Runnable r : uidNotifies.get(mapping.uid))
                r.run();
        }
    }

    private boolean suppressFileWriting = false;
    public void readFromFile() {
        mappings.clear();
        Object deserializedObject;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            suppressFileWriting = true;
            // Read the object from the file
            deserializedObject = ois.readObject();

            Object[] arr = (Object[]) deserializedObject;
            for (Object o : arr) {
                if (o instanceof LayerMapping) addMapping((LayerMapping) o);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during deserialization: " + e.getMessage());
            e.printStackTrace();
        } finally {
            suppressFileWriting = false;
        }
    }

    public void writeToFile() {
        if (suppressFileWriting) return;
        Object obj = mappings.values().toArray();
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(filePath)))) {
            oos.writeObject(obj);
            System.out.println("Object successfully serialized to " + filePath);
        } catch (IOException e) {
            System.err.println("Error during serialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

