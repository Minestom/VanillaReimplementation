# luajava

一款使 `lua` 可以方便的调用 `java` 的工具.

## 介绍
LuaJava is a scripting tool for Java. The goal of this tool is to allow scripts written in Lua to manipulate components developed in Java.

It allows Java components to be accessed from Lua using the same syntax that is used for accessing Lua`s native objects, without any need
for declarations or any kind of preprocessing.  LuaJava also allows Java to implement an interface using Lua. This way any interface can be
implemented in Lua and passed as parameter to any method, and when called, the equivalent function will be called in Lua, and it's result
passed back to Java.

## 与原luajava项目变化

* 适配 luajava 至 lua5.3.5 版本
* lua 源码升级至 5.3.5 版本
* windows 下 nmake 下编译
* 其他系统自行编译
	
## 编译

###windows下 nmake 编译
#### x86版本DLL

1. 配置 JAVA_HOME 环境变量.

2. cmd 中,切换至项目目录并运行如下命令.

	```
	"C:\Program Files (x86)\Microsoft Visual Studio 12.0\VC\vcvarsall.bat" X86

	nmake

	```
3. 编译完成,在项目目录中可看到 **luajava-1.0.dll**、**luajava-1.0.jar** 2个文件.

#### x64版本Dll
只需将第二步 "**X86**" 替换为 "**X64**" 即可.


注意:其中 vcvarsall.bat 的路径需要改为你自己的实际路径.

若需编译好的文件,直接到 **release** 分支下载

![编译截图](https://s2.ax1x.com/2019/07/25/eeZyQ0.jpg)

#### nmake 相关命令解释

命令         | 解释 
:-           | :- 
nmake build  | 等于jar,dll,clean三个命令(默认)
nmake dll    |  只生成luajava dll文件
nmake jar    |  只生成luajava jar文件
nmake clean  |  清除编译过程中间文件

### 其他系统,自行编译测试


## 测试

1. 交互方式执行lua
	`java -cp luajava-1.0.jar org/keplerproject/luajava/Console` 

2. 直接执行lua文件
	`java -cp luajava-1.0.jar org/keplerproject/luajava/Console  xxx.lua` 

3. 部分例子
	```
	java -cp luajava-1.0.jar org/keplerproject/luajava/Console ./test/awtTest.lua
	
	java -cp luajava-1.0.jar org/keplerproject/luajava/Console ./test/simpleLuaFile.lua
	
	java -cp luajava-1.0.jar org/keplerproject/luajava/Console ./test/testMap.lua
	
	java -cp luajava-1.0.jar org/keplerproject/luajava/Console ./test/swingTest.lua
	
	java -cp luajava-1.0.jar org/keplerproject/luajava/Console
	```

![例子截图](https://s2.ax1x.com/2019/07/25/eeZKde.jpg)

## 其它
- 如果你有好的建议或者发现bug，欢迎给我提 **issue**.
- 如果该repo对大家有帮助，给个star鼓励鼓励吧.

## 常见问题

1.  Can't load AMD 64-bit .dll on a IA 32-bit platform
答:这个是由于 JAVA 虚拟机版本和 DLL 版本不一致,比如64位虚拟机配64位DLL.


## 致谢

- [原luajva项目](https://github.com/jasonsantos/luajava)

