/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.gui.app.core.categories;

import com.haulmont.cuba.core.entity.CategoryAttributeEnumValue;
import com.haulmont.cuba.core.entity.LocaleHelper;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.listeditor.ListEditorHelper;
import com.haulmont.cuba.gui.components.listeditor.ListEditorWindowController;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class LocalizedEnumerationWindow extends AbstractWindow implements ListEditorWindowController {

    @Inject
    protected Button addBtn;

    @Inject
    protected TextField valueField;

    @Inject
    protected LocalizedNameFrame localizedFrame;

    @Inject
    protected Table<CategoryAttributeEnumValue> enumValuesTable;

    @Inject
    protected CollectionDatasource<CategoryAttributeEnumValue, UUID> enumValuesDs;

    @Inject
    protected ComponentsFactory factory;

    @WindowParam
    protected List<Object> values;

    @WindowParam
    protected Map<Object, String> valuesMap;

    @WindowParam
    protected String enumerationLocales;

    @Inject
    private Action add;

    @Override
    public void init(Map<String, Object> params) {
        enumValuesTable.addGeneratedColumn("cancel", entity -> {
            LinkButton delItemBtn = factory.createComponent(LinkButton.class);
            delItemBtn.setIcon("icons/item-remove.png");
            delItemBtn.setAction(new AbstractAction("") {
                @Override
                public void actionPerform(Component component) {
                    enumValuesDs.removeItem(entity);
                    valuesMap.remove(entity.getValue());
                }
            });
            return delItemBtn;
        });
        enumValuesTable.setColumnWidth("cancel", 30);

        enumValuesDs.addItemChangeListener(e -> {
            if (e.getPrevItem() == null) { // if the first time selected
                localizedFrame.setEditableFields(true);
            } else {
                e.getPrevItem().setLocalizedValues(
                        LocaleHelper.convertFromSimpleKeyLocales(e.getPrevItem().getValue(), localizedFrame.getValue())
                );
            }
            if (e.getItem() == null) { // if item deleted and selection disappeared
                localizedFrame.clearFields();
                localizedFrame.setEditableFields(false);
            } else {
                String localizedValues =
                        e.getItem().getLocalizedValues() == null ?
                                "" : LocaleHelper.convertToSimpleKeyLocales(e.getItem().getLocalizedValues());
                localizedFrame.setValue(localizedValues);
            }
        });

        addBtn.setAction(add);

        initValues();
    }

    @Override
    public void ready() {
        localizedFrame.setEditableFields(false);
    }

    protected void initValues() {
        if (values == null) {
            values = new ArrayList<>();
        }

        valuesMap = values.stream()
                .collect(Collectors.toMap(Function.identity(), o -> ListEditorHelper.getValueCaption(o, ListEditor.ItemType.STRING)));

        Map<String, String> localizedValues = LocaleHelper.getLocalizedValuesMap(enumerationLocales);

        for (Map.Entry<Object, String> entry : valuesMap.entrySet()) {
            addValueToDatasource(entry.getKey(), buildLocalizedValuesForEnumValue(entry.getValue(), localizedValues));
        }

        enumValuesDs.commit();
    }

    @Override
    public List<Object> getValue() {
        return new ArrayList<>(valuesMap.keySet());
    }

    public String getLocalizedValues() {
        Properties properties = new Properties();
        for (CategoryAttributeEnumValue enumValue : enumValuesDs.getItems()) {
            properties.putAll(LocaleHelper.getLocalizedValuesMap(enumValue.getLocalizedValues()));
        }

        enumerationLocales = LocaleHelper.convertPropertiesToString(properties);
        return enumerationLocales;
    }

    protected boolean valueExists(Object value) {
        return valuesMap.keySet().contains(value);
    }

    protected void addValueToDatasource(Object value, String enumLocaleValues) {
        CategoryAttributeEnumValue enumValue = new CategoryAttributeEnumValue();
        enumValue.setValue(value.toString());
        enumValue.setLocalizedValues(enumLocaleValues);
        enumValuesDs.addItem(enumValue);
    }

    protected String buildLocalizedValuesForEnumValue(String enumValue, Map<String, String> localizedValues) {
        StringBuilder sb = new StringBuilder();
        List<String> values = new ArrayList<>();

        for (Map.Entry<String, String> entry : localizedValues.entrySet()) {
            String key = entry.getKey();
            if (key.contains(enumValue)) {
                sb.append(key)
                  .append("=")
                  .append(entry.getValue())
                  .append("\r\n");

                values.add(sb.toString());
                sb.delete(0, sb.length());
            }
        }
        values.sort(Comparator.reverseOrder());

        return String.join("", values);
    }

    public void addEnumValue() {
        Object value = valueField.getValue();
        if (value != null) {
            if (!valueExists(value)) {
                valueField.setValue(null);
                addValueToDatasource(value, null);
                valuesMap.put(value, ListEditorHelper.getValueCaption(value, ListEditor.ItemType.STRING));
            }
        }
    }

    public void commit() {
        if (!enumValuesTable.getSelected().isEmpty()) {
            CategoryAttributeEnumValue enumValue = enumValuesTable.getSelected().iterator().next();
            enumValue.setLocalizedValues(
                    LocaleHelper.convertFromSimpleKeyLocales(enumValue.getValue(), localizedFrame.getValue())
            );
        }
        enumValuesDs.commit();

        close(COMMIT_ACTION_ID);
    }

    public void cancel() {
        close(CLOSE_ACTION_ID);
    }
}
