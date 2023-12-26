package com.cooper.wheellog.utils

object ReflectUtil {
    fun setPrivateField(`object`: Any, propertyName: String?, value: Any?): Boolean {
        return try {
            val wdField = `object`.javaClass.getDeclaredField(propertyName)
            wdField.isAccessible = true
            wdField[`object`] = value
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
