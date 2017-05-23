package sviolet.turquoise.kotlin.extensions

/**
 * kotlin common extensions
 *
 * Created by S.Violet on 2017/5/23.
 */

/**
 * Get jvm class name, return null if receiver is null
 */
fun Any?.getClassName() : String{
    val instanceString = this.toString()
    if ("@" in instanceString){
        return instanceString.split("@")[0]
    } else {
        return "null"
    }
}

/**
 * Get jvm class simple name (without package), return null if receiver is null
 */
fun Any?.getSimpleClassName() : String{
    val className = getClassName()
    if (className == "null") return className
    val list = className.split(".")
    if (className.startsWith("[L")){
        return list[list.size - 1].replace(";", "[]")
    } else if (className.startsWith("[[L")) {
        return list[list.size - 1].replace(";", "[][]")
    } else {
        return list[list.size - 1]
    }
}