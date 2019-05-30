

## 前言

由于最近项目中频繁使用[EventBus](https://github.com/greenrobot/EventBus)，所以想了解下它内部原理，我们得"知其然知其所以然"。在这里总结一下，方便自己复习。 

**[欢迎start我的GitHub项目](https://github.com/BryceCui/TechnicalSummary)，里面会不定时更细Android 、java、设计模式相关知识！**

## EventBus的使用

### 项目配置

很简单只需要在`build.gradle(app)`下添加如下：

```java
implementation 'org.greenrobot:eventbus:3.1.1'
```

### 项目中使用

#### 普通使用：

1、发送消息：

```java
EventBus.getDefault().post(事件);
```

2、在接受页面注册和反注册，并接收。

```java
//注册
EventBus.getDefault().register(this);
//反注册
EventBus.getDefault().unregister(this);
//接收事件(getdata这名字是随便的，起什么都可以)
@Subscribe(threadMode = ThreadMode.MAIN)
   public void getdata(事件) {
  //在这里处理发送的事件
 }
```

#####普通使用例子：

比如现在有这样一个需求，有两个页面分别是`MainActivity`和`Main2Activity`。需要`MainActivity`跳转到`Main2Activity`，点击`Main2Activity`中的按钮返回并携带数据。（当然我们也可以使用`onActivityResult`去实现 -。-）

`MainActivity`代码:

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      //注册
        EventBus.getDefault().register(this);
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_tv:
                Intent intent = new Intent(this, Main2Activity.class);
                startActivity(intent);
                break;
        }
    }
  //订阅者方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getdata(Message message) {
        Toast.makeText(this, "Type=" + message.getType(), Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
      //反注册
        EventBus.getDefault().unregister(this);
    }
}
```

`Main2Activity`代码：

```java
public class Main2Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
    public void onClick2(View view) {
      //发送消息
        EventBus.getDefault().post(new Message(1));
        finish();
    }
}
```

`Message`事件实体:

```java
public class Message {
    private int type;

