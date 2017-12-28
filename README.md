# IBleLib

IBLeLib是一个安卓Android平台的蓝牙库，可以方便的实现蓝牙的搜索、连接、数据读取的操作。

![IBleLibLogo](https://file.2fun.xyz/ible_show_logo.jpg)

## 使用方法

所有的操作都简化到了一个类里面`IBleManager`，通过调用`IBleManager.getInstance()`来获取单例对象。

#### 一、打开关闭

* 打开蓝牙

  ```java
  IBleManager.getInstance().openBle()
  ```


* 关闭蓝牙

  ```java
  IBleManager.getInstance().closeBle()
  ```

* 判断蓝牙是否打开

  ```java
  IBleManager.getInstance().isOpen()
  ```
#### 二、搜索

* 开始搜索

  ```java
  IBleManager.getInstance().startSearch(Context activity, IBleSearchManager.OnIBleSearchListener listener)
  ```

* 结束搜索

  ```java
  IBleManager.getInstance().stopSearch() 
  ```

#### 三、连接

* 配对

  ```java
  IBleManager.getInstance().bond(String mac, OnBleBondListener listener)
  ```

* 开始连接

  ```java
  IBleManager.getInstance().connect(Context context, String mac, UUID uuid, OnBleConnectListener listener)
  ```

  **UUID**:UUID是通用唯一识别码（Universally Unique Identifier）的缩写，蓝牙的每一个服务通过特定的UUID来标识，可以通过查看蓝牙服务于UUID的对应关系来找到自己需要的UUID。

* 断开连接

  ```java
  IBleManager.getInstance().disConnect(String mac) 
  ```

* 判断连接是否成功

  ```java
  IBleManager.getInstance().isConnect(String mac)
  ```

#### 四、注意事项

蓝牙的搜索需要使用到**定位权限**，需要动态申请权限的时候请务必先获取权限然后在搜索蓝牙。
