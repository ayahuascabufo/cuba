/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 11.12.2008 19:02:39
 *
 * $Id$
 */
package com.haulmont.cuba.web.gui;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.DataService;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.gui.components.ComponentsHelper;
import com.itmill.toolkit.terminal.Sizeable;
import com.itmill.toolkit.ui.*;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.*;

public class Window implements com.haulmont.cuba.gui.components.Window, Component.Wrapper, Component.HasXmlDescriptor
{
    private String id;

    private Map<String, Component> componentByIds = new HashMap<String, Component>();
    private String messagePack;

    protected com.itmill.toolkit.ui.Component component;
    private Element element;

    private DsContext dsContext;
    private String caption;

    private List<CloseListener> listeners = new ArrayList<CloseListener>();

    public Window() {
        component = createLayout();
    }

    protected com.itmill.toolkit.ui.Component createLayout() {
        VerticalLayout layout = new VerticalLayout();

        layout.setMargin(true);
        layout.setSizeFull();

        return layout;
    }

    protected ComponentContainer getContainer() {
        return (ComponentContainer) component;
    }

    public String getMessagesPack() {
        return messagePack;
    }

    public void setMessagesPack(String name) {
        messagePack = name;
    }

    public String getMessage(String key) {
        if (messagePack == null)
            throw new IllegalStateException("MessagePack is not set");
        return MessageProvider.getMessage(messagePack, key);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public <T extends com.haulmont.cuba.gui.components.Window> T openWindow(String windowAlias, WindowManager.OpenType openType, Map<String, Object> params) {
        final com.haulmont.cuba.gui.config.WindowConfig windowConfig = AppConfig.getInstance().getWindowConfig();
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return App.getInstance().getWindowManager().<T>openWindow(windowInfo, openType, params);
    }

    public <T extends com.haulmont.cuba.gui.components.Window> T openWindow(String windowAlias, WindowManager.OpenType openType) {
        final com.haulmont.cuba.gui.config.WindowConfig windowConfig = AppConfig.getInstance().getWindowConfig();
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return App.getInstance().getWindowManager().<T>openWindow(windowInfo, openType);
    }

    public <T extends com.haulmont.cuba.gui.components.Window> T openEditor(String windowAlias, Object item, WindowManager.OpenType openType, Map<String, Object> params) {
        final com.haulmont.cuba.gui.config.WindowConfig windowConfig = AppConfig.getInstance().getWindowConfig();
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return App.getInstance().getWindowManager().<T>openEditor(windowInfo, item, openType, params);
    }

    public <T extends com.haulmont.cuba.gui.components.Window> T openEditor(String windowAlias, Object item, WindowManager.OpenType openType) {
        final com.haulmont.cuba.gui.config.WindowConfig windowConfig = AppConfig.getInstance().getWindowConfig();
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return App.getInstance().getWindowManager().<T>openEditor(windowInfo, item, openType);
    }

    public <T extends com.haulmont.cuba.gui.components.Window> T openLookup(String windowAlias, com.haulmont.cuba.gui.components.Window.Lookup.Handler handler, WindowManager.OpenType openType, Map<String, Object> params) {
        final com.haulmont.cuba.gui.config.WindowConfig windowConfig = AppConfig.getInstance().getWindowConfig();
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return App.getInstance().getWindowManager().<T>openLookup(windowInfo, handler, openType, params);
    }

    public <T extends com.haulmont.cuba.gui.components.Window> T openLookup(String windowAlias, com.haulmont.cuba.gui.components.Window.Lookup.Handler handler, WindowManager.OpenType openType) {
        final com.haulmont.cuba.gui.config.WindowConfig windowConfig = AppConfig.getInstance().getWindowConfig();
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return App.getInstance().getWindowManager().<T>openLookup(windowInfo, handler, openType);
    }

    public void showMessageDialog(String title, String message, MessageType messageType) {
        App.getInstance().getWindowManager().showMessageDialog(title, message, messageType);
    }

    public void showOptionDialog(String title, String message, MessageType messageType, Action[] actions) {
        App.getInstance().getWindowManager().showOptionDialog(title, message, messageType, actions);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public DsContext getDsContext() {
        return dsContext;
    }

    public void setDsContext(DsContext dsContext) {
        this.dsContext = dsContext;
    }

    public void addListener(CloseListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeListener(CloseListener listener) {
        listeners.remove(listener);
    }

    public Element getXmlDescriptor() {
        return element;
    }

    public void setXmlDescriptor(Element element) {
        this.element = element;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void add(Component component) {
        getContainer().addComponent(ComponentsHelper.unwrap(component));
        if (component.getId() != null) {
            componentByIds.put(component.getId(), component);
        }
    }

    public void remove(Component component) {
        getContainer().removeComponent(ComponentsHelper.unwrap(component));
        if (component.getId() != null) {
            componentByIds.remove(component.getId());
        }
    }

    public boolean onClose(String actionId) {
        fireWindowClosed(actionId);
        return true;
    }

    protected void fireWindowClosed(String actionId) {
        for (Object listener : listeners) {
            if (listener instanceof CloseListener) {
                ((CloseListener) listener).windowClosed(actionId);
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void requestFocus() {
    }

    public float getHeight() {
        return component.getHeight();
    }

    public int getHeightUnits() {
        return component.getHeightUnits();
    }

    public void setHeight(String height) {
        component.setHeight(height);
    }

    public float getWidth() {
        return component.getWidth();
    }

    public int getWidthUnits() {
        return component.getWidthUnits();
    }

    public void setWidth(String width) {
        component.setWidth(width);
    }

    public <T extends Component> T getOwnComponent(String id) {
        //noinspection unchecked
        return (T) componentByIds.get(id);
    }

    public <T extends Component> T getComponent(String id) {
        return ComponentsHelper.<T>getComponent(this, id);
    }

    public Alignment getAlignment() {
        return Alignment.MIDDLE_CENTER; 
    }

    public void setAlignment(Alignment alignment) {}

    public void expand(Component component, String height, String width) {
        final com.itmill.toolkit.ui.Component expandedComponent = ComponentsHelper.unwrap(component);
        if (getContainer() instanceof AbstractOrderedLayout) {
            ComponentsHelper.expand((AbstractOrderedLayout) getContainer(), expandedComponent, height, width);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public <T> T getComponent() {
        //noinspection unchecked
        return (T) component;
    }

    public boolean close(String actionId) {
        App.getInstance().getWindowManager().close(this);
        return onClose(actionId);
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public <A extends IFrame> A getFrame() {
        //noinspection unchecked
        return (A) this;
    }

    public void setFrame(IFrame frame) {
        throw new UnsupportedOperationException();
    }

    public static class Editor extends Window implements com.haulmont.cuba.gui.components.Window.Editor {
        protected Object item;
        protected Form form;

        public Object getItem() {
            return item;
        }

        @Override
        protected com.itmill.toolkit.ui.Component createLayout() {
            VerticalLayout layout = new VerticalLayout();

            form = createForm();

            HorizontalLayout okbar = new HorizontalLayout();
            okbar.setHeight(-1, Sizeable.UNITS_PIXELS);

            HorizontalLayout buttonsContainer = new HorizontalLayout();

            final String messagesPackage = AppConfig.getInstance().getMessagesPack();

            buttonsContainer.addComponent(new Button(MessageProvider.getMessage(messagesPackage, "actions.Ok"), this, "commit"));
            buttonsContainer.addComponent(new Button(MessageProvider.getMessage(messagesPackage, "actions.Cancel"), new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    close("cancel");
                }
            }));

            okbar.addComponent(buttonsContainer);

            layout.addComponent(form);
            layout.addComponent(okbar);

            form.setSizeFull();
            layout.setExpandRatio(form, 1);
            layout.setComponentAlignment(okbar, com.itmill.toolkit.ui.Alignment.BOTTOM_RIGHT);

            return layout;
        }

        protected Form createForm() {
            final VerticalLayout formLayout = new VerticalLayout();
            formLayout.setSizeFull();

            return  new Form(formLayout);
        }

        @Override
        protected ComponentContainer getContainer() {
            return form.getLayout();
        }

        public void setItem(Object item) {
            final Datasource ds = getDatasource();
            if (ds == null) throw new IllegalStateException("Can't find main datasource");

            Entity entity = getEntity(item, ds);

            this.item = item;
            //noinspection unchecked
            ds.setItem(entity);
        }

        protected Entity getEntity(Object item, Datasource ds) {
            Entity entity;
            if (item instanceof Datasource) {
                final Datasource itemDs = (Datasource) item;
                entity = itemDs.getItem();

                if (entity == null) return null;

                if (!PersistenceHelper.isNew(entity)) {
                    // TODO (abramov) refactor this trick
                    if (Datasource.CommitMode.DATASTORE.equals(itemDs.getCommitMode())) {
                        final DataService dataservice = ds.getDataService();
                        entity = dataservice.reload(entity, ds.getView());
                    }
                }
            } else {
                if (!PersistenceHelper.isNew((Entity) item)) {
                    final DataService dataservice = ds.getDataService();
                    entity = dataservice.reload((Entity) item, ds.getView());
                } else {
                    entity = (Entity) item;
                }
            }
            
            return entity;
        }

        protected Datasource getDatasource() {
            final Element element = getXmlDescriptor();

            final String datasourceName = element.attributeValue("datasource");
            if (!StringUtils.isEmpty(datasourceName)) {
                final DsContext context = getDsContext();
                if (context != null) {
                    final Datasource ds = context.get(datasourceName);
                    return ds;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        protected MetaClass getMetaClass(Object item) {
            final MetaClass metaClass;
            if (item instanceof Datasource) {
                metaClass = ((Datasource) item).getMetaClass();
            } else {
                metaClass = ((Instance) item).getMetaClass();
            }
            return metaClass;
        }

        protected Instance getInstance(Object item) {
            if (item instanceof Datasource) {
                return (Instance) ((Datasource) item).getItem();
            } else {
                return (Instance) item;
            }
        }

        public void commit() {
            form.commit();

            final DsContext context = getDsContext();
            if (context != null) {
                context.commit();
            } else {
                if (item instanceof Datasource) {
                    final Datasource ds = (Datasource) item;
                    ds.commit();
                } else {
                    DataService service = getDataService();
                    service.commit((Entity) item);
                }
            }
            close("commit");
        }

        protected DataService getDataService() {
            final DsContext context = getDsContext();
            if (context == null) {
                throw new UnsupportedOperationException();
            } else {
                return context.getDataService();
            }
        }
    }

    public static class Lookup extends Window implements com.haulmont.cuba.gui.components.Window.Lookup {
        private Handler handler;

        private Component lookupComponent;
        private VerticalLayout contaiter;

        public com.haulmont.cuba.gui.components.Component getLookupComponent() {
            return lookupComponent;
        }

        public void setLookupComponent(Component lookupComponent) {
            this.lookupComponent = lookupComponent;
        }

        public Handler getLookupHandler() {
            return handler;
        }

        public void setLookupHandler(Handler handler) {
            this.handler = handler;
        }

        @Override
        protected ComponentContainer getContainer() {
            return contaiter;
        }

        @Override
        protected com.itmill.toolkit.ui.Component createLayout() {
            final VerticalLayout form = new VerticalLayout();

            contaiter = new VerticalLayout();

            HorizontalLayout okbar = new HorizontalLayout();
            okbar.setHeight(-1, Sizeable.UNITS_PIXELS);

            final String messagesPackage = AppConfig.getInstance().getMessagesPack();
            final Button selectButton = new Button(MessageProvider.getMessage(messagesPackage, "actions.Select"));
            selectButton.addListener(new SelectAction(this));

            final Button cancelButton = new Button(MessageProvider.getMessage(messagesPackage, "actions.Cancel"), new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    close("cancel");
                }
            });

            okbar.addComponent(selectButton);
            okbar.addComponent(cancelButton);

            form.addComponent(contaiter);
            form.addComponent(okbar);

            contaiter.setSizeFull();
            form.setExpandRatio(contaiter, 1);
            form.setComponentAlignment(okbar, com.itmill.toolkit.ui.Alignment.MIDDLE_RIGHT);

            return form;
        }
    }
}