    public Message(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
}
```

**效果图： **

![EventBusPost](/Users/cuipengyu/Desktop/EventBusPost.gif)

可能有人会说，官网的是在onStart()/onStop()中注册和反注册，你怎么在onCreate方法中注册，在onDestory方法中反注册？这样的话有一些情况我们普通的方式去发送事件，是在需要接收消息页面会收不到，因为使用时需要往已经注册的页面发送消息，才能接受到。当我们发送消息时注册方法还没有被调用和关联订阅者，那么就无法接受事件。

#### 粘性事件使用：

1、发送消息：

```java
EventBus.getDefault().postSticky(事件);
```

2、在接受页面注册和反注册，并接收。

```java
//注册
EventBus.getDefault().register(this);
//反注册
EventBus.getDefault().unregister(this);
//接收事件(getdata这名字是随便的，起什么都可以)
@Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
   public void getdata(事件) {
  //在这里处理发送的事件
 }
```

##### 粘性事件例子;

有这样的一个需求，在MainActivity往Main2Activity（没有创建过）页面传递数据(当然我们也可以使用Intent携带Bundle传递)。

`MainActivity`代码：

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_tv:
            //发送消息
                EventBus.getDefault().postSticky(new Message(1));
                Intent intent = new Intent(this, Main2Activity.class);
                startActivity(intent);
                break;
        }
    }
}
```

`Main2Activity`代码：

```java
public class Main2Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
    @Override
    protected void onStart() {
        super.onStart();
      //注册
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
      //反注册
        EventBus.getDefault().unregister(this);
    }
    public void onClick2(View view) {
        finish();
    }
  //订阅者方法
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void getdata(Message message) {
        Toast.makeText(this, "MainActivity发送Type为" + message.getType(), Toast.LENGTH_SHORT).show();
    }
}
```

**效果图**

![EventBuspostSticky](/Users/cuipengyu/Desktop/EventBuspostSticky.gif)



## EventBus源码分析：

###***EventBus.getDefault()分析***

我们在使用时不管操作什么都需要调用`EventBus.getDefault()`方法。

```java
public static EventBus getDefault() {
    if (defaultInstance == null) {
        synchronized (EventBus.class) {
            if (defaultInstance == null) {
                defaultInstance = new EventBus();
            }
        }
    }
    return defaultInstance;
}
```

我们看上面源码可知，它用到了双重检锁获取到了EventBus的对象。

###***register注册分析***

```java
public void register(Object subscriber) {
  //获取注册的对象
 Class<?> subscriberClass = subscriber.getClass();
  //找到订阅方法集合
 List<SubscriberMethod>subscriberMethods=subscriberMethodFinder.findSubscriberMethods(subscriberClass);
    synchronized (this) {
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            //订阅
            subscribe(subscriber, subscriberMethod);
        }
    }
}
```

***解析1***`SubscriberMethod`是存储订阅方法信息：

```java
public class SubscriberMethod {
    final Method method;
    final ThreadMode threadMode;
    final Class<?> eventType;
    final int priority;
    final boolean sticky;
    /** Used for efficient comparison */
    String methodString;
    ......
    }
```

`ThreadMode`订阅者线程

`eventType`事件

`priority  `优先级

`sticky ` 是否粘性事件

`  Method`接受事件的方法(比如例子中的`getdata()`)

***解析2***`findSubscriberMethods`查找订阅者方法

```java

private static final Map<Class<?>, List<SubscriberMethod>> METHOD_CACHE=new ConcurrentHashMap<>();

List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
    //通过订阅者对象查找缓存（以ConcurrentHashMap存储的key为订阅者的类，value为订阅者方法集合）
    List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
    //查到的缓存不为空直接返回
    if (subscriberMethods != null) {
        return subscriberMethods;
    }
   //是否忽略生成的索引 ，默认false
    if (ignoreGeneratedIndex) {
      //以反射的方式获取订阅者订阅方法
        subscriberMethods = findUsingReflection(subscriberClass);
    } else {
      //以索引的形式获取
        subscriberMethods = findUsingInfo(subscriberClass);
    }   
    //判断内容是否为空，为空抛异常
    if (subscriberMethods.isEmpty()) {
        throw new EventBusException("Subscriber " + subscriberClass + " and its super classes have no public methods with the @Subscribe annotation");
    } else {
        //缓存到ConcurrentHashMap中，以便于下次查找
        METHOD_CACHE.put(subscriberClass, subscriberMethods);
        return subscriberMethods;
    }
}
```

**解析2.1**`findUsingReflection`

```java
private List<SubscriberMethod> findUsingReflection(Class<?> subscriberClass) {
   //创建FindState对象
  FindState findState = prepareFindState();
   //于订阅者类关联
    findState.initForSubscriber(subscriberClass);
    while (findState.clazz != null) {
         //使用反射获取订阅方法
        findUsingReflectionInSingleClass(findState);
        findState.moveToSuperclass();
    }
    //返回FindState中的订阅方法集合
    return getMethodsAndRelease(findState);
} 
```

***解析2.1.1***`findUsingReflectionInSingleClass`

```java
private void findUsingReflectionInSingleClass(FindState findState) {
    Method[] methods;
    try {
        // This is faster than getMethods, especially when subscribers are fat classes like Activities
        //获取本类中的所有方法
        methods = findState.clazz.getDeclaredMethods();
    } catch (Throwable th) {
        // Workaround for java.lang.NoClassDefFoundError, see https://github.com/greenrobot/EventBus/issues/149
        //获取本类和父类的所有公共方法
        methods = findState.clazz.getMethods();
        //设置标记
        findState.skipSuperClasses = true;
    }
    //遍历
    for (Method method : methods) {
        //返回方法的int值
        int modifiers = method.getModifiers();
         //忽略不是PUBLIC和static
        if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
          //获取参数类型
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1) {
              //获取Subscribe注解(里面有线程模型、是否粘性、优先级)
                Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                if (subscribeAnnotation != null) {
                    Class<?> eventType = parameterTypes[0];
                    if (findState.checkAdd(method, eventType)) {
                      //获取线程模型
                        ThreadMode threadMode = subscribeAnnotation.threadMode();
                      //添加到订阅者集合
                        findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode,subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
                    }
                }
              ......
}
```

***解析2.2***`findUsingInfo`

```java
private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
    FindState findState = prepareFindState();
    findState.initForSubscriber(subscriberClass);
    while (findState.clazz != null) {
        //第一次会为空
        findState.subscriberInfo = getSubscriberInfo(findState);
        if (findState.subscriberInfo != null) {
            SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods();
            for (SubscriberMethod subscriberMethod : array) {
             if(findState.checkAdd(subscriberMethod.method,subscriberMethod.eventType)){
                    findState.subscriberMethods.add(subscriberMethod);
                }
            }
        } else {
          //调用解析2.1.1反射获取
            findUsingReflectionInSingleClass(findState);
        }
      //在父类中查找
        findState.moveToSuperclass();
    }
    //返回订阅者集合
    return getMethodsAndRelease(findState);
}
```

***解析3***`subscribe`

```java
private final Map<Class<?>,CopyOnWriteArrayList<Subscription>>=subscriptionsByEventType;
private final Map<Object, List<Class<?>>> typesBySubscriber;

// Must be called in synchronized block
private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
    //获取的事件
    Class<?> eventType = subscriberMethod.eventType;
    //存储订阅者和订阅者方法信息
    Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
    CopyOnWriteArrayList<Subscription>subscriptions = subscriptionsByEventType.get(eventType);
    //是否为空，并存储
    if (subscriptions == null) {
        subscriptions = new CopyOnWriteArrayList<>();
        subscriptionsByEventType.put(eventType, subscriptions);
    } else {
        //判断是否已经注册过，不能重复注册，抛异常
        if (subscriptions.contains(newSubscription)) {
            throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "+ eventType);
        }
    }
    
    int size = subscriptions.size();
    //优先级排序
    for (int i = 0; i <= size; i++) {
        if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
            subscriptions.add(i, newSubscription);
            break;
        }
    }
    //获取当前订阅者的事件
    List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
    //为空创建并添加缓存中
    if (subscribedEvents == null) {
        subscribedEvents = new ArrayList<>();
        typesBySubscriber.put(subscriber, subscribedEvents);
    }
    //添加当前事件
    subscribedEvents.add(eventType);
    //是否是粘性事件
    if (subscriberMethod.sticky) {
        //默认true
        if (eventInheritance) {
            // Existing sticky events of all subclasses of eventType have to be considered.
            // Note: Iterating over all events may be inefficient with lots of sticky events,
            // thus data structure should be changed to allow a more efficient lookup
            // (e.g. an additional map storing sub classes of super classes: Class -> List<Class>).
            Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
            for (Map.Entry<Class<?>, Object> entry : entries) {
                Class<?> candidateEventType = entry.getKey();
                if (eventType.isAssignableFrom(candidateEventType)) {
                    Object stickyEvent = entry.getValue();
                    checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                }
            }
        } else {
            //获取粘性事件
            Object stickyEvent = stickyEvents.get(eventType);
            //发送事件
            checkPostStickyEventToSubscription(newSubscription, stickyEvent);
        }
    }
}
```

**注册总结： **![注册图](/Users/cuipengyu/Downloads/Eventbus注册流程图.png)

注册时发送事件`checkPostStickyEventToSubscription`会调用这个方法`postSingleEventForEventType`

### post普通发送分析

```java
private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
    @Override
    protected PostingThreadState initialValue() {
        return new PostingThreadState();
    }
};
```

使用`ThreadLocal`存储`PostingThreadState`,线程安全的

```java
/** For ThreadLocal, much faster to set (and get multiple values). */
fin al static class PostingThreadState {
   //事件队列集合
    final List<Object> eventQueue = new ArrayList<>();
   //是否发送
    boolean isPosting;
   //是否是主线程
    boolean isMainThread;
   //存储订阅信息的
    Subscription subscription;
  //事件
    Object event;
  //是否取消
    boolean canceled;
}
```


```java
/** Posts the given event to the event bus. */
public void post(Object event) {
    //
    PostingThreadState postingState = currentPostingThreadState.get();
   //获取事件队列集合
    List<Object> eventQueue = postingState.eventQueue;
   //添加当前事件
    eventQueue.add(event);
    //判断是否发送
    if (!postingState.isPosting) {
        //是否是主线程
        postingState.isMainThread = isMainThread();
        //更改是否发送过默认值
        postingState.isPosting = true;
       //判断是否取消
        if (postingState.canceled) {
            throw new EventBusException("Internal error. Abort state was not reset");
        }
        try {
            while (!eventQueue.isEmpty()) {
                //移除事件并传递
                postSingleEvent(eventQueue.remove(0), postingState);
            }
        } finally {
          //恢复状态
            postingState.isPosting = false;
            postingState.isMainThread = false;
        }
    }
}
```

`postSingleEvent`

```java
private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
    Class<?> eventClass = event.getClass();
    boolean subscriptionFound = false;
    //默认值为true
    if (eventInheritance) {
        //查找事件所有Class对象，包括超类和接口内部使用hashmap缓存
        List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
        int countTypes = eventTypes.size();
        //遍历
        for (int h = 0; h < countTypes; h++) {
            Class<?> clazz = eventTypes.get(h);
            //发送事件
            subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
        }
    } else {
        //发送事件
        subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
    }
    //subscriptionFound为true是调用 也就是 subscriptions为空或者size为0时
    if (!subscriptionFound) { 
        if (logNoSubscriberMessages) {
            logger.log(Level.FINE, "No subscribers registered for event " + eventClass);
        }
        if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class &&
                eventClass != SubscriberExceptionEvent.class) {
            post(new NoSubscriberEvent(this, event));
        }
    }
}
```

`postSingleEventForEventType`发送事件

```java
private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
    //存储订阅者信息
    CopyOnWriteArrayList<Subscription> subscriptions;
    synchronized (this) {
      //获取缓存
        subscriptions = subscriptionsByEventType.get(eventClass);
    }
  //如果不为空
    if (subscriptions != null && !subscriptions.isEmpty()) {
      //遍历
        for (Subscription subscription : subscriptions) {
          //设置订阅者的信息
            postingState.event = event;
            postingState.subscription = subscription;
            boolean aborted = false;
            try {
                postToSubscription(subscription, event, postingState.isMainThread);
                aborted = postingState.canceled;
            } finally {
               //清除状态
                postingState.event = null;
                postingState.subscription = null;
                postingState.canceled = false;
            }
            if (aborted) {
                break;
            }
        }
        return true;
    }
    return false;
}
```

`postToSubscription`判断哪个线程并通过反射调用订阅方法

```java
private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
  //判断获取的线程模式
    switch (subscription.subscriberMethod.threadMode) {
        case POSTING:
            //直接调用
            invokeSubscriber(subscription, event);
            break;
        case MAIN:
            //判断是否是主线程
            if (isMainThread) {
                invokeSubscriber(subscription, event);
            } else {
                mainThreadPoster.enqueue(subscription, event);
            }
            break;
        case MAIN_ORDERED:
            if (mainThreadPoster != null) {
                mainThreadPoster.enqueue(subscription, event);
            } else {
                // temporary: technically not correct as poster not decoupled from subscriber
                invokeSubscriber(subscription, event);
            }
            break;
        case BACKGROUND:
            if (isMainThread) {
                backgroundPoster.enqueue(subscription, event);
            } else {
                invokeSubscriber(subscription, event);
            }
            break;
        case ASYNC:
            asyncPoster.enqueue(subscription, event);
            break;
        default:
            throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
    }
}
```

**1、PostThread** 默认的线程模式，直接调用`invokeSubscriber`方法。

**发布事件和接收事件在同一个线程**

**2、MAIN**判断是否在主线程，true直接调用`invokeSubscriber`方法,false使用`mainThreadPoster`处理，他的视线类是`HandlerPoster`。加入队列使用handler发送消息在handler的handleMessage中去执行`invokeSubscriber`方法。

**事件的处理会在UI线程中执行**

**3、MAIN_ORDERED**先判断`mainThreadPoster`是否为空，true调用`mainThreadPoster`排队等待传递非阻塞的，否则直接调用`invokeSubscriber`方法。 

**事件的处理会在UI线程中执行**

**4、BACKGROUND**判断是否在主线程，true使用BackgroundPoster处理，false直接调用`invokeSubscriber`

**事件是在UI线程中发布出来的，那么该事件处理函数就会在新的线程中运行，如果事件本来就是子线程中发布出来的，那么该事件处理函数直接在发布事件的线程中执行**

**5、ASYNC**直接交由`AsyncPoster`处理 

**事件处理函数都会在新建的子线程中执行**

`invokeSubscriber`通过反射调用订阅方法

```java
void invokeSubscriber(Subscription subscription, Object event) {
    try {
        subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
    } catch (InvocationTargetException e) {
        handleSubscriberException(subscription, event, e.getCause());
    } catch (IllegalAccessException e) {
        throw new IllegalStateException("Unexpected exception", e);
    }
}
```

### postSticky粘性事件

```java
private final Map<Class<?>, Object> stickyEvents;
public void postSticky(Object event) {
    synchronized (stickyEvents) {
        stickyEvents.put(event.getClass(), event);
    }
    // Should be posted after it is putted, in case the subscriber wants to remove immediately
    post(event);
}
```

先同步，缓存到`stickyEvents`**（ConcurrentHashMap缓存的）**，之后调用post方法。之后流程跟普通发送事件一样。

### unregister 反注册分析

```java
/** Unregisters the given subscriber from all event classes. */
public synchronized void unregister(Object subscriber) {
  //获取订阅者集合
    List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
    if (subscribedTypes != null) {
      //遍历
        for (Class<?> eventType : subscribedTypes) {
            unsubscribeByEventType(subscriber, eventType);
        }
      //移除订阅者
        typesBySubscriber.remove(subscriber);
    } else {
        logger.log(Level.WARNING, "Subscriber to unregister was not registered before: " + subscriber.getClass());
    }
}
```

`unsubscribeByEventType`

```java
/** Only updates subscriptionsByEventType, not typesBySubscriber! Caller must update typesBySubscriber. */
private void unsubscribeByEventType(Object subscriber, Class<?> eventType) {
  //获取订阅者信息集合
    List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
    if (subscriptions != null) {
        int size = subscriptions.size();
      //遍历
        for (int i = 0; i < size; i++) {
            Subscription subscription = subscriptions.get(i);
          //判断是否是当前订阅者
            if (subscription.subscriber == subscriber) {
                subscription.active = false;
              //移除
                subscriptions.remove(i);
                i--;
                size--;
            }
        }
    }
}
```