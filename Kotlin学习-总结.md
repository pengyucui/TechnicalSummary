### 类

**1、**Kotlin中使用class关键字声明类.例：

```Java 
class Test {
  //具体实现
}
```

**2**、类中的定义变量

使用`val`和`var`分别是定义只能赋值一次不可变和可以多次赋值可变的。如下：

```java
var a = 0
val b = 3.1415926
```

使用伴生对象定义静态的变量和方法。如下：

```java
  companion object {
        var a = 0
        val b = 3.1415926
        fun test(){
            a++
        }
    }
```

**3、**类的构造函数

主构造函数是类头的一部分：它跟在类名（与可选的类型参数）后。配合`constructor`关键字。例：

```java
class People(name: String) {
    private var age: Int? = null
    private var school: String? = null
    private var name: String? = name
    init {
        println("这是People--init")
    }
    constructor(name: String, age: Int) : this(name) {
        this.age = age
    }

    constructor(name: String, age: Int, school: String) : this(name, age) {
        this.school = school
    }

    fun main() {
        println("这是People:$name,$age,$school")
    }
}
```

或者只使用`constructor`

```java
class People() {
    private var age: Int? = null
    private var school: String? = null
    private var name: String? = null

    init {
        println("这是People--init")
    }

    constructor(name: String) : this(name, 0) {
        this.name = name
    }

    constructor(name: String, age: Int) : this(name, age, "") {
        this.age = age
        this.name = name
    }

    constructor(name: String, age: Int, school: String) : this() {
        this.age = age
        this.name = name
        this.school = school
    }

    fun main() {
        println("这是People:$name,$age,$school")
    }
}
```

**4、**类的继承与实现

接口：

```java
interface AInterface {
  //方法
    fun test()
}
```

父类：

```java
/**
 * abstract或open关键字 声明该类可被继承
 * 否则会报错
 */
abstract class BClass(int: Int) {
  //用open声明表示该方法可重写
   open fun test01(){
    }
}
```

子类继承父类`BClass`并实现`AInterface`：

```java
/**
 * 来自官方文档
 * 如果派生类有一个主构造函数，其基类型可以（并且必须）用基类的主构造函数参数就地初始化。
 */
class AClass(int: Int) : BClass(int), AInterface {
    override fun test() {
        TODO("not implemented")
    }
    //final override  它的子类不可再重写此方法
    final override fun test01() {
      //这里的super或this跟java一样指父类或自身
        super.test01()
    }
}
```

**5、泛型**

（1、泛型类

​         和java使用方法类似

```java
class Box<T>(t: T) {
    var value = t
    fun test() {
        print(value)
    }
}
```

（2、泛型接口

