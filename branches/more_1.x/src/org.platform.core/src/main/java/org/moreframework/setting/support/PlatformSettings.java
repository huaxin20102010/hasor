/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.moreframework.setting.support;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.more.global.Global;
import org.more.global.assembler.xml.XmlProperty;
import org.more.global.assembler.xml.XmlPropertyGlobalFactory;
import org.more.util.ResourceWatch;
import org.more.util.ResourcesUtils;
import org.more.util.StringUtils;
import org.more.util.map.Properties;
import org.moreframework.Assert;
import org.moreframework.MoreFramework;
import org.moreframework.setting.SettingListener;
import org.moreframework.setting.Settings;
/**
 * Settings�ӿڵ�ʵ�֣������ṩ�˶�config.xml��static-config.xml��config-mapping.properties�ļ��Ľ���֧�֡�
 * ����֮�⻹�ṩ�˶�config.xml�����ļ��ĸı�������������ļ�Ӧ��ֻ��һ������
 * @version : 2013-4-2
 * @author ������ (zyc@byshell.org)
 */
public class PlatformSettings extends Global implements Settings {
    private final String                appSettingsName1         = "config.xml";
    private final String                appSettingsName2         = "static-config.xml";
    private final String                appSettingsName3         = "config-mapping.properties";
    private final List<SettingListener> settingListenerList      = new ArrayList<SettingListener>();
    private final List<String>          loadNameSpaceList        = new ArrayList<String>();         //�Զ��������ļ������ռ䡣
    private String                      settingsEncoding         = "utf-8";
    private boolean                     enableSettingsMonitoring = true;                            //�Ƿ����������ļ��Ķ�����
    //
    //
    //
    public PlatformSettings() {
        this.disableCaseSensitive();
        //1.finalSettings
        this.loadALLConfig();
        //2.resourceWatch
        try {
            URL configURL = ResourcesUtils.getResource(appSettingsName1);
            Assert.isNotNull(configURL, "Can't get to " + configURL);
            this.resourceWatch = new SettingsResourceWatch(configURL.toURI(), 15 * 1000/*15����һ��*/);
            this.resourceWatch.setDaemon(true);
            this.resourceWatch.start();
        } catch (Exception e) {
            MoreFramework.error("resourceWatch start error, on : %s Settings file !%s", appSettingsName1, e);
        }
    };
    //
    //
    /**����ȫ�����ò��������ҷ�����{@link XmlProperty}��ʽ����*/
    public XmlProperty getXmlProperty(Enum<?> name) {
        return this.getToType(name, XmlProperty.class, null);
    };
    /**����ȫ�����ò��������ҷ�����{@link XmlProperty}��ʽ����*/
    public XmlProperty getXmlProperty(String name) {
        return this.getToType(name, XmlProperty.class, null);
    }
    /**���������ļ������������*/
    public void addSettingsListener(SettingListener settingsListener) {
        if (this.settingListenerList.contains(settingsListener) == false)
            this.settingListenerList.add(settingsListener);
    }
    /**ɾ�������ļ���������*/
    public void removeSettingsListener(SettingListener settingsListener) {
        if (this.settingListenerList.contains(settingsListener) == true)
            this.settingListenerList.remove(settingsListener);
    }
    @Override
    public SettingListener[] getSettingListeners() {
        return this.settingListenerList.toArray(new SettingListener[this.settingListenerList.size()]);
    }
    /**��ȡ���������ļ�ʱʹ�õ��ַ����롣*/
    public String getSettingsEncoding() {
        return this.settingsEncoding;
    }
    /**���ý��������ļ�ʱʹ�õ��ַ����롣*/
    public void setSettingsEncoding(String encoding) {
        this.settingsEncoding = encoding;
    }
    /**��ȡ�����������ļ�ʱ�ᱻ�����������ռ䡣*/
    public String[] getLoadNameSpaceList() {
        return this.loadNameSpaceList.toArray(new String[this.loadNameSpaceList.size()]);
    }
    /**���ӵ����������ļ�ʱ�ᱻ�����������ռ䡣*/
    public void addLoadNameSpace(String newLoadNameSpace) {
        this.loadNameSpaceList.add(newLoadNameSpace);
    }
    /**ɾ�������������ļ�ʱ�ᱻ�����������ռ䡣*/
    public void removeLoadNameSpace(String loadNameSpace) {
        this.loadNameSpaceList.remove(loadNameSpace);
    }
    /**����һ��ֵȷ���Ƿ����ö�config.xml�ļ��ĸı��ء�*/
    public boolean isEnableSettingsMonitoring() {
        return enableSettingsMonitoring;
    }
    /**����һ��ֵȷ���Ƿ����ö�config.xml�ļ��ĸı��ء�*/
    public void setEnableSettingsMonitoring(boolean enableSettingsMonitoring) {
        this.enableSettingsMonitoring = enableSettingsMonitoring;
    }
    /*-------------------------------------------------------------------------------------------------------*/
    private ResourceWatch resourceWatch = null; /*��س���*/
    /**װ�����������ļ�*/
    protected void loadALLConfig() {
        HashMap<String, Object> finalSettings = new HashMap<String, Object>();
        this.loadStaticConfig(finalSettings);
        this.loadMainConfig(finalSettings);
        this.loadMappingConfig(finalSettings);
        setContainer(finalSettings);
    }
    /**װ���������ļ���̬���á�*/
    protected void loadMainConfig(Map<String, Object> toMap) {
        String encoding = this.getSettingsEncoding();
        try {
            URL configURL = ResourcesUtils.getResource(appSettingsName1);
            if (configURL != null) {
                MoreFramework.info("load ��%s��", configURL);
                loadConfig(configURL.toURI(), encoding, toMap);
            }
        } catch (Exception e) {
            MoreFramework.error("load ��%s�� error!%s", appSettingsName1, e);
        }
    }
    /**װ�ؾ�̬���á�*/
    protected void loadStaticConfig(Map<String, Object> toMap) {
        String encoding = this.getSettingsEncoding();
        //1.װ������static-config.xml
        try {
            List<URL> streamList = ResourcesUtils.getResources(appSettingsName2);
            if (streamList != null) {
                for (URL resURL : streamList) {
                    MoreFramework.info("load ��%s��", resURL);
                    loadConfig(resURL.toURI(), encoding, toMap);
                }
            }
        } catch (Exception e) {
            MoreFramework.error("load ��%s�� error!%s", appSettingsName2, e);
        }
    }
    /**װ������ӳ�䣬�����ǲ��յ�ӳ�����á�*/
    protected void loadMappingConfig(Map<String, Object> referConfig) {
        try {
            List<URL> mappingList = ResourcesUtils.getResources(appSettingsName3);
            if (mappingList != null)
                for (URL url : mappingList) {
                    InputStream inputStream = ResourcesUtils.getResourceAsStream(url);
                    Properties prop = new Properties();
                    prop.load(inputStream);
                    for (String key : prop.keySet()) {
                        String $propxyKey = key.toLowerCase();
                        String $key = prop.get(key).toLowerCase();
                        Object value = referConfig.get($key);
                        if (value == null) {
                            MoreFramework.warning("%s mapping to %s value is null.", $propxyKey, $key);
                            continue;
                        }
                        value = (value instanceof XmlProperty) ? ((XmlProperty) value).getText() : value;
                        /*���Գ�ͻ��ӳ��*/
                        if (referConfig.containsKey($propxyKey) == true) {
                            MoreFramework.error("mapping conflict! %s has this key.", $propxyKey);
                        } else
                            referConfig.put($propxyKey, value);
                    }
                }
        } catch (Exception e) {
            MoreFramework.error("load ��%s�� error!%s", appSettingsName3, e);
        }
    }
    /**loadConfigװ������*/
    private void loadConfig(URI configURI, String encoding, Map<String, Object> loadTo) throws IOException {
        //Platform.info("PlatformSettings loadConfig Xml namespace : %s", configURI);
        XmlPropertyGlobalFactory xmlg = null;
        //1.<������Ч�������ռ�>
        try {
            xmlg = new XmlPropertyGlobalFactory();
            xmlg.setIgnoreRootElement(true);/*���Ը�*/
            /*�����Զ���������ռ�֧�֡�*/
            if (this.loadNameSpaceList != null && this.loadNameSpaceList.isEmpty() == false)
                for (String loadNS : this.loadNameSpaceList)
                    if (StringUtils.isBlank(loadNS) == false)
                        xmlg.getLoadNameSpace().add(loadNS);
            //
            Map<String, Object> dataMap = xmlg.createMap(encoding, new Object[] { ResourcesUtils.getResourceAsStream(configURI) });
            /*������ֵ�ϲ����⣨���ø��Ǻ�׷�ӵĲ��ԣ�*/
            for (String key : dataMap.keySet()) {
                String $key = key.toLowerCase();
                Object $var = dataMap.get(key);
                Object $varConflict = loadTo.get($key);
                if ($varConflict != null && $varConflict instanceof XmlProperty && $var instanceof XmlProperty) {
                    XmlProperty $new = (XmlProperty) $var;
                    XmlProperty $old = (XmlProperty) $varConflict;
                    XmlProperty $final = $old.clone();
                    /*���ǲ���*/
                    $final.getAttributeMap().putAll($new.getAttributeMap());
                    $final.setText($new.getText());
                    /*׷�Ӳ���*/
                    List<XmlProperty> $newChildren = new ArrayList<XmlProperty>($new.getChildren());
                    List<XmlProperty> $oldChildren = new ArrayList<XmlProperty>($old.getChildren());
                    Collections.reverse($newChildren);
                    Collections.reverse($oldChildren);
                    $final.getChildren().clear();
                    $final.getChildren().addAll($oldChildren);
                    $final.getChildren().addAll($newChildren);
                    Collections.reverse($final.getChildren());
                    loadTo.put($key, $final);
                } else
                    loadTo.put($key, $var);
            }
        } catch (Exception e) {
            MoreFramework.warning("namespcae [%s] no support!", configURI);
        }
    }
    /**���������������ļ�ʱ*/
    protected void reLoadConfig() {
        for (SettingListener listener : this.settingListenerList)
            listener.loadConfig(this);
    }
    /*-------------------------------------------------------------------------------------------------------*/
    /***/
    private class SettingsResourceWatch extends ResourceWatch {
        public SettingsResourceWatch(URI uri, int watchStepTime) {
            super(uri, watchStepTime);
        }
        public void reload(URI resourceURI) throws IOException {
            if (enableSettingsMonitoring == false)
                return;
            loadALLConfig();
            reLoadConfig();
        }
        public long lastModify(URI resourceURI) throws IOException {
            if ("file".equals(resourceURI.getScheme()) == true)
                return new File(resourceURI).lastModified();
            return 0;
        }
        public void firstLoad(URI resourceURI) throws IOException {}
    }
}