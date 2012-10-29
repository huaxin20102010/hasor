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
package org.more.webui.support;
/**
 * ��������ĸ���ͬʱҲ���𱣴�������ͼ���������齨��ʹ��@UIComע��ע��
 * @version : 2012-3-29
 * @author ������ (zyc@byshell.org)
 */
public class UIViewRoot extends UIComponent {
    public UIViewRoot() {
        this.setId("com_root");
    }
    public void restoreState(String componentID, Object[] stateData) {
        UIComponent com = this.getChildByID(componentID);
        com.restoreState(stateData);
    }
    public Object[] saveState(String componentID) {
        UIComponent com = this.getChildByID(componentID);
        return com.saveState();
    }
}