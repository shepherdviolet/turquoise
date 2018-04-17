package sviolet.turquoise.utilx.eventbus;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 注释式接收器
 */
class EvReceiverForMethodAnnotation extends EvReceiver {

    private WeakReference<Object> objectWeakReference;
    private Method method;

    EvReceiverForMethodAnnotation(Object obj, Method method) {
        objectWeakReference = new WeakReference<>(obj);
        this.method = method;
    }

    @Override
    protected void onReceive(EvMessage message) {
        Object obj = objectWeakReference.get();
        if (obj == null) {
            return;
        }
        try {
            method.invoke(obj, message);
        } catch (Exception e) {
            TLogger.get(EvBus.class).e("Error while invoke \"EvReceiverDeclared\" method " + method.getName() + ", contextClass:" + obj.getClass(), e);
        }
    }

}
