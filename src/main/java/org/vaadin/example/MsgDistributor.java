package org.vaadin.example;

import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.ui.UI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;

@Singleton
public class MsgDistributor {

    private final Logger log = Logger.getLogger(getClass().getName() + hashCode());

    private final Set<MyUI> activeUIs = new HashSet<>();
    
    private boolean sessionDestroyedListenerSet = false;

    public void register(MyUI ui) {
        synchronized (activeUIs) {
            activeUIs.add(ui);
        }
        if(!sessionDestroyedListenerSet) {
            ui.getSession().getService().addSessionDestroyListener((SessionDestroyEvent event) -> {
                log.log(Level.INFO, "Session destroyed listener");
                Collection<UI> uIs = event.getSession().getUIs();
                for (UI uI : uIs) {
                    if(uI.isAttached()) {
                        if (uI instanceof MyUI) {
                            MyUI myUI = (MyUI) uI;
                            deRegister(myUI);
                        }
                        log.log(Level.INFO, "De-registered UI {0}", uI.hashCode());
                    } else {
                        log.log(Level.INFO, "UI {0} was already properly detached during session destroyed listener.", uI.hashCode());
                    }
                }
            });
            sessionDestroyedListenerSet = true;
        }
        
        ui.addDetachListener(e -> {
            deRegister(ui);
        });
        log.log(Level.INFO, "Registered UI {0}", ui.hashCode());
    }

    public void deRegister(MyUI ui) {
        boolean removed;
        synchronized (activeUIs) {
            removed = activeUIs.remove(ui);
        }
        log.log(Level.INFO, "De-registered UI {0} : {1}", new Object[]{ui.hashCode(), removed});
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
