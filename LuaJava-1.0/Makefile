#
# Makefile for Windows
#

#############################################################
#Windows
LUA_VERSION = 5.3.5
LUA_SRC = lua-$(LUA_VERSION)/src
JDK = $(JAVA_HOME)
INCS = -I"$(JDK)/include"  -I"$(JDK)/include/win32"  -I"$(LUA_SRC)"
CFLAGS = -nologo  -DLUA_COMPAT_5_1 -DLUA_COMPAT_5_2 -DLUA_BUILD_AS_DLL  $(INCS) -Fosrc\c\ -MD

#########################################################
LUAJAVA_VERSION = 1.0
PKG = luajava-$(LUAJAVA_VERSION)
JAR_FILE = $(PKG).jar
DLL_FILE = $(PKG).dll


CLASSES = src/java/org/keplerproject/luajava/CPtr.class \
	src/java/org/keplerproject/luajava/JavaFunction.class \
	src/java/org/keplerproject/luajava/LuaException.class \
	src/java/org/keplerproject/luajava/LuaInvocationHandler.class \
	src/java/org/keplerproject/luajava/LuaJavaAPI.class \
	src/java/org/keplerproject/luajava/LuaObject.class \
	src/java/org/keplerproject/luajava/LuaState.class \
	src/java/org/keplerproject/luajava/LuaStateFactory.class \
	src/java/org/keplerproject/luajava/Console.class

#
# Targets
#

build: dll jar clean
	@echo ------------------
	@echo Build Complete
	@echo ------------------

#
# Build .class files.   $(JAR_FILE)   $(DLL_FILE)
#

.SUFFIXES: .java
.java.class:
	@"$(JDK)\bin\javac"  -sourcepath src/java/   $*.java


jar:checkjdk $(CLASSES)
	@cd src/java
	@"$(JDK)\bin\jar" -cvf ../../$(JAR_FILE) org/keplerproject/luajava/*.class
	@cd ..
	@cd ..

dll:
	 $(CC) $(CFLAGS) /c $(LUA_SRC)/*.c  src\c\luajava.c
	 -@del  src\c\lua.obj  src\c\luac.obj
	 link /dll src\c\*.obj   /out:$(DLL_FILE)

checkjdk:"$(JDK)\bin\java.exe"

test:
	@echo test

clean:
	-@del src\java\org\keplerproject\luajava\*.class src\c\*.obj *.exp *.lib




