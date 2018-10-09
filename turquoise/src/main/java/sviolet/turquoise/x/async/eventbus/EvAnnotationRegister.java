package sviolet.turquoise.x.async.eventbus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import sviolet.thistle.util.reflect.ReflectCache;

/**
 * 注释式注册逻辑
 */
class EvAnnotationRegister {

    static void register(EvStation evStation, Object context) throws EvBus.MissingMessageException{
        Class<?> contextClass = context.getClass();

        // fields
        Field[] fields = ReflectCache.getDeclaredFields(contextClass);
        for (Field field : fields) {
            if (!field.isAnnotationPresent(EvTransmitPopDeclare.class)) {
                continue;
            }
            EvTransmitPopDeclare evTransmitPopDeclare = field.getAnnotation(EvTransmitPopDeclare.class);
            Class<?> fieldType = field.getType();
            if (!EvMessage.class.isAssignableFrom(fieldType)) {
                throw new RuntimeException("[EvBus]registerAnnotations: Field " + field.getName() + " is not an implement of EvMessage, contextClass:" + contextClass.getName() + ", field type:" + fieldType.getName());
            }
            EvMessage message;
            if (evTransmitPopDeclare.remove()) {
                message = evStation.removeTransmitMessage((Class<? extends EvMessage>)fieldType);
            } else {
                message = evStation.popTransmitMessage((Class<? extends EvMessage>)fieldType);
            }
            if (message == null) {
                if (evTransmitPopDeclare.required()) {
                    throw new EvBus.MissingMessageException((Class<? extends EvMessage>)fieldType, "[EvBus]registerAnnotations: Missing required EvMessage, contextClass:" + contextClass.getName() + ", messageClass:" + fieldType.getName() + ", field:" + field.getName());
                }
            } else {
                try {
                    field.set(context, message);
                } catch (Exception e) {
                    throw new RuntimeException("[EvBus]registerAnnotations: Error while setting message to field, contextClass:" + contextClass.getName() + ", messageClass:" + fieldType.getName() + ", field:" + field.getName(), e);
                }
            }
        }

        // methods
        Method[] methods = ReflectCache.getDeclaredMethods(contextClass);
        for (Method method : methods) {
            if (!method.isAnnotationPresent(EvReceiverDeclare.class)) {
                continue;
            }
            EvReceiverDeclare evReceiverDeclare = method.getAnnotation(EvReceiverDeclare.class);
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new RuntimeException("[EvBus]registerAnnotations: Method parameters can only be one, params num: " + parameterTypes.length + ", contextClass:" + contextClass.getName() + ", method:" + method.getName());
            }
            Class<?> parameterType = parameterTypes[0];
            if (!EvMessage.class.isAssignableFrom(parameterType)) {
                throw new RuntimeException("[EvBus]registerAnnotations: Method parameter is not an implement of EvMessage, parameter type:" + parameterType.getName() + ", contextClass" + contextClass.getName() + ", method:" + method.getName());
            }
            if (!evStation.registerByAnnotation((Class<? extends EvMessage>) parameterType, evReceiverDeclare.type(), new EvReceiverForMethodAnnotation(context, method))){
                throw new RuntimeException("[EvBus]registerAnnotations: Duplicate receiver with the same message type:" + parameterType.getName() + ", contextClass" + contextClass.getName() + ", method:" + method.getName());
            }
        }

    }

}
