# Handler作用
Android 规定只有在主线程(UI线程)更新UI，否则会抛出异常，并且Android又建议不要在主线程做线程去做好事操作，否则可能会导致ANR。
所以Handler的主要作用是解决子线程无法更新UI的问题。
# Handler构成
Handler主要是由`MessageQueue`,`Looper`构成。
#### MessageQueue：
1、MessageQueue(消息队列)是以队列的形式插入和删除，采用的是单链表的形式存储消息。  
2、MessageQueue主要有两个操作：插入和读取(删除)消息。  
①使用enqueueMessage去往MessageQueue中插入一条消息。  
②使用next从MessageQueue中读取一条消息并在MessageQueue中删除该消息。
#### Looper：
1、Looper(负责循环)是以无限循环的形式查找消息，当所有的消息处理完应该终止消息循环，否则会一直处于等待状态。  
2、Looper的创建：  
①在子线程使用Handler时`必须先手动创建一个Looper`，否则会抛异常。使用Looper.prepare方法创建。  
②在主线程中ActivityThread已经默认使用prepareMainLooper方法为我们创建了，其内部也是使用了prepare方法创建的。  
3、Looper的退出：  
①quit会直接退出Looper
②quitSafely是处理完消息后才退出
4、Looper的循环
使用Looper.loop方法开启循环。
#### Handler工作原理：
Handler通过post或者send方法发送一条消息到MessageQueue并插入。Looper发现有新消息就会通过Handler的dispatchMessage处理这条消息。
```java
    public void dispatchMessage(Message msg) {
        if (msg.callback != null) {
            handleCallback(msg);
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            handleMessage(msg);
        }
    }
```
检查Message的callback是否为空也就是Handler的post方法传的Runnable，不为空由handleCallback处理。
为空再去判断mCallback是否为空，不为空就调用mCallback.handleMessage，最后调用handleMessage处理消息。




##### d参考《Android 开发艺术探索》