```java
interface IBox<T> {
    fun add(t:T)
}
```

  (3、in 和 out  [参考连接](https://www.jianshu.com/p/c5ef8b30d768)  

​       如果你的类是将泛型作为内部方法的返回，那么可以用 out。例:

```kotlin
interface IBox<out T> {
    //返回list泛型
    fun add():List<T>?
}
```

​       如果你的类是将泛型对象作为函数的参数，那么可以用 in。例：

```kotlin
interface IBox<in T> {
    fun add(t:T)
}
```

​       如果既有传参又有返回那就不写，否则报错!!

```kotlin
class Box<T>(t: T) {
    fun add(t: T): List<T>? {
        return null
    }
}
```



### 控制流

**if的使用**

判断`hasGo`是否为`true`返回字符串。列：

```kotlin
var hasGo = true
val mString = if (hasGo) "hasGo 为 true" else "hasGo 为 false"
```

上面的例子有点类似java 中

```java
boolean hasGo = true;
String mString = hasGo == true ? "hasGo 为 true" : "hasGo 为 false";
```

**when的使用**

当我们根据一个可变的标识去做不同的响应时，在java中我们可以使用switch去处理。例：

```java
switch (waht) {
    case 0:
        break;
    case 1:
        break;
    case 2:
        break;
    default:
        break;
}
```

在kotlin中并没有switch，我们可以用when：

```kotlin
when (what) {
    0 -> "0"
    1 -> "1"
    3, 4 -> "3和4"
    else -> "默认"
}
```

**for循环**

有一个数组我想遍历它的值。例:

```kotlin
//其中(可以自己命名)index和value对应数组中的下标和下标所对应的值 
for ((index, value) in list.withIndex()) {
    print("position->$index,value->$value")
}
输出结果：
position->0,value->1
position->1,value->2
position->2,value->3
position->3,value->4
```

```kotlin
for (value in list){
    print("$value")
}
输出结果：1234
```

```kotlin
/*
 * list.indices:返回此集合的有效索引的
 **/
for (index in list.indices){
    print("${list[index]} ")
}
输出结果：1 2 3 4 
```

### 作用域函数的使用

**T.let()函数**

`let`通常用于仅使用非空值执行代码块。要对非空对象执行操作，请在该对象上使用安全调用运算符`?.`。

```kotlin
fun useLet() {
    var str:String?=null
    str?.let {
        //it指str
        println(it)
    }
}
```

*默认当前这个对象作为闭包的it参数，返回值是函数里面最后一行，或者指定return*

```kotlin
val str: String = "a"
val result = str.let {
    println(it)
    69
    "75"//返回值
}
println(result)
//输出结果
a
75
```

**run()和T.run()函数**

*1、`run()`作为代码块使用时他可以是独立的，它不会影响整个项目的正常运行。如下：*

```kotlin
val str = "kotlin"
run {
    val str = "kotlin in run"
    println(str)
}
println(str)
//输出结果
kotlin in run
kotlin
```

*2、`T.run()`想要使用当前对象的上下文的时候，我们可以使用这个函数。如下：*

```kotlin
user.run {
    userAge = 18
    userName = "BryceCui"
}
```

***with()函数***

可以直接调用对象的方法

```kotlin
private lateinit var grid: RecyclerView
 with(grid) {
     layoutManager = gridLayoutManager
     adapter = feedAdapter
     addOnScrollListener(toolbarElevation)
     addOnScrollListener(infiniteScrollListener)
     setHasFixedSize(true)
     addItemDecoration( GridItemDividerDecoration(
              this@HomeActivity, R.dimen.divider_height,
              R.color.divider
            )
      )
            itemAnimator = HomeGridItemAnimator()
            addOnScrollListener(shotPreloader)
 }
```

**apply()函数**

在函数范围内，可以调用该对象的方法，并返回该对象

```kotlin
mutableListOf<String>().apply {
    add("add first")
    add("add second")
    println(this)
}.let { print(it[0]) }
//输出结果
[add first, add second]
add first
```

### 集合

``` kotlin
//将可变集合转为不可变
toImmutableMap
toImmutableList
```

**List**

创建`list`

```kotlin
 /**
 * 不可添加删除
 */
val list= listOf<Int>(1,2,3)
 /**
 * 可添加删除
 * 默认创建ArrayList()
 */
val listMutable = mutableListOf<Int>(1,2,3)
```

获取元素

```kotlin
//获取下标为0的值
listMutable[0]
listMutable.get(0)
```

删除元素

```kotlin
//删除指定位置元素
listMutable.removeAt(0)
//删除指定元素
listMutable.remove(2)
```

**set**

创建`set`

```kotlin
//不可变
val set = setOf(1, 2, 3)
/**
* 创建可变set
* 默认创建LinkedHashSet()
*/
val setMutable = mutableSetOf(1, 2, 3)
/**
 * 可变set集合操作
 * 删除元素remove
 * 添加元素add
 */
setMutable.remove(1)
setMutable.add(4)
```

**map**

创建`map`

```kotlin
/**
 * 创建可变map
 * 默认创建的是LinkedHashMap()
 */
val mutableMap = mutableMapOf<String, Int>()
/**
 * 创建不可变map
 */
val map = mapOf<String, Int>()
/**
 * 创建hashMap
 */
val hashMap = hashMapOf<String, Int>()
```

获取元素

```kotlin
mutableMap["key"]
mutableMap.get("key")

hashMap["key"]
hashMap.get("key")
```

删除元素

```kotlin
/**
 * 删除指定key元素
 */
mutableMap.remove("key")
/**
 * 删除指定key，value元素
 */
mutableMap.remove("key", "value")
```



### 操作符重载

**自增操作`inc()`和自减操作`dec()`**

官方文档：inc() 和 dec() 函数必须返回⼀个值，它⽤于赋值给使⽤ ++ 或 -- 操作的变量。它们不应该改变 在其上调⽤ inc() 或 dec() 的对象。

 也就是不改变原有的值，而是返回一个自增或者自减的结果

