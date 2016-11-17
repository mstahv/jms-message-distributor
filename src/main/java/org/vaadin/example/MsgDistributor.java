package org.vaadin.example;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;

@Singleton
public class MsgDistributor {

    private final Logger log = Logger.getLogger(getClass().getName());

    private final Set<MyUI> activeUIs = new HashSet<>();

    public void register(MyUI ui) {
        synchronized (activeUIs) {
            activeUIs.add(ui);
        }
        ui.addDetachListener(e -> {
            deRegister(ui);
        });
        log.log(Level.INFO, "Registered UI {0}", ui.hashCode());
    }

    public void deRegister(MyUI ui) {
        synchronized (activeUIs) {
            activeUIs.remove(ui);
        }
        log.log(Level.INFO, "De-registered UI {0}", ui.hashCode());
    }

    public void distribute(String body) {
        synchronized (activeUIs) {
            HashSet<MyUI> detachedUIs = new HashSet<>();
            activeUIs.parallelStream().forEach(ui -> {
                try {
                    ui.showMessage(body);
                } catch (com.vaadin.ui.UIDetachedException e) {
                    detachedUIs.add(ui);
                }
            });
            activeUIs.removeAll(detachedUIs);
        }
    }

}
