/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.web.gui.components;

import com.google.common.base.Joiner;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.listeditor.ListEditorDelegate;
import com.haulmont.cuba.gui.components.listeditor.ListEditorHelper;
import com.haulmont.cuba.gui.data.ValueListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.ldap.OperationNotSupportedException;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class WebListEditor extends WebAbstractField<WebListEditor.CubaListEditor> implements ListEditor {

    protected ListEditorDelegate delegate;

    public WebListEditor() {
        delegate = AppBeans.get(ListEditorDelegate.class);
        delegate.setActualField(this);
        component = new CubaListEditor(delegate.getLayout());
        setStyleName("c-listeditor");
    }

    @Override
    public ItemType getItemType() {
        return delegate.getItemType();
    }

    @Override
    public void setItemType(ItemType itemType) {
        delegate.setItemType(itemType);
    }

    @Override
    public boolean isUseLookupField() {
        return delegate.isUseLookupField();
    }

    @Override
    public void setUseLookupField(boolean useLookupField) {
        delegate.setUseLookupField(useLookupField);
    }

    @Override
    public String getLookupScreen() {
        return delegate.getLookupScreen();
    }

    @Override
    public void setLookupScreen(String lookupScreen) {
        delegate.setLookupScreen(lookupScreen);
    }

    @Override
    public String getEntityName() {
        return delegate.getEntityName();
    }

    @Override
    public void setEntityName(String entityName) {
        delegate.setEntityName(entityName);
    }

    @Override
    public List<Object> getOptionsList() {
        return delegate.getOptionsList();
    }

    @Override
    public void setOptionsList(List<Object> optionsList) {
        delegate.setOptionsList(optionsList);
    }

    @Override
    public String getEntityJoinClause() {
        return delegate.getEntityJoinClause();
    }

    @Override
    public void setEntityJoinClause(String entityJoinClause) {
        delegate.setEntityJoinClause(entityJoinClause);
    }

    @Override
    public String getEntityWhereClause() {
        return delegate.getEntityWhereClause();
    }

    @Override
    public void setEntityWhereClause(String entityWhereClause) {
        delegate.setEntityWhereClause(entityWhereClause);
    }

    @Override
    public void setValue(Object newValue) {
        if (!(newValue instanceof List)) {
            throw new IllegalArgumentException("Value type must be List");
        }
        super.setValue(newValue);
        delegate.setValue((List) newValue);
        fireValueChanged(newValue);
    }

    @Override
    public List getValue() {
        return delegate.getValue();
    }

    protected void fireValueChanged(Object value) {
        if (!ObjectUtils.equals(prevValue, value)) {
            Object oldValue = prevValue;

            prevValue = value;

            if (listeners != null && !listeners.isEmpty()) {
                ValueChangeEvent event = new ValueChangeEvent(this, oldValue, value);
                for (ValueChangeListener listener : listeners) {
                    listener.valueChanged(event);
                }
            }
        }
    }

    public class CubaListEditor extends CustomField<List> {

        private final Component content;

        public CubaListEditor(HBoxLayout mainLayout) {
            content = WebComponentsHelper.unwrap(mainLayout);
        }

        @Override
        protected Component initContent() {
            return content;
        }

        @Override
        public Class getType() {
            return List.class;
        }
    }
}