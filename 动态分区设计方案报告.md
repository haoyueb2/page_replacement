# 动态分区分配设计方案报告

**1652613 白皓月**

开发语言为java，界面基于javafx构建

## 项目背景

假设初始态下，可用内存空间为640K，并有下列请求序列，请分别用首次适应算法和最佳适应算法进行内存块的分配和回收，并显示出每次分配和回收后的空闲分区链的情况来。

## 算法设计

以javafx的TableView组件展示当前的内存情况，第一列是各内存块的大小，第二列展示的是这个内存块是由哪个作业申请的，-1表示当前内存块是空闲的。从上到下为从低地址到高地址。

初始状态如图，表示有640K的空闲内存块。

![1559828566361](C:\Users\74293\AppData\Roaming\Typora\typora-user-images\1559828566361.png)

该组件绑定了javafx的ObservableList：

```java
  public class Block {
        public int size;
        public int task;
        Block(int size_,int task_) {
            size = size_;
            task = task_;
        }
        public int getSize() {
            return size;
        }
        public int getTask() {
            return task;
        }
    }
 private ObservableList<Block> memory =
            FXCollections.observableArrayList(new Block(640,-1));

```

执行的任务序列仍然是按照ppt来的：

>         作业1申请130K
>         作业2申请60K
>         作业3申请100k
>         作业2释放60K
>         作业4申请200K
>         作业3释放100K
>         作业1释放130K
>         作业5申请140K
>         作业6申请60K
>         作业7申请50K
>         作业6释放60K

任务序列是采用了LinkedList存储：

```java
public class Task {
    public int size;
    public int task;
    //true表示申请内存，false表示释放内存
    public boolean type;
    Task(int size_ ,int task_,boolean type_) {
        size = size_;
        task = task_;
        type = type_;
    }
}

Deque<Task> taskSequence = new LinkedList<>();
```

在初始化函数initialize中将每个任务用offer函数添加进去。

### 最先适配算法

点击`首次适应(单步执行)`按钮会执行singleFirstFit函数。由于任务序列数较少，故我只设置了单步执行的按钮。按钮第一次按下即会把另一个最佳适应算法的按钮设为disable。

每单步执行一次，taskSequence会poll出当前队列的头部，是这次要处理的任务。如果其type值为true，表示为申请内存。那就循环memory，找第一个size大于申请的内存的size 的空闲块。然后将改空闲块的size减少相应的size，并在其前方添加新申请的这块内存块。

```java
        Task now = taskSequence.poll();
        if(now == null) return;
        //当前任务是申请内存
        if(now.type == true) {
            for(int i = 0; i< memory.size(); i++) {
                if(memory.get(i).task == -1 && memory.get(i).size > now.size) {
                    memory.get(i).size -= now.size;
                    memory.add(i,new Block(now.size,now.task));
                    //首次适应找到第一个就好。没有break的话，add后size增大了，可能继续下去。
                    break;
                }
            }
        }
```

如果其type值为false，表示为释放内存，那就在memory中循环找到该作业占据的内存块，将其值设为-1.如果其后边还有空闲区，那就将后面的空闲区合并，如果其前边还有空闲区，那就再合并到前方的空闲区。

```java
//当前任务是释放内存
        else {
            for(int i =0 ;i < memory.size() ;i++) {
                if(memory.get(i).task == now.task) {
                    memory.get(i).task = -1;
                    table.refresh();
                    //先合并后边的空闲区，再合并到前边的，避免索引混乱
                    if(i <= memory.size() -2 && memory.get(i+1).task == -1) {
                        memory.get(i).size += memory.get(i+1).size;
                        memory.remove(i+1);
                    }
                    if(i>= 1 && memory.get(i-1).task == -1) {
                        memory.get(i-1).size += now.size;
                        memory.remove(i);
                    }
                }
            }
        }
```

## 最佳适应算法

最佳适应算法的实现大体思路与最先适应算法差不多，只是找空闲区时是在满足条件的空闲区中找最小的。

```java
        Task now = taskSequence.poll();
        if(now == null) return;
        //当前任务是申请内存
        if(now.type == true) {
            int minSize = 999;
            int min = -1;
            for(int i = 0; i< memory.size(); i++) {
                //在满足条件的空闲区中找最小的
                if(memory.get(i).task == -1 && memory.get(i).size > now.size &&  memory.get(i).size < minSize) {
                    minSize = memory.get(i).size;
                    min = i;
                }
            }
            //如果找到了
            if(min != -1) {
                memory.get(min).size -= now.size;
                memory.add(min,new Block(now.size,now.task));
            }
        }
```

释放内存的操作一样。此处不再赘述。

## 展示

首次适应算法按前述的任务序列执行到最后的结果：

![1559830487055](C:\Users\74293\AppData\Roaming\Typora\typora-user-images\1559830487055.png)

最佳适应算法按前述的任务序列执行到最后的结果：

![1559830560414](C:\Users\74293\AppData\Roaming\Typora\typora-user-images\1559830560414.png)

点击重置：memory会回到初始状态，并且按钮状态也都恢复成可按下。任务序列也会被重置。

![1559830624044](C:\Users\74293\AppData\Roaming\Typora\typora-user-images\1559830624044.png)

