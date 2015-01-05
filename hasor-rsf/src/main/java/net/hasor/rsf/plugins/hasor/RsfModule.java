/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
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
package net.hasor.rsf.plugins.hasor;
import net.hasor.core.ApiBinder;
import net.hasor.core.EventContext;
import net.hasor.core.EventListener;
import net.hasor.core.Module;
import net.hasor.rsf.RsfBinder;
import net.hasor.rsf.RsfContext;
import net.hasor.rsf.bootstrap.RsfBootstrap;
import net.hasor.rsf.bootstrap.RsfStart;
import net.hasor.rsf.plugins.local.LocalPrefPlugin;
import net.hasor.rsf.plugins.qps.QPSPlugin;
/**
 * Rsf 制定 Hasor Module。
 * @version : 2014年11月12日
 * @author 赵永春(zyc@hasor.net)
 */
public abstract class RsfModule implements Module {
    private final RsfContext createRsfContext(ApiBinder apiBinder) throws Throwable {
        //1.调用引导程序启动 RSF
        RsfBootstrap bootstrap = new RsfBootstrap();
        bootstrap.bindSettings(apiBinder.getEnvironment().getSettings());
        bootstrap.doBinder(new RsfStart() {
            public void onBind(RsfBinder rsfBinder) throws Throwable {
                rsfBinder.bindFilter("QPS", new QPSPlugin());
                rsfBinder.bindFilter("LocalPre", new LocalPrefPlugin());
            }
        });
        bootstrap.socketBind(this.bindAddress(), this.bindPort());
        final RsfContext rsfContext = bootstrap.sync();
        //
        //2.同步接收 AppContext 的 shutdown 通知，并且传递给 RSF
        EventContext eventContext = apiBinder.getEnvironment().getEventContext();
        apiBinder.bindType(RsfContext.class, rsfContext);
        eventContext.pushListener(EventContext.ContextEvent_Shutdown, new EventListener() {
            public void onEvent(String event, Object[] params) throws Throwable {
                rsfContext.shutdown();
            }
        });
        return rsfContext;
    }
    //
    private static final Object LOCK_KEY   = new Object();
    private static RsfContext   rsfContext = null;
    //
    public final void loadModule(final ApiBinder apiBinder) throws Throwable {
        //
        synchronized (LOCK_KEY) {
            if (rsfContext == null) {
                rsfContext = createRsfContext(apiBinder);
            }
        }
        //
        //3.装载RSF Module
        this.loadModule(new InnerRsfApiBinder(apiBinder, rsfContext));
    }
    /**用于覆盖 rsf 配置文件中的配置。*/
    protected String bindAddress() {
        return null;
    }
    /**用于覆盖 rsf 配置文件中的配置。*/
    protected int bindPort() {
        return 0;
    }
    //
    public abstract void loadModule(RsfApiBinder apiBinder) throws Throwable;
}