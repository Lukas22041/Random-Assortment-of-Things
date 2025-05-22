package assortment_of_things.misc

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URL
import java.net.URLClassLoader

object ReflectionUtils {

    private val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
    private val setFieldHandle = MethodHandles.lookup().findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Any::class.java, Any::class.java))
    private val getFieldHandle = MethodHandles.lookup().findVirtual(fieldClass, "get", MethodType.methodType(Any::class.java, Any::class.java))
    private val getFieldNameHandle = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String::class.java))
    private val setFieldAccessibleHandle = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))
    private val getFieldTypeHandle = MethodHandles.lookup().findVirtual(fieldClass, "getType", MethodType.methodType(Class::class.java))

    private val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    private val getMethodNameHandle = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
    private val invokeMethodHandle = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

    @JvmStatic
    fun set(fieldName: String, instanceToModify: Any, newValue: Any?)
    {
        var field: Any? = null
        try {  field = instanceToModify.javaClass.getField(fieldName) } catch (e: Throwable) {
            try {  field = instanceToModify.javaClass.getDeclaredField(fieldName) } catch (e: Throwable) { }
        }

        setFieldAccessibleHandle.invoke(field, true)
        setFieldHandle.invoke(field, instanceToModify, newValue)
    }

    fun setFieldOfType(type: Class<*>, instanceToModify: Any, newValue: Any?)
    {
        var decFieldsA: Array<Any> = instanceToModify.javaClass.declaredFields as Array<Any>
        var fields: MutableList<Any> = decFieldsA.toMutableList()
        var nonDecFieldsA: Array<Any> = instanceToModify.javaClass.fields as Array<Any>
        var nonDecFields: MutableList<Any> = nonDecFieldsA.toMutableList()

        fields.addAll(nonDecFields)

        for (field: Any in fields)
        {
            setFieldAccessibleHandle.invoke(field, true)
            var fieldType: Class<*> = getFieldTypeHandle.invoke(field) as Class<*>
            if (fieldType == type) {
                setFieldHandle.invoke(field, instanceToModify, newValue)
            }
        }
    }

    fun get(fieldName: String, instanceToGetFrom: Any) : Any? {
        var field: Any? = null
        try {  field = instanceToGetFrom.javaClass.getField(fieldName) } catch (e: Throwable) {
            try {  field = instanceToGetFrom.javaClass.getDeclaredField(fieldName) } catch (e: Throwable) { }
        }

        setFieldAccessibleHandle.invoke(field, true)
        return getFieldHandle.invoke(field, instanceToGetFrom)
    }

    fun getWithSuper(fieldName: String, instanceToGetFrom: Any, limit: Int, clazz: Class<*>? = null) : Any?
    {
        if (limit < 0) return null

        var field: Any? = null
        var claz = clazz
        if (claz == null) claz = instanceToGetFrom.javaClass

        try {  field = instanceToGetFrom.javaClass.getField(fieldName) } catch (e: Throwable) {
            try {  field = instanceToGetFrom.javaClass.getDeclaredField(fieldName) } catch (e: Throwable) { }
        }
        if (field == null) {
            return getWithSuper(fieldName, instanceToGetFrom, limit-1, claz.superclass)
        }

        setFieldAccessibleHandle.invoke(field, true)
        return getFieldHandle.invoke(field, instanceToGetFrom)
    }

    fun getFieldOfType(type: Class<*>, instanceToGetFrom: Any)  : Any?
    {
        var decFieldsA: Array<Any> = instanceToGetFrom.javaClass.declaredFields as Array<Any>
        var fields: MutableList<Any> = decFieldsA.toMutableList()
        var nonDecFieldsA: Array<Any> = instanceToGetFrom.javaClass.fields as Array<Any>
        var nonDecFields: MutableList<Any> = nonDecFieldsA.toMutableList()

        fields.addAll(nonDecFields)

        for (field: Any in fields)
        {
            setFieldAccessibleHandle.invoke(field, true)
            var fieldType: Class<*> = getFieldTypeHandle.invoke(field) as Class<*>
            if (fieldType == type) {
                return getFieldHandle.invoke(field, instanceToGetFrom) as Any?
            }

            //check for the super too
            if (fieldType.superclass == type) {
                return getFieldHandle.invoke(field, instanceToGetFrom) as Any?
            }
        }
        return null
    }

    fun hasMethodOfName(name: String, instance: Any, contains: Boolean = false) : Boolean {
        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods() as Array<out Any>

        if (!contains) {
            return instancesOfMethods.any { getMethodNameHandle.invoke(it) == name }
        }
        else  {
            return instancesOfMethods.any { (getMethodNameHandle.invoke(it) as String).contains(name) }
        }
    }

    fun hasVariableOfName(name: String, instance: Any) : Boolean {

        val instancesOfFields: Array<out Any> = instance.javaClass.getDeclaredFields() as Array<out Any>
        return instancesOfFields.any { getFieldNameHandle.invoke(it) == name }
    }

    fun instantiate(clazz: Class<*>, vararg arguments: Any?) : Any?
    {
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it!!::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        val constructorHandle = MethodHandles.lookup().findConstructor(clazz, methodType)
        val instance = constructorHandle.invokeWithArguments(arguments.toList())

        return instance
    }

    fun invoke(methodName: String, instance: Any, vararg arguments: Any?, declared: Boolean = false) : Any?
    {
        var method: Any? = null

        val clazz = instance.javaClass
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        if (!declared) {
            method = clazz.getMethod(methodName, *methodType.parameterArray()) as Any?
        }
        else  {
            method = clazz.getDeclaredMethod(methodName, *methodType.parameterArray()) as Any?
        }

        return invokeMethodHandle.invoke(method, instance, arguments)
    }

    fun getMethodsOfName(name: String, instance: Any): List<Any> {
        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        return instancesOfMethods.filter { getMethodNameHandle.invoke(it) == name }
    }

    fun rawInvoke(method: Any?, instance: Any, vararg arguments: Any?): Any? {
        return invokeMethodHandle.invoke(method, instance, arguments)
    }

    fun getField(fieldName: String, instanceToGetFrom: Any) : ReflectedField? {
        var field: Any? = null
        try {  field = instanceToGetFrom.javaClass.getField(fieldName) } catch (e: Throwable) {
            try {  field = instanceToGetFrom.javaClass.getDeclaredField(fieldName) } catch (e: Throwable) { }
        }

        if (field == null) return null

        setFieldAccessibleHandle.invoke(field, true)
        return ReflectedField(field)
    }

    fun getField(fieldName: String, clazz: Class<*>) : ReflectedField? {
        var field: Any? = null
        try {  field = clazz.getField(fieldName) } catch (e: Throwable) {
            try {  field = clazz.getDeclaredField(fieldName) } catch (e: Throwable) { }
        }

        if (field == null) return null

        setFieldAccessibleHandle.invoke(field, true)
        return ReflectedField(field)
    }

    fun getMethod(methodName: String, instance: Any, vararg arguments: Any?) : ReflectedMethod? {
        var method: Any? = null

        val clazz = instance.javaClass
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        try { method = clazz.getMethod(methodName, *methodType.parameterArray())  }
            catch (e: Throwable) {
            try {  method = clazz.getDeclaredMethod(methodName, *methodType.parameterArray()) } catch (e: Throwable) { }
        }

        if (method == null) return null
        return ReflectedMethod(method)
    }

    fun getMethod(methodName: String, clazz: Class<*>, vararg arguments: Any?) : ReflectedMethod? {
        var method: Any? = null

        val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        try { method = clazz.getMethod(methodName, *methodType.parameterArray())  }
        catch (e: Throwable) {
            try {  method = clazz.getDeclaredMethod(methodName, *methodType.parameterArray()) } catch (e: Throwable) { }
        }

        if (method == null) return null
        return ReflectedMethod(method)
    }

    fun createClassThroughCustomLoader(claz: Class<*>) : MethodHandle
    {
        var loader = this::class.java.classLoader
        val urls: Array<URL> = (loader as URLClassLoader).urLs
        val reflectionLoader: Class<*> = object : URLClassLoader(urls, ClassLoader.getSystemClassLoader()) {
        }.loadClass(claz.name)
        var handle = MethodHandles.lookup().findConstructor(reflectionLoader, MethodType.methodType(Void.TYPE))
        return handle
    }

    fun setWithSuper(fieldName: String, instanceToModify: Any, newValue: Any?, limit: Int, clazz: Class<*>? = null)
    {
        if (limit < 0) return

        var field: Any? = null
        var claz = clazz
        if (claz == null) claz = instanceToModify.javaClass

        try {  field = claz.getField(fieldName) } catch (e: Throwable) {
            try {  field = claz.getDeclaredField(fieldName) } catch (e: Throwable) { }
        }
        if (field == null) {
            setWithSuper(fieldName, instanceToModify, newValue, limit-1, claz.superclass)
            return
        }

        setFieldAccessibleHandle.invoke(field, true)
        setFieldHandle.invoke(field, instanceToModify, newValue)
    }

    class ReflectedField(private val field: Any) {
        fun get(instance: Any?): Any? = getFieldHandle.invoke(field, instance)
        fun set(instance: Any?, value: Any?) {
            setFieldHandle.invoke(field, instance, value)
        }
    }

    class ReflectedMethod(private val method: Any) {
        fun invoke(instance: Any?, vararg arguments: Any?): Any? = invokeMethodHandle.invoke(method, instance, arguments)
    }
